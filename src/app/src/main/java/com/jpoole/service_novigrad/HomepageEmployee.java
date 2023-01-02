package com.jpoole.service_novigrad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.atomic.AtomicReference;

public class HomepageEmployee extends AppCompatActivity {

    //Used to identify an when a result is returned to this activity from the ManageBranches activity
    public static final int SELECT_BRANCH_REQUEST_CODE = 12345;
    public static final int EDIT_BRANCH_REQUEST_CODE = 12346;

    Button logoutButton, employeeBranchButton, editBranchButton, requestsButton;
    TextView emailAddress, fullName, accountType, welcomeText, employeeBranchStatus;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;

    //Using atomic variable to allow DB to modify asynchronously
    AtomicReference<Employee> loggedInEmployee = new  AtomicReference<Employee>();

    DocumentReference employerDocumentReference;

    ListenerRegistration employeeDBListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_employee);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        logoutButton = findViewById(R.id.logout);
        employeeBranchButton = findViewById(R.id.employeeBranchButton);
        editBranchButton = findViewById(R.id.editBranchButton);
        requestsButton = findViewById(R.id.requestsButton);
        fullName = findViewById(R.id.fullName);
        emailAddress = findViewById(R.id.emailAddress);
        accountType = findViewById(R.id.accountType);
        welcomeText = findViewById(R.id.welcome);
        //searchButton = findViewById(R.id.searchButton);
        employeeBranchStatus = findViewById(R.id.employeeBranchStatus);
        requestsButton = findViewById(R.id.requestsButton);

        logoutButton.setOnClickListener(this::logout);

        userId = fAuth.getCurrentUser().getUid();
        employerDocumentReference = fStore.collection("users").document(userId);
        employeeDBListener = employerDocumentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                loggedInEmployee.set(snapshot.toObject(Employee.class));
                fullName.setText("Full Name: " + loggedInEmployee.get().getFirstName() + " " + loggedInEmployee.get().getLastName());
                emailAddress.setText("Email: " + loggedInEmployee.get().getEmail());
                accountType.setText("Role: " + loggedInEmployee.get().getRoleName());
                welcomeText.setText("Welcome " + loggedInEmployee.get().getFirstName());

                if (loggedInEmployee.get().getBranch() == null) {
                    employeeBranchStatus.setText("You are not currently assigned a branch");
                    employeeBranchButton.setText("Create or choose existing branch");
                    editBranchButton.setVisibility(View.INVISIBLE);
                    requestsButton.setVisibility(View.INVISIBLE);
                } else {
                    employeeBranchStatus.setText("Assigned branch with ID: " + loggedInEmployee.get().getBranch());
                    employeeBranchButton.setText("Change my branch");
                    editBranchButton.setVisibility(View.VISIBLE);
                    editBranchButton.setText("Edit my branch");
                    requestsButton.setVisibility(View.VISIBLE);
                    requestsButton.setText("View service requests");
                }
            }
        });

        employeeBranchButton.setOnClickListener( (v) -> {
            Intent manageBranches = new Intent(getApplicationContext(), ManageBranches.class);
            manageBranches.putExtra("PREVIOUS_BRANCH_ID", loggedInEmployee.get().getBranch());
            startActivityForResult(manageBranches, SELECT_BRANCH_REQUEST_CODE);
        });

        //Only way for this button to be clicked is if the employee currently has a branch assigned
        editBranchButton.setOnClickListener( (v -> {
            Intent editBranch = new Intent(getApplicationContext(),CreateBranch.class);
            editBranch.putExtra("MODIFY_MODE", "edit");
            editBranch.putExtra("BRANCH_ID", loggedInEmployee.get().getBranch());
            startActivityForResult(editBranch, EDIT_BRANCH_REQUEST_CODE);
        }));

        requestsButton.setOnClickListener( (v -> {
            Intent handleRequests = new Intent(getApplicationContext(),ManageRequests.class);
            handleRequests.putExtra("BRANCH_ID", loggedInEmployee.get().getBranch());
            startActivity(handleRequests);
        }));
    }

    public void logout(View view){
        employeeDBListener.remove();

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            //For branch selection
            if (requestCode == SELECT_BRANCH_REQUEST_CODE){
                if(resultCode  == ManageBranches.NEW_BRANCH_SELECTED) {
                    //Update the employee in DB with new branch
                    loggedInEmployee.get().setBranch(data.getStringExtra("BRANCH_ID"));
                    fStore.collection("users").document(userId).set(loggedInEmployee.get());
                    Toast.makeText(this, "Successfully selected branch", Toast.LENGTH_SHORT).show();
                }
                if(resultCode  == ManageBranches.NO_BRANCH_SELECTED) {
                    loggedInEmployee.get().setBranch(null);
                    fStore.collection("users").document(userId).set(loggedInEmployee.get());
                    Toast.makeText(this, "Previously selected branch was deleted", Toast.LENGTH_SHORT).show();
                }
                if(resultCode == ManageBranches.NO_CHANGE_SELECTED) {
                    Toast.makeText(this, "No changes made to selection", Toast.LENGTH_SHORT).show();
                }
            }

            //For branch editing
            else if (requestCode == EDIT_BRANCH_REQUEST_CODE  && resultCode  == RESULT_OK) {
                Toast.makeText(this, "Successfully edited branch", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }


}
