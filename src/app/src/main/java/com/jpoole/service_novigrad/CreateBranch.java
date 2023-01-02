package com.jpoole.service_novigrad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import java.text.ParseException;
import java.util.Date;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.*;

public class CreateBranch extends AppCompatActivity {

    //UI components
    Button branchButton;
    TextView weekdayOpenTime, weekdayCloseTime, weekendOpenTime, weekendCloseTime, modifyBranchText, selectServicesText;
    EditText addressText, phoneText;
    ListView selectServices;
    ConstraintLayout constraintLayout;

    //true if the creation/modification of the branch was is successful, false if it was cancelled
    boolean successful;

    //The branch currently being edited
    String branchID;

    //Collection references for both the branches collection and services collection
    FirebaseFirestore fStore;
    CollectionReference databaseServices;
    CollectionReference databaseBranches;

    //Tracks the services which are currently offered at the branch
    ArrayList<String> branchServices;
    //Complete list of all available services
    List<Service> serviceList;

    ServiceList serviceListAdapter;
    ListenerRegistration branchDBListener;
    ListenerRegistration serviceDBListener;

    //Used to track the last UI interaction timestamp to stop accidental double-clicking
    private long uiLastClickTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_branch);
        successful = false; //set to true if employee confirms they want to create new branch

        //UI elements
        addressText = findViewById(R.id.address);
        phoneText = findViewById(R.id.phone);
        selectServices = findViewById(R.id.select_services);
        branchButton = findViewById(R.id.branchButton);
        weekdayOpenTime = findViewById(R.id.weekday_open_time);
        weekdayCloseTime = findViewById(R.id.weekday_close_time);
        weekendOpenTime = findViewById(R.id.weekend_open_time);
        weekendCloseTime = findViewById(R.id.weekend_close_time);
        constraintLayout = findViewById(R.id.constraintLayout);
        modifyBranchText = findViewById(R.id.modifyBranchText);
        selectServicesText = findViewById(R.id.selectServicesText);

        serviceList = new ArrayList<Service>();
        branchServices = new ArrayList<String>();

        //Initialize Firebase
        FirebaseApp.initializeApp(this);
        fStore = FirebaseFirestore.getInstance();
        databaseBranches = fStore.collection("branches");
        databaseServices = fStore.collection("services");


        Intent intent = getIntent(); // return intent that started this activity
        Bundle extras = intent.getExtras();
        String modifyMode = extras.getString("MODIFY_MODE");
        if(modifyMode.equals("create")){
            branchID = addBranch();
            branchButton.setText("Create Branch and Assign to Myself");
            modifyBranchText.setText("Create New Branch");
        }else if (modifyMode.equals("edit")){
            branchID = extras.getString("BRANCH_ID");
            branchButton.setText("Edit Branch");
            modifyBranchText.setText("Editing Branch");
        }

        //Initialize the listview for services and add the adapter
        serviceListAdapter = new ServiceList(CreateBranch.this, serviceList);
        serviceListAdapter.setSelectionModeEnabled(true);
        serviceListAdapter.setSelectedList(branchServices);
        selectServices.setAdapter(serviceListAdapter);

        //Load in the selected branch and add a listener
        DocumentReference branchReference = databaseBranches.document(branchID);
        branchDBListener = branchReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                Branch branch = documentSnapshot.toObject(Branch.class);

                addressText.setText(branch.getAddress());
                phoneText.setText(branch.getPhone());

                weekdayOpenTime.setText(branch.getSpecificHours(0));
                weekdayCloseTime.setText(branch.getSpecificHours(1));
                weekendOpenTime.setText(branch.getSpecificHours(2));
                weekendCloseTime.setText(branch.getSpecificHours(3));

                branchServices = (ArrayList) branch.getServices();
                serviceListAdapter.setSelectedList(branchServices);
            }
        });

        //Keep the list of services up to date
        serviceDBListener = databaseServices.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshot, FirebaseFirestoreException e) {
                serviceList.clear();
                for (QueryDocumentSnapshot document : snapshot) {
                    Service service = document.toObject(Service.class);
                    serviceList.add(service);
                }
                serviceListAdapter.notifyDataSetChanged();
            }
        });

        addTimeSelector(weekdayOpenTime);
        addTimeSelector(weekdayCloseTime);
        addTimeSelector(weekendOpenTime);
        addTimeSelector(weekendCloseTime);

        // Button to create branch object and assign to employee
        branchButton.setOnClickListener( (v) -> {
            // ensure all edit text instances update
            constraintLayout.requestFocus();

            if(modifyBranch(branchID)){
                successful = true;
                Intent returnIntent = new Intent();
                returnIntent.putExtra("BRANCH_ID", branchID);
                setResult(RESULT_OK, returnIntent);
                finish(); //Return to caller
            } else{
                //Toast.makeText(getApplicationContext(), "Invalid fields. Please try again.", Toast.LENGTH_LONG).show();
            }
        });

        // when service is clicked, add to branch
        selectServices.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String serviceId = serviceList.get(i).getId();
                TextView listItem = view.findViewById(R.id.nameTextView);
                selectServices.requestFocus();

                if (listItem.getTypeface().equals(Typeface.DEFAULT_BOLD)){
                    branchServices.remove(serviceId);
                }else{
                    branchServices.add(serviceId);
                }
                serviceListAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }


    @Override
    protected void onPause(){
        super.onPause();
        // if branch was being created and back button was pressed (need to cleanup)
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String modifyMode = extras.getString("MODIFY_MODE");
        if(!successful && modifyMode.equals("create")){
            databaseBranches.document(branchID).delete();
        }
        // detach DB listeners
        branchDBListener.remove();
        serviceDBListener.remove();

        setResult(RESULT_CANCELED);
    }

    public String addBranch(){
        // create branch and add to DB
        DocumentReference branchReference = databaseBranches.document(); // creates empty doc
        Branch newBranch = new Branch();
        newBranch.setId(branchReference.getId());

        //Default time is required to allow the time picker to work correctly, this will also speed up testing
        ArrayList<String> hours = new ArrayList<String>();
        hours.add("8:00 AM");
        hours.add("8:00 PM");
        hours.add("11:00 AM");
        hours.add("5:00 PM");
        newBranch.setHours(hours);

        branchReference.set(newBranch);
        return branchReference.getId();
    }

    public boolean modifyBranch(String branchID){
        DocumentReference branchReference = databaseBranches.document(branchID);
        Branch branch = new Branch();

        // Retrieve all branch info
        String address = addressText.getText().toString();
        String phone = phoneText.getText().toString();
        ArrayList<String> hours = new ArrayList();
        hours.add(weekdayOpenTime.getText().toString());  // Week open hours
        hours.add(weekdayCloseTime.getText().toString()); // Week close hours
        hours.add(weekendOpenTime.getText().toString());  // Weekend open hours
        hours.add(weekdayCloseTime.getText().toString()); // Weekend close hours

        //Validate address field
        if(address.equals(null) || address.equals("")){   // Check something has been entered for address
            addressText.setError("Address must be provided");
            return false;
        }
        else if(!address.matches(ValidationManager.getAddressRegex())){
            addressText.setError(ValidationManager.getAddressRegexError());
            return false;
        }

        //Validate phone number field
        if(phone.equals(null) || phone.equals("")){ //Check something has been entered for phone
            phoneText.setError("Phone number must be provided");
            return false;
        }
        else if(!phone.matches(ValidationManager.getPhoneNumberRegex())){
            phoneText.setError(ValidationManager.getPhoneNumberRegexError());
            return false;
        }

        //Validate working hours
        if ((hours.get(0).equals(null) || hours.get(0).equals("")) && (hours.get(1).equals(null) || hours.get(1).equals(""))){
            Toast.makeText(this, "Branch will be set to closed on weekdays.", Toast.LENGTH_SHORT).show();
            return false;
        } else if ((hours.get(2).equals(null) || hours.get(2).equals("")) && (hours.get(3).equals(null) || hours.get(3).equals(""))){
            Toast.makeText(this, "Branch will be set to closed on weekends.", Toast.LENGTH_SHORT).show();
            return false;
        } else if ((hours.get(0).equals(null) || hours.get(0).equals("")) || (hours.get(1).equals(null) || hours.get(1).equals(""))){
            Toast.makeText(this, "Invalid weekday hours.", Toast.LENGTH_SHORT).show();
            return false;
        } else if((hours.get(2).equals(null) || hours.get(2).equals("")) && (hours.get(3).equals(null) || hours.get(3).equals(""))){
            Toast.makeText(this, "Invalid weekend hours.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (branchServices.size() < 1) {
            selectServicesText.setError("Must select at least 1 service to be provided");
            selectServicesText.requestFocus();
            return false;
        }

        //Add all data to new branch
        branch.setAddress(address);
        branch.setPhone(phone);
        branch.setHours(hours);
        branch.setId(branchID);
        //Add all selected services to branch
        for (String serviceId : branchServices){
            branch.addService(serviceId);
        }
        branchReference.set(branch);
        return true;
    }

    //time selection
    //source: https://www.youtube.com/watch?v=o-HVE_VxyjQ

    private void addTimeSelector(TextView timeView){
        timeView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - uiLastClickTime < 1000) {return;}
                uiLastClickTime = SystemClock.elapsedRealtime();

                int currentSelectedHour = 0;
                int currentSelectedMinute = 0;

                //Parse the current time from the textField
                try{
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat onlyHours = new SimpleDateFormat("HH");
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat onlyMinutes = new SimpleDateFormat("mm");
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat expectedFormat = new SimpleDateFormat("hh:mm aa");
                    String timeString = timeView.getText().toString();
                    Date date = expectedFormat.parse(timeString);
                    currentSelectedHour = Integer.parseInt(onlyHours.format(date));
                    currentSelectedMinute = Integer.parseInt(onlyMinutes.format(date));
                } catch (ParseException e) {
                    //e.printStackTrace();
                }

                // Initialize time picker dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(CreateBranch.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            // Store hour and minute in string
                            String timeSet = hourOfDay + ":" + minute;
                            // Initialize 24 hour time format
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat f24Hour = new SimpleDateFormat("HH:mm");
                            try {
                                Date date = f24Hour.parse(timeSet);
                                // Initialize 12 hour time format
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat f12Hour = new SimpleDateFormat("hh:mm aa");
                                // Set selected time on text view
                                timeView.setText(f12Hour.format(date));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }, currentSelectedHour, currentSelectedMinute,false
                );
                // Set transparent background
                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                // Display previous selected time
                timePickerDialog.updateTime(currentSelectedHour, currentSelectedMinute);
                // Show dialog
                timePickerDialog.show();
            }
        });
    }

}