package com.jpoole.service_novigrad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class SearchBranches extends AppCompatActivity {

    //UI elements
    Spinner serviceSpinner;
    Spinner addressSpinner;
    ListView branchesList;
    BranchList branchAdapter;
    TextView searchErrorTextView;

    //Data updated from DB
    List<Branch> allBranches;
    List<Branch> selectedBranches;
    List<Service> allServices;

    //Firebase stuff
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    CollectionReference databaseBranches, databaseServices;
    ListenerRegistration branchesDBListener;
    ListenerRegistration servicesDBListener;

    //Name of selected items in spinners
    String selectedServiceId;
    String selectedBranchId;

    //Spinner options
    List<String> serviceNames;
    List<String> branchAddresses;

    //Adapters
    ArrayAdapter<String> servicesSpinnerAdapter;
    ArrayAdapter<String> branchesSpinnerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_branches);

        branchesList = (ListView) findViewById(R.id.branchesListView);
        serviceSpinner = findViewById(R.id.serviceSearchDropDown);
        addressSpinner = findViewById(R.id.branchSearchDropDown);
        searchErrorTextView = findViewById(R.id.searchErrorTextView);
        searchErrorTextView.setVisibility(View.INVISIBLE);

        //Start with nothing selected
        selectedServiceId = "";
        selectedBranchId = "";
        selectedBranches = new ArrayList<>();
        allBranches = new ArrayList<>();
        allServices = new ArrayList<>();

        //Get a database reference to the branches DB
        FirebaseApp.initializeApp(this);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        databaseBranches = fStore.collection("branches");
        databaseServices = fStore.collection("services");


        //configure services spinner
        serviceNames = new ArrayList<>();
        servicesSpinnerAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, serviceNames);
        servicesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceSpinner.setAdapter(servicesSpinnerAdapter);
        serviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedServiceId = getServiceIdFromName(serviceSpinner.getSelectedItem().toString());
                updateBranchMatches();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedServiceId = "";
                updateBranchMatches();
            }
        });


        //configure branches spinner
        branchAddresses = new ArrayList<>();
        branchesSpinnerAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, branchAddresses);
        branchesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addressSpinner.setAdapter(branchesSpinnerAdapter);
        addressSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBranchId = getBranchIdFromAddress(addressSpinner.getSelectedItem().toString());
                updateBranchMatches();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedBranchId = "";
                updateBranchMatches();
            }
        });



        //Create the adapter to display the branches in the list
        branchAdapter = new BranchList(SearchBranches.this, selectedBranches);
        branchesList.setAdapter(branchAdapter);

        //if branch is long clicked, will attempt to create a request
        branchesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String branchId = selectedBranches.get(i).getId();
                String userId = fAuth.getCurrentUser().getUid();
                String serviceId = selectedServiceId;

                Intent intent = new Intent(getApplicationContext(), CreateRequest.class);
                intent.putExtra("SERVICE_ID", serviceId);
                intent.putExtra("CUSTOMER_ID", userId);
                intent.putExtra("BRANCH_ID", branchId);
                startActivity(intent);
                finish();

                return true;
            }
        });


        //Attach a DB listener for the branches
        branchesDBListener = databaseBranches.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshot, FirebaseFirestoreException e) {
                allBranches.clear();
                for (QueryDocumentSnapshot document : snapshot) {
                    Branch branch = document.toObject(Branch.class);
                    allBranches.add(branch);
                }
                loadBranchSpinnerOptions();
                updateBranchMatches();


                /*
                branchAdapter.notifyDataSetChanged();
                if (selectedBranches.size() == 0) {
                    Toast.makeText(getApplicationContext(), "No branches meet the search criteria. Please modify.", Toast.LENGTH_LONG).show();
                }
                */

            }
        });

        //Attach a DB listener for the services
        servicesDBListener = databaseServices.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshot, FirebaseFirestoreException e) {
                allServices.clear();
                for (QueryDocumentSnapshot document : snapshot) {
                    Service service = document.toObject(Service.class);
                    allServices.add(service);
                }
                loadServiceSpinnerOptions();
                updateBranchMatches();
            }
        });
    }

    public void loadBranchSpinnerOptions(){
        branchAddresses.clear();
        branchAddresses.add("all");
        for(Branch branch : allBranches){
            branchAddresses.add(branch.getAddress());
        }
        branchesSpinnerAdapter.notifyDataSetChanged();
    }
    public void loadServiceSpinnerOptions() {
        serviceNames.clear();
        serviceNames.add("all");
        for (Service service : allServices) {
            serviceNames.add(service.getName());
        }
        servicesSpinnerAdapter.notifyDataSetChanged();
    }


    public String getBranchIdFromAddress(String branchAddress){
        for(Branch branch : allBranches){
            if(branch.getAddress().equals(branchAddress)){
                return branch.getId();
            }
        }
        return "";
    }
    public String getServiceIdFromName(String serviceName){
        for(Service service : allServices){
            if(service.getName().equals(serviceName)){
                return service.getId();
            }
        }
        return "";
    }


    private void updateBranchMatches(){
        selectedBranches.clear();
        if(selectedServiceId.equals("")){
            //Do nothing, no service selected means no options
            searchErrorTextView.setText("Service must be specified. Please modify.");
            searchErrorTextView.setVisibility(View.VISIBLE);
        }
        else if(selectedBranchId.equals("")){
            for(Branch branch : allBranches){
                if(branch.getServices().contains(selectedServiceId)){
                    selectedBranches.add(branch);
                    searchErrorTextView.setVisibility(View.INVISIBLE);
                }
            }
        }
        else{
            for(Branch branch : allBranches){
                if(branch.getId().equals(selectedBranchId)){
                    if(branch.getServices().contains(selectedServiceId)){
                        selectedBranches.add(branch);
                        searchErrorTextView.setVisibility(View.INVISIBLE);
                    }else{
                        searchErrorTextView.setText("No branches meet the above search criteria. Please modify.");
                        searchErrorTextView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        branchAdapter.notifyDataSetChanged();
    }




    @Override
    protected void onDestroy(){
        super.onDestroy();
        branchesDBListener.remove();
        servicesDBListener.remove();
    }
}