package com.jpoole.service_novigrad;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageServices extends AppCompatActivity {

    private Button buttonNewService;
    List<Service> services;
    FirebaseFirestore fStore;
    CollectionReference databaseServices;
    ListView servicesList;

    ListenerRegistration servicesDBListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_services);

        buttonNewService = (Button) findViewById(R.id.newServiceButton);
        servicesList = (ListView) findViewById(R.id.servicesListView);

        services = new ArrayList<Service>();

        //Get a database reference to the services DB
        FirebaseApp.initializeApp(this);
        fStore = FirebaseFirestore.getInstance();
        databaseServices = fStore.collection("services");

        //adding an onclicklistener to button
        buttonNewService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ModifyService.class);
                intent.putExtra("MODIFY_MODE","create");
                startActivity(intent);
            }
        });

        servicesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Service service = services.get(i);
                showContextMenu(service);
                return true;
            }
        });

        servicesDBListener = databaseServices.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshot, FirebaseFirestoreException e) {
                services.clear();

                for (QueryDocumentSnapshot document : snapshot) {
                    Service service = document.toObject(Service.class);
                    services.add(service);
                }
                //Create the adapter to display the products
                ServiceList productsAdapter = new ServiceList(ManageServices.this, services);
                servicesList.setAdapter(productsAdapter);
            }
        });


    }


    //Based on material from Lab 5
    private void showContextMenu(Service service) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.layout_service_list_context_menu, null);
        dialogBuilder.setView(dialogView);

        final TextView nameText = dialogView.findViewById(R.id.confirmText);
        final TextView priceText  = dialogView.findViewById(R.id.roleText);
        final TextView openRequestsText  = dialogView.findViewById(R.id.weekdayHoursText);
        final TextView totalRequestsText  = dialogView.findViewById(R.id.weekendHoursText);
        final TextView requiredInformationText = dialogView.findViewById(R.id.requiredInformationText);
        final Button buttonModify = dialogView.findViewById(R.id.noDeleteButton);
        final Button buttonDelete = dialogView.findViewById(R.id.yesDeleteButton);

        nameText.setText(service.getName());
        priceText.setText("Price: " + Double.toString(service.getPrice()));
        openRequestsText.setText("Number of open requests: " + Integer.toString(service.getOpenRequestsCount()));
        totalRequestsText.setText("Total number of requests: " + Integer.toString(service.getTotalRequestsCount()));

        String requiredInformation = "";
        ArrayList<ServiceElement> formElements = service.getServiceElements();
        for (int i = 0; i < formElements.size(); i++) {
            requiredInformation += formElements.get(i).getName();
            if(i != (formElements.size() - 1)){
                requiredInformation += ", ";
            }
        }
        requiredInformationText.setText("Required information: " + requiredInformation);

        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        buttonModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ModifyService.class);
                intent.putExtra("MODIFY_MODE","edit");
                intent.putExtra("SERVICE_ID", service.getId());
                startActivity(intent);

                dialog.dismiss();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(service.getOpenRequestsCount() == 0){
                    databaseServices.document(service.getId()).delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Service deleted", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(getApplicationContext(), "Failed to delete service", Toast.LENGTH_LONG).show();
                                }
                            });
                }else{
                    Toast.makeText(getApplicationContext(), "Cannot delete service that has open requests", Toast.LENGTH_LONG).show();
                }


                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        servicesDBListener.remove();
    }

}