package com.jpoole.service_novigrad;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateRequest extends AppCompatActivity {

    public static final int PICK_PICTURE_REQUEST_CODE = 54321; //random
    public static final int TAKE_PICTURE_REQUEST_CODE = 54322; //random

    TextView requestTypeTextView;
    Button submitRequestButton;
    CheckBox certifyCheckBox;
    RecyclerView requestElementsRecyclerView;
    RequestElementsAdapter requestElementsAdapter;
    ConstraintLayout createRequestConstraintLayout;

    FirebaseFirestore fStore;
    CollectionReference databaseRequests;
    ListenerRegistration serviceDBListener;

    String serviceId;
    String customerId;
    String branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);

        //Configure Recycler view for request elements
        requestElementsAdapter = new RequestElementsAdapter(new ArrayList<RequestElement>(), this);
        requestElementsRecyclerView = findViewById(R.id.requestElementsRecyclerView);
        requestElementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestElementsRecyclerView.setAdapter(requestElementsAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        requestElementsRecyclerView.addItemDecoration(dividerItemDecoration);


        requestTypeTextView = findViewById(R.id.requestTypeTextView);
        submitRequestButton = findViewById(R.id.submitReqestButton);
        requestElementsRecyclerView = findViewById(R.id.requestElementsRecyclerView);
        createRequestConstraintLayout = findViewById(R.id.createRequestConstraintLayout);
        certifyCheckBox = findViewById(R.id.certifyCheckBox);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        serviceId = extras.getString("SERVICE_ID");
        customerId = extras.getString("CUSTOMER_ID");
        branchId = extras.getString("BRANCH_ID");


        //Initialize Firebase
        FirebaseApp.initializeApp(this);
        fStore = FirebaseFirestore.getInstance();
        databaseRequests = fStore.collection("requests");


        //Load in the selected service and add a listener
        DocumentReference serviceReference = fStore.collection("services").document(serviceId);
        serviceDBListener = serviceReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                //Get Service Information
                Service service = documentSnapshot.toObject(Service.class);
                requestTypeTextView.setText(service.getName() + " request");

                List<ServiceElement> serviceElements = service.getServiceElements();
                List<RequestElement> requestElements = new ArrayList<>();
                for( ServiceElement serviceElement : serviceElements){
                    RequestElement newRequestElement = new RequestElement();

                    newRequestElement.setMandatory(serviceElement.isMandatory());
                    newRequestElement.setValidationType(serviceElement.getValidationType());
                    newRequestElement.setName(serviceElement.getName());
                    newRequestElement.setType(serviceElement.getType());
                    requestElements.add(newRequestElement);
                }

                requestElementsAdapter.setRequestElements(requestElements);
                requestElementsAdapter.notifyDataSetChanged(); //Updated all items in Recycler view

            }
        });

        submitRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Request focus to insure that all RequestElement instances get updates
                createRequestConstraintLayout.requestFocus();

                if(submitRequest()){
                    finish();
                }
            }
        });
        //start the submit button in the disabled state
        setSubmitButtonState(false);
        certifyCheckBox.setChecked(false);

        //Allow the checkbox to tell when the user has navigated away on the page
        certifyCheckBox.setFocusable(true);
        certifyCheckBox.setFocusableInTouchMode(true);

        //Both clicks and focus changes should drive identical UI behaviour
        certifyCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = certifyCheckBox.isChecked();
                setSubmitButtonState(checked);
            }
        });
        certifyCheckBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                certifyCheckBox.setChecked(hasFocus);
                setSubmitButtonState(hasFocus);
            }
        });

    }


    public void setSubmitButtonState(boolean enabled){
        if(enabled) {
            submitRequestButton.getBackground().setColorFilter(null);
            submitRequestButton.setEnabled(true);
        }else{
            submitRequestButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            submitRequestButton.setEnabled(false);
        }
    }

    public boolean submitRequest(){
        ArrayList<RequestElement> requestElements = (ArrayList) requestElementsAdapter.getRequestElements();

        //Validate
        for( RequestElement requestElement : requestElements){
            if(!requestElement.validate()){
                //Validation failed, notify recycler view
                requestElementsAdapter.notifyDataSetChanged();
                return false;
            }
        }

        Request request = new Request();
        request.setName(requestTypeTextView.getText().toString());
        request.setCustomer(customerId);
        request.setBranch(branchId);
        request.setService(serviceId);
        request.setStatus(Request.OPEN);
        request.setRequestElements(requestElements);

        //Add the request
        DocumentReference requestReference = databaseRequests.document();
        String requestId = requestReference.getId();
        request.setId(requestId);
        requestReference.set(request);

        //Update the user with the new request
        DocumentReference userReference = fStore.collection("users").document(customerId);
        userReference.update("requests", FieldValue.arrayUnion(requestId));

        //Update the branch with the new request
        DocumentReference branchReference = fStore.collection("branches").document(branchId);
        branchReference.update("openRequests", FieldValue.arrayUnion(requestId));

        //Update the service number of open requests
        DocumentReference serviceReference = fStore.collection("services").document(serviceId);
        serviceReference.update("openRequestsCount", FieldValue.increment(1));
        serviceReference.update("totalRequestsCount", FieldValue.increment(1));

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == TAKE_PICTURE_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                int position = requestElementsAdapter.getLastImageSelectionPosition();
                Uri imageUri = requestElementsAdapter.getLastImageUri();
                ArrayList<RequestElement> requestElements = (ArrayList) requestElementsAdapter.getRequestElements();
                RequestElement imageElement = requestElements.get(position);

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    imageElement.assignImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                requestElementsAdapter.notifyItemChanged(position);

            }else if(resultCode == RESULT_CANCELED){

            }
        }
        else if (requestCode == PICK_PICTURE_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                int position = requestElementsAdapter.getLastImageSelectionPosition();
                ArrayList<RequestElement> requestElements = (ArrayList) requestElementsAdapter.getRequestElements();
                RequestElement imageElement = requestElements.get(position);

                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    imageElement.assignImage(bitmap);
                } catch (IOException e) {
                e.printStackTrace();
                }
                requestElementsAdapter.notifyItemChanged(position);

            }else if(resultCode == RESULT_CANCELED){

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serviceDBListener.remove();
    }
}