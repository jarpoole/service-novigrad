package com.jpoole.service_novigrad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

public class HomepageAdmin extends AppCompatActivity {

    Button logoutButton, manageServicesButton, manageUsersButton;
    TextView emailAddress, fullName, accountType, welcomeText;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;

    ListenerRegistration adminDBListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_admin);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        logoutButton = findViewById(R.id.logout);
        manageServicesButton = findViewById(R.id.manageServicesButton);
        manageUsersButton = findViewById(R.id.manageUsersButton);
        fullName = findViewById(R.id.fullName);
        emailAddress = findViewById(R.id.emailAddress);
        accountType = findViewById(R.id.accountType);
        welcomeText = findViewById(R.id.welcome);

        logoutButton.setOnClickListener(this::logout);

        userId = fAuth.getCurrentUser().getUid();
        DocumentReference userDocumentReference = fStore.collection("users").document(userId);
        adminDBListener = userDocumentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                User loggedInUser = documentSnapshot.toObject(User.class);
                fullName.setText("Full Name: " + loggedInUser.getFirstName() + " " + loggedInUser.getLastName());
                emailAddress.setText("Email: " + loggedInUser.getEmail());
                accountType.setText("Role: " + loggedInUser.getRoleName());
                welcomeText.setText("Welcome " + loggedInUser.getFirstName());
            }
        });

        manageServicesButton.setOnClickListener( (v) -> {
            Intent manageServices = new Intent(getApplicationContext(), ManageServices.class);
            startActivity(manageServices);
        });

        manageUsersButton.setOnClickListener( (v) -> {
            Intent manageServices = new Intent(getApplicationContext(), ManageUsers.class);
            startActivity(manageServices);
        });

    }

    public void logout(View view){
        adminDBListener.remove();

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

}