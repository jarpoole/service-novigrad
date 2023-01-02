package com.jpoole.service_novigrad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ModifyService extends AppCompatActivity {

    Button createServiceButton, addTextFieldButton, addImageFieldButton, addNumberFieldButton, addDateFieldButton;
    EditText nameEditText, priceEditText;
    RecyclerView serviceElementsRecyclerView;
    ServiceElementsAdapter serviceElementsAdapter;
    ConstraintLayout createPageConstraintLayout;
    TextView createServiceTitle;

    FirebaseFirestore fStore;
    CollectionReference databaseServices;

    ListenerRegistration serviceDBListener;
    String serviceId;
    boolean successful;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_service);
        successful = false; //Successful will only be set to true if the user confirms they wish to modify or create a new service

        createServiceButton = findViewById(R.id.createServiceButton);
        nameEditText = findViewById(R.id.nameEditText);
        priceEditText = findViewById(R.id.priceEditText);
        addTextFieldButton = findViewById(R.id.addTextFieldButton);
        addImageFieldButton = findViewById(R.id.addImageFieldButton);
        addNumberFieldButton = findViewById(R.id.addNumberFieldButton);
        addDateFieldButton = findViewById(R.id.addDateFieldButton);
        createPageConstraintLayout = findViewById(R.id.createPageConstraintLayout);
        createServiceTitle = findViewById(R.id.createServiceTitle);

        //Configure Recycler view for form elements
        serviceElementsAdapter = new ServiceElementsAdapter(new ArrayList<ServiceElement>());
        serviceElementsRecyclerView = findViewById(R.id.formElementsRecyclerView);
        serviceElementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        serviceElementsRecyclerView.setAdapter(serviceElementsAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        serviceElementsRecyclerView.addItemDecoration(dividerItemDecoration);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int draggedFromListPosition = viewHolder.getAdapterPosition();
                int draggedToListPosition = target.getAdapterPosition();
                List<ServiceElement> serviceElements = serviceElementsAdapter.getServiceElements();

                if (draggedFromListPosition < draggedToListPosition) {
                    for (int i = draggedFromListPosition; i < draggedToListPosition; i++) {
                        Collections.swap(serviceElements, i, i + 1);
                    }
                } else {
                    for (int i = draggedFromListPosition; i > draggedToListPosition; i--) {
                        Collections.swap(serviceElements, i, i - 1);
                    }
                }

                //Collections.swap(serviceElementsAdapter.getServiceElements(), draggedFromListPosition, draggedToListPosition);
                serviceElementsAdapter.notifyItemMoved(draggedFromListPosition, draggedToListPosition);

                //ServiceElement temp = serviceElementsAdapter.getServiceElements().remove(draggedFromListPosition);
                //serviceElementsAdapter.getServiceElements().add(draggedToListPosition, temp);
                //serviceElementsAdapter.notifyDataSetChanged();

                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //Request focus to insure that all EditText instances update before the deletion
                createPageConstraintLayout.requestFocus();

                int swipedItemPosition = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT){
                    serviceElementsAdapter.getServiceElements().remove(swipedItemPosition);
                    serviceElementsAdapter.notifyItemRemoved(swipedItemPosition);

                    //serviceElementsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                //Using library here because it is a small visual addition and when I looked at the documentation for how to do it manually it was a large undertaking
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(ModifyService.this, R.color.colorAccent))
                        .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(serviceElementsRecyclerView);


        //Initialize Firebase
        FirebaseApp.initializeApp(this);
        fStore = FirebaseFirestore.getInstance();
        databaseServices = fStore.collection("services");


        Intent intent = getIntent();

        //Extract the modification mode
        Bundle extras = intent.getExtras();
        String modifyMode = extras.getString("MODIFY_MODE");
        if(modifyMode.equals("create")){
            serviceId = addService();
            createServiceButton.setText("Create Service");
            createServiceTitle.setText("Create Service");
        }else if (modifyMode.equals("edit")){
            serviceId = extras.getString("SERVICE_ID");
            createServiceButton.setText("Edit Service");
            createServiceTitle.setText("Edit Service");
        }else{
            return;
        }

        //Load in the selected service and add a listener
        DocumentReference serviceReference = databaseServices.document(serviceId);
        serviceDBListener = serviceReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                Service service = documentSnapshot.toObject(Service.class);
                nameEditText.setText(service.getName());
                priceEditText.setText(Double.toString(service.getPrice()));
                serviceElementsAdapter.setServiceElements(service.getServiceElements());
                serviceElementsAdapter.notifyDataSetChanged(); //Updated all items in Recycler view
            }
        });


        createServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Request focus to insure that all EditText instances update
                createPageConstraintLayout.requestFocus();

                if(modifyService(serviceId)){
                    //Display toast
                    if (modifyMode.equals("create")) {
                        Toast.makeText(getApplicationContext(), "Service added", Toast.LENGTH_LONG).show();
                    } else if (modifyMode.equals("modify")) {
                        Toast.makeText(getApplicationContext(), "Service modified", Toast.LENGTH_LONG).show();
                    }
                    successful = true;
                    finish();
                }
            }
        });

        addTextFieldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServiceElement serviceText = new ServiceElement();
                serviceText.setName("");
                serviceText.setType("text");
                serviceText.setValidationTypes(R.array.textFieldValidationOptions);
                serviceText.setFormElementTypeName("Text Field Name");
                serviceText.setDrawable(R.drawable.ic_baseline_text_fields_24);
                serviceElementsAdapter.getServiceElements().add(serviceText);
                serviceElementsRecyclerView.smoothScrollToPosition(serviceElementsAdapter.getServiceElements().size());
                serviceElementsAdapter.notifyItemInserted(serviceElementsAdapter.getServiceElements().size() - 1);
            }
        });

        addImageFieldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServiceElement serviceImage = new ServiceElement();
                serviceImage.setName("");
                serviceImage.setType("image");
                serviceImage.setValidationTypes(R.array.imageFieldValidationOptions);
                serviceImage.setFormElementTypeName("Image/Document Name");
                serviceImage.setDrawable(R.drawable.ic_baseline_image_24);
                serviceElementsAdapter.getServiceElements().add(serviceImage);
                serviceElementsRecyclerView.smoothScrollToPosition(serviceElementsAdapter.getServiceElements().size());
                serviceElementsAdapter.notifyItemInserted(serviceElementsAdapter.getServiceElements().size() - 1);
            }
        });

        addNumberFieldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServiceElement serviceImage = new ServiceElement();
                serviceImage.setName("");
                serviceImage.setType("number");
                serviceImage.setValidationTypes(R.array.numberFieldValidationOptions);
                serviceImage.setFormElementTypeName("Number Field Name");
                serviceImage.setDrawable(R.drawable.ic_baseline_dialpad_24);
                serviceElementsAdapter.getServiceElements().add(serviceImage);
                serviceElementsRecyclerView.smoothScrollToPosition(serviceElementsAdapter.getServiceElements().size());
                serviceElementsAdapter.notifyItemInserted(serviceElementsAdapter.getServiceElements().size() - 1);
            }
        });

        addDateFieldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServiceElement serviceImage = new ServiceElement();
                serviceImage.setName("");
                serviceImage.setType("date");
                serviceImage.setValidationTypes(R.array.dateFieldValidationOptions);
                serviceImage.setFormElementTypeName("Date Name");
                serviceImage.setDrawable(R.drawable.ic_baseline_date_range_24);
                serviceElementsAdapter.getServiceElements().add(serviceImage);
                serviceElementsRecyclerView.smoothScrollToPosition(serviceElementsAdapter.getServiceElements().size());
                serviceElementsAdapter.notifyItemInserted(serviceElementsAdapter.getServiceElements().size() - 1);
            }
        });

    }

    @Override
    protected void onPause(){
        super.onPause();

        //If we were in the process of creating a new service but the back button was pressed to cancel then run cleanup
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String modifyMode = extras.getString("MODIFY_MODE");
        if(!successful && modifyMode.equals("create")){
            databaseServices.document(serviceId).delete();
        }

        //Detach the service DB listener because the service editor is no longer open
        serviceDBListener.remove();

    }


    public String addService(){
        //Create a new service and add it to the database
        DocumentReference serviceReference = databaseServices.document(); //Create a new empty document
        Service newService = new Service();
        newService.setId(serviceReference.getId());
        serviceReference.set(newService);
        return serviceReference.getId();
    }
    public boolean modifyService(String serviceId){
        DocumentReference serviceReference = databaseServices.document(serviceId);
        Service service = new Service();

        //Get the product information with form validation
        String serviceName = nameEditText.getText().toString().trim();
        if( TextUtils.isEmpty(serviceName)){
            nameEditText.setError("Service name required");
            return false;
        }
        double servicePrice = 0;
        try{
            servicePrice = Double.parseDouble(String.valueOf(priceEditText.getText().toString()));
        }catch(NumberFormatException e){
            priceEditText.setError("Price must be a number");
            return false;
        }

        service.setServiceElements( (ArrayList) serviceElementsAdapter.getServiceElements());
        service.setId(serviceId);
        service.setName(serviceName);
        service.setPrice(servicePrice);

        //Update DB
        serviceReference.set(service);
        return true;
    }
}