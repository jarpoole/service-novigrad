package com.jpoole.service_novigrad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    EditText emailText, passwordText;
    Button createAccountButton;
    Button loginButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Disable Strict mode checking to allow for file URI sharing within Intents
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());


        //UI elements (In order)
        emailText = findViewById(R.id.emailAddress);
        passwordText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        createAccountButton = findViewById(R.id.create_account_button);

        //Check for permissions
        if(!PermissionManager.checkPermissions(this)){
            Intent getPermissions = new Intent(getApplicationContext(), PermissionManager.class);
            startActivity(getPermissions);
        }

        //Firebase authentication object
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        //Add event listeners
        loginButton.setOnClickListener( (v) -> {
            String email = emailText.getText().toString().trim();
            String password = passwordText.getText().toString().trim();

            //Validate data
            if(email.equals(null) || email.equals("")){
                emailText.setError("Email is mandatory");
                return;
            }
            // Test regex with: https://regexr.com/
            if(!email.matches("^\\w+@\\w+\\.\\w+$")){ //Check email matches predefined format : ^\w{1,}@\w{1,}\.\w{1,}$
                emailText.setError("Not a valid email address");
                return;
            }
            if(password.equals(null) || password.equals("")){
                passwordText.setError("Password is mandatory");
                return;
            }

            //Authenticate the user
            fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        redirectToHomepage();
                    }else{
                        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                        if(errorCode.equals("ERROR_USER_DISABLE")){
                            emailText.setError("Account Disabled");
                        }else if(errorCode.equals("ERROR_WRONG_PASSWORD")){
                            passwordText.setError("Incorrect Password");
                            passwordText.setText("");
                        }else if(errorCode.equals("ERROR_USER_NOT_FOUND")){
                            emailText.setError("No account with this email");
                        }
                        //Useful for debug
                        //Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        createAccountButton.setOnClickListener( (v) -> {
            Intent createAccount = new Intent(getApplicationContext(), CreateAccount.class);
            startActivity(createAccount);
        });

        //If someone is already logged in then bypass the user account creation screen
        if(fAuth.getCurrentUser() != null){
            redirectToHomepage();
            finish();
        }


    }

    public void redirectToHomepage(){
        String userId = fAuth.getCurrentUser().getUid();
        DocumentReference user = fStore.collection("users").document(userId);
        user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                if(user.getRoleName().equals("Administrator")){
                    Intent intent = new Intent(getApplicationContext(), HomepageAdmin.class);
                    startActivity(intent);
                    finish(); //Finish to stop the user from being able to go back
                }else if (user.getRoleName().equals("Employee")){
                    Intent intent = new Intent(getApplicationContext(), HomepageEmployee.class);
                    startActivity(intent);
                    finish(); //Finish to stop the user from being able to go back
                }else if (user.getRoleName().equals("Customer")){
                    Intent intent = new Intent(getApplicationContext(), HomepageCustomer.class);
                    startActivity(intent);
                    finish(); //Finish to stop the user from being able to go back
                }else{
                    Toast.makeText(getApplicationContext(), "DB error: invalid user role", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}