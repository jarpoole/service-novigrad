package com.jpoole.service_novigrad;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageRequests extends AppCompatActivity {

    List<Request> requests;
    FirebaseFirestore fStore;
    CollectionReference databaseRequests;
    ListView requestsList;
    RequestList requestAdapter;

    String branchId;
    Branch branch;

    ListenerRegistration requestsDBListener;
    ListenerRegistration branchDBListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_requests);

        requestsList = (ListView) findViewById(R.id.requestsListView);

        requests = new ArrayList<Request>();

        //Get a database reference to the requests DB
        FirebaseApp.initializeApp(this);
        fStore = FirebaseFirestore.getInstance();
        databaseRequests = fStore.collection("requests");


        Intent intent = getIntent(); //intent that started this activity
        Bundle extras = intent.getExtras();
        branchId = extras.getString("BRANCH_ID");


        //Create the adapter to display the requests in the list
        requestAdapter = new RequestList(ManageRequests.this, requests);
        requestsList.setAdapter(requestAdapter);


        //if request is long clicked employee can approve or reject it
        requestsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Request request = requests.get(i);
                showContextMenu(request);
                return true;
            }
        });

        DocumentReference branchDocumentReference = fStore.collection("branches").document(branchId);
        branchDBListener = branchDocumentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                branch = documentSnapshot.toObject(Branch.class);

                //Remove the previous requests listener
                if(requestsDBListener != null) {
                    requestsDBListener.remove();
                }
                //Attach a new requests listener
                ArrayList<String> requestIds = (ArrayList) branch.getOpenRequests();
                if(requestIds.size() > 0){ //Firebase "whereIn" query crashes if array is empty
                    requestsDBListener = fStore.collection("requests").whereIn(FieldPath.documentId(), requestIds).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            //ArrayList<Request> requests = (ArrayList) requestRatingAdapter.getRequests();
                            requests.clear();
                            for(DocumentSnapshot documentSnapshot : value.getDocuments()) {
                                Request request = documentSnapshot.toObject(Request.class);
                                requests.add(request);
                            }
                            requestAdapter.notifyDataSetChanged();
                        }
                    });
                }else{
                    requests.clear();
                    requestAdapter.notifyDataSetChanged();
                }
            }
        });


    }


    //Based on material from Lab 5
    private void showContextMenu(Request request) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.layout_request_list_context_menu, null);
        dialogBuilder.setView(dialogView);

        final TextView confirmationText = dialogView.findViewById(R.id.confirmationText);
        final TextView requestText = dialogView.findViewById(R.id.requestText);
        final TextView customerText = dialogView.findViewById(R.id.customerText);
        final TextView serviceText = dialogView.findViewById(R.id.serviceText);
        final Button yesButton = dialogView.findViewById(R.id.yesButton);
        final Button noButton = dialogView.findViewById(R.id.noButton);

        //dialogBuilder.setTitle(service.getName());
        requestText.setText("Request Id: " + request.getId());
        customerText.setText("Customer Id: " + request.getCustomer());
        serviceText.setText("Associated Service Id: " + request.getService());

        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent processRequest = new Intent(getApplicationContext(),ProcessRequest.class);
                processRequest.putExtra("REQUEST_ID", request.getId());
                processRequest.putExtra("BRANCH_ID", branchId);
                startActivity(processRequest);
                dialog.dismiss();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        branchDBListener.remove();

        //Remove the previous requests listener
        if(requestsDBListener != null) {
            requestsDBListener.remove();
        }
    }
}