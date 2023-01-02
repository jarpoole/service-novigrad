package com.jpoole.service_novigrad;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ProcessRequest extends AppCompatActivity {

    TextView requestDisplayNameTextView, customerNameTextView, customerEmailTextView;
    Button approveRequestButton, denyRequestButton;
    RecyclerView requestDisplayRecyclerView;
    RequestDisplayAdapter requestDisplayAdapter;

    String requestId;
    String branchId;
    String serviceId;

    FirebaseFirestore fStore;
    CollectionReference databaseRequests;
    ListenerRegistration requestDBListener;
    ListenerRegistration customerDBListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_request);

        approveRequestButton = findViewById(R.id.approveRequestButton);
        denyRequestButton = findViewById(R.id.denyRequestButton);
        requestDisplayNameTextView = findViewById(R.id.requestDisplayNameTextView);
        customerNameTextView = findViewById(R.id.customerNameTextView);
        customerEmailTextView = findViewById(R.id.customerEmailTextView);

        //Configure Recycler view for request elements
        requestDisplayAdapter = new RequestDisplayAdapter(new ArrayList<RequestElement>());
        requestDisplayRecyclerView = findViewById(R.id.requestDisplayRecyclerView);
        requestDisplayRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestDisplayRecyclerView.setAdapter(requestDisplayAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        requestDisplayRecyclerView.addItemDecoration(dividerItemDecoration);

        //Initialize Firebase
        FirebaseApp.initializeApp(this);
        fStore = FirebaseFirestore.getInstance();
        databaseRequests = fStore.collection("requests");

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        requestId = extras.getString("REQUEST_ID");
        branchId = extras.getString("BRANCH_ID");


        //Load in the selected request and add a listener
        DocumentReference requestReference = databaseRequests.document(requestId);
        requestDBListener = requestReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                //Get request Information
                Request request = documentSnapshot.toObject(Request.class);
                serviceId = request.getService();

                requestDisplayNameTextView.setText(request.getName());

                List<RequestElement> requestDisplayElements = request.getRequestElements();

                requestDisplayAdapter.setRequestDisplayElements(requestDisplayElements);
                requestDisplayAdapter.notifyDataSetChanged(); //Updated all items in Recycler view

                //Remove the previous customer listener
                if(customerDBListener != null) {
                    customerDBListener.remove();
                }
                String customerId = request.getCustomer();
                DocumentReference customerDocumentReference = fStore.collection("users").document(customerId);
                customerDBListener = customerDocumentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                        Customer customer = documentSnapshot.toObject(Customer.class);
                        customerNameTextView.setText("Customer: " + customer.getFirstName() + " " + customer.getLastName());
                        customerEmailTextView.setText("Email: " + customer.getEmail());
                    }
                });


            }
        });

        approveRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRequestStatus(Request.APPROVED);
                finish();
            }
        });
        denyRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRequestStatus(Request.DENIED);
                finish();
            }
        });

    }

    public void updateRequestStatus(String status){
        DocumentReference requestReference = databaseRequests.document(requestId);
        requestReference.update("status", status);

        //Update branch
        DocumentReference branchReference = fStore.collection("branches").document(branchId);
        branchReference.update("openRequests", FieldValue.arrayRemove(requestId));

        //Update the service number of open requests
        DocumentReference serviceReference = fStore.collection("services").document(serviceId);
        branchReference.update("openRequestsCount", FieldValue.increment(-1));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        requestDBListener.remove();
        if(customerDBListener != null) {
            customerDBListener.remove();
        }
    }
}