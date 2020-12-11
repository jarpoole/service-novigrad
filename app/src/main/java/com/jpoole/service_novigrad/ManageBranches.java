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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

public class ManageBranches extends AppCompatActivity {

    public static final int CREATE_BRANCH_REQUEST_CODE = 54321;

    //Return codes
    public static final int NEW_BRANCH_SELECTED = 50001;
    public static final int NO_CHANGE_SELECTED = 50002;
    public static final int NO_BRANCH_SELECTED = 50003;
    //There are 4 ways to exit this activity
    //    - Select a branch by long pressing and then click assign                                   -> NEW_BRANCH_SELECTED
    //    - Select create new branch and then complete the creation process                          -> NEW_BRANCH_SELECTED
    //    - Press the back button to leave the activity after deleting the currently selected branch -> NO_BRANCH_SELECTED
    //    - Press the back button to leave the activity after having changed nothing                 -> NO_CHANGE_SELECTED

    private Button buttonNewBranch;
    List<Branch> branches;
    FirebaseFirestore fStore;
    CollectionReference databaseBranches;
    ListView branchesList;
    BranchList branchAdapter;

    String selectedBranchID;

    ListenerRegistration branchesDBListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_branches);

        buttonNewBranch = (Button) findViewById(R.id.newBranchButton);
        branchesList = (ListView) findViewById(R.id.branchesListView);

        branches = new ArrayList<Branch>();

        //Get a database reference to the branches DB
        FirebaseApp.initializeApp(this);
        fStore = FirebaseFirestore.getInstance();
        databaseBranches = fStore.collection("branches");


        Intent intent = getIntent(); //intent that started this activity
        Bundle extras = intent.getExtras();
        selectedBranchID = extras.getString("PREVIOUS_BRANCH_ID");


        //Create the adapter to display the branches in the list
        branchAdapter = new BranchList(ManageBranches.this, branches);
        branchesList.setAdapter(branchAdapter);

        //adding an onclicklistener to button
        buttonNewBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CreateBranch.class);
                intent.putExtra("MODIFY_MODE","create");
                startActivityForResult(intent, CREATE_BRANCH_REQUEST_CODE);
            }
        });

        //if branch is long clicked, will assign it to employee
        branchesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Branch branch = branches.get(i);
                showContextMenu(branch);
                return true;
            }
        });

        branchesDBListener = databaseBranches.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshot, FirebaseFirestoreException e) {
                branches.clear();
                for (QueryDocumentSnapshot document : snapshot) {
                    Branch branch = document.toObject(Branch.class);
                    branches.add(branch);
                }
                branchAdapter.notifyDataSetChanged();
            }
        });

        //Set default return state
        setResult(NO_CHANGE_SELECTED);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == CREATE_BRANCH_REQUEST_CODE  && resultCode  == RESULT_OK) {
                //Pass the info back to the employee homepage
                setResult(NEW_BRANCH_SELECTED, data);
                finish();
            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    //Based on material from Lab 5
    private void showContextMenu(Branch branch) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.layout_branch_list_context_menu, null);
        dialogBuilder.setView(dialogView);

        final TextView addressText = dialogView.findViewById(R.id.addressText);
        final TextView phoneText = dialogView.findViewById(R.id.phoneText);
        final TextView weekdayHoursText = dialogView.findViewById(R.id.weekdayHoursText);
        final TextView weekendHoursText = dialogView.findViewById(R.id.weekendHoursText);
        final Button buttonAssign = dialogView.findViewById(R.id.assignButton);
        final Button buttonDelete = dialogView.findViewById(R.id.deleteButton);

        //dialogBuilder.setTitle(service.getName());
        addressText.setText("Address: " + branch.getAddress());
        phoneText.setText("Phone: " + branch.getPhone());
        weekdayHoursText.setText("Mon-Fri: " + branch.getSpecificHours(0)+"-"+branch.getSpecificHours(1));
        weekendHoursText.setText("Sat-Sun: " + branch.getSpecificHours(2)+"-"+branch.getSpecificHours(3));

        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        buttonAssign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("BRANCH_ID", branch.getId());
                setResult(NEW_BRANCH_SELECTED, returnIntent);
                selectedBranchID = branch.getId();
                dialog.dismiss();
                finish();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(branch.getId().equals(selectedBranchID)){
                    selectedBranchID = null;
                    setResult(NO_BRANCH_SELECTED);
                }
                databaseBranches.document(branch.getId()).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "Branch deleted", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getApplicationContext(), "Failed to delete branch", Toast.LENGTH_LONG).show();
                            }
                        });
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        branchesDBListener.remove();
    }
}