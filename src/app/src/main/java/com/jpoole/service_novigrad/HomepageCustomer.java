package com.jpoole.service_novigrad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class HomepageCustomer extends AppCompatActivity {

    Button logoutButton, searchButton;
    TextView emailAddress, fullName, accountType, welcomeText;
    RecyclerView requestRatingRecyclerView;
    RequestRatingAdapter requestRatingAdapter;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;

    ListenerRegistration customerDBListener;
    ListenerRegistration requestsDBListener;

    Customer loggedInCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_customer);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        logoutButton = findViewById(R.id.logout);
        fullName = findViewById(R.id.fullName);
        emailAddress = findViewById(R.id.emailAddress);
        accountType = findViewById(R.id.accountType);
        welcomeText = findViewById(R.id.welcome);
        searchButton = findViewById(R.id.searchButton);

        //Configure rating recycler view
        requestRatingAdapter = new RequestRatingAdapter(new ArrayList<Request>(), this);
        requestRatingRecyclerView = findViewById(R.id.customerRequestsRecyclerView);
        requestRatingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestRatingRecyclerView.setAdapter(requestRatingAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        requestRatingRecyclerView.addItemDecoration(dividerItemDecoration);


        logoutButton.setOnClickListener(this::logout);

        userId = fAuth.getCurrentUser().getUid();
        DocumentReference userDocumentReference = fStore.collection("users").document(userId);
        customerDBListener = userDocumentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                loggedInCustomer = documentSnapshot.toObject(Customer.class);
                fullName.setText("Full Name: " + loggedInCustomer.getFirstName() + " " + loggedInCustomer.getLastName());
                emailAddress.setText("Email: " + loggedInCustomer.getEmail());
                accountType.setText("Role: " + loggedInCustomer.getRoleName());
                welcomeText.setText("Welcome " + loggedInCustomer.getFirstName());


                //Remove the previous requests listener
                if(requestsDBListener != null) {
                    requestsDBListener.remove();
                }
                //Attach a new requests listener
                ArrayList<String> requestIds = (ArrayList) loggedInCustomer.getRequests();
                if(requestIds.size() > 0){ //Firebase "whereIn" query crashes if array is empty
                    requestsDBListener = fStore.collection("requests").whereIn(FieldPath.documentId(), requestIds).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            ArrayList<Request> requests = (ArrayList) requestRatingAdapter.getRequests();
                            requests.clear();
                            for(DocumentSnapshot documentSnapshot : value.getDocuments()) {
                                Request request = documentSnapshot.toObject(Request.class);
                                requests.add(request);
                            }
                            requestRatingAdapter.notifyDataSetChanged();
                        }
                    });
                }

            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchBranches.class);
                startActivity(intent);
            }
        });

    }

    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        //startActivity(intent);

        customerDBListener.remove();

        //Remove the previous requests listener
        if(requestsDBListener != null) {
            requestsDBListener.remove();
        }

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

}