package com.jpoole.service_novigrad;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUsers extends AppCompatActivity {

    ListView userList;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userId;

    CollectionReference databaseUsers;
    List<User> users;

    ListenerRegistration usersDBListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_users_admin);

        FirebaseApp.initializeApp(this);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        databaseUsers = fStore.collection("users");

        userList = findViewById(R.id.userList);
        users = new ArrayList<User>();

        userList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = users.get(i);
                deleteUser(user);
                return true;
            }
        });

        usersDBListener = databaseUsers.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshot, FirebaseFirestoreException e) {
                users.clear();

                for (QueryDocumentSnapshot document : snapshot) {
                    User user = document.toObject(User.class);
                    users.add(user);
                }
                //Create the adapter to display the products
                UserList productsAdapter = new UserList(ManageUsers.this, users);
                userList.setAdapter(productsAdapter);
            }

        });
    }

    public void deleteUser(User user){
        if (user.getRoleName().equals("Customer")||user.getRoleName().equals("Employee")){
            user.delete();
            showContextMenu( user);
        }
        else{
            Toast.makeText(getApplicationContext(), "User is an Admin and cannot be deleted.", Toast.LENGTH_LONG).show();
        }

    }

    private void showContextMenu(User user) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.layout_user_list_context_menu, null);
        dialogBuilder.setView(dialogView);

        final TextView nameText = dialogView.findViewById(R.id.nameText);
        final TextView roleText  = dialogView.findViewById(R.id.roleText);
        final Button buttonNo = dialogView.findViewById(R.id.noDeleteButton);
        final Button buttonYes = dialogView.findViewById(R.id.yesDeleteButton);

        nameText.setText("Name: " + user.getFirstName()+" "+user.getLastName());
        roleText.setText("Role: " + user.getRoleName());


        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(getApplicationContext(), Delete.class);
                //startActivity(intent);

                dialog.dismiss();
            }
        });

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseUsers.document(user.getId()).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "User deleted", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getApplicationContext(), "Failed to delete user", Toast.LENGTH_LONG).show();
                            }
                        });
                dialog.dismiss();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        usersDBListener.remove();
    }
}