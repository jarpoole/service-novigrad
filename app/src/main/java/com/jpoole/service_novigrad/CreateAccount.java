package com.jpoole.service_novigrad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateAccount extends AppCompatActivity {

    EditText firstNameText, lastNameText, emailText, passwordText, passwordCheckText;
    Button createAccountButton;
    RadioButton userTypeCustomer;
    RadioButton userTypeEmployee;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    ProgressBar progressBar;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //UI elements (In order)
        firstNameText = findViewById(R.id.first_name);
        lastNameText = findViewById(R.id.last_name);
        emailText = findViewById(R.id.emailAddress);
        passwordText = findViewById(R.id.password);
        passwordCheckText = findViewById(R.id.passwordCheck);
        userTypeCustomer = findViewById(R.id.userTypeCustomer);
        userTypeEmployee = findViewById(R.id.userTypeEmployee);
        createAccountButton = findViewById(R.id.create_account);
        progressBar = findViewById(R.id.progress_bar);

        //Firebase objects
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        createAccountButton.setOnClickListener( (v) -> {
            String email = emailText.getText().toString().trim();
            String password = passwordText.getText().toString().trim();
            String passwordCheck = passwordCheckText.getText().toString().trim();
            String firstName = firstNameText.getText().toString();
            String lastName = lastNameText.getText().toString();


            //Validate name fields
            if(firstName.equals(null) || firstName.equals("")){ //Check something has been entered for first name
                firstNameText.setError("First name is mandatory");
                return;
            }
            else if(!firstName.matches(ValidationManager.getNameRegex())){ //Check that first name is only composed of alphabetic characters plus hyphen and comma
                firstNameText.setError(ValidationManager.getNameRegexError());
                return;
            }
            if(lastName.equals(null) || lastName.equals("")){ //Check something has been entered for last name
                lastNameText.setError("Last name is mandatory");
                return;
            }
            else if(!lastName.matches(ValidationManager.getNameRegex())){ //Check that last name is only composed of alphabetic characters plus hyphen and comma
                lastNameText.setError(ValidationManager.getNameRegexError());
                return;
            }

            //Validate email field
            if(email.equals(null) || email.equals("")){ //Check something has been entered for email
                emailText.setError("Email is mandatory");
                return;
            }
            else if(!email.matches(ValidationManager.getEmailRegex())){ //Check email matches predefined format : ^\w{1,}@\w{1,}\.\w{1,}$
                emailText.setError(ValidationManager.getEmailRegexError());
                return;
            }

            //Validate password fields
            if(password.equals(null) || password.equals("")){
                passwordText.setError("Password is mandatory");
                return;
            }
            else if(!email.matches(ValidationManager.getPasswordRegex())){
                passwordText.setError(ValidationManager.getPasswordRegexError());
                return;
            }
            if(passwordCheck.equals(null) || passwordCheck.equals("")){
                passwordCheckText.setError("Please confirm password");
                return;
            }
            else if(!passwordCheck.equals(password)){
                passwordCheckText.setError("Passwords much match");
                passwordText.setText("");
                passwordCheckText.setText("");
                return;
            }

            //Alert the user that the registration request has now started
            progressBar.setVisibility(View.VISIBLE);

            fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        userId = fAuth.getCurrentUser().getUid();
                        DocumentReference newDocument = fStore.collection("users").document(userId);
                        User newUser;
                        Class nextPage;

                        if(userTypeCustomer.isChecked()){
                            newUser = new Customer();
                            nextPage = HomepageCustomer.class;
                        }else if(userTypeEmployee.isChecked()){
                            newUser = new Employee();
                            nextPage = HomepageEmployee.class;
                        }else{
                            return; //Shouldn't get here. Something went wrong
                        }
                        newUser.setName(firstName, lastName);
                        newUser.setEmail(email);
                        newUser.setId(userId);
                        newDocument.set(newUser);
                        startActivity(new Intent(getApplicationContext(), nextPage));
                        finish();

                    }else{
                        //All exception codes found here: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuthException
                        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                        if(errorCode.equals("ERROR_EMAIL_ALREADY_IN_USE")){
                            emailText.setError("Account with this email already exists");
                        }
                        progressBar.setVisibility(View.INVISIBLE);

                        // Some of these still need to be implemented
                        /*
                        ("ERROR_INVALID_CUSTOM_TOKEN", "The custom token format is incorrect. Please check the documentation."));
                        ("ERROR_CUSTOM_TOKEN_MISMATCH", "The custom token corresponds to a different audience."));
                        ("ERROR_INVALID_CREDENTIAL", "The supplied auth credential is malformed or has expired."));
                        ("ERROR_INVALID_EMAIL", "The email address is badly formatted."));
                        ("ERROR_WRONG_PASSWORD", "The password is invalid or the user does not have a password."));
                        ("ERROR_USER_MISMATCH", "The supplied credentials do not correspond to the previously signed in user."));
                        ("ERROR_REQUIRES_RECENT_LOGIN", "This operation is sensitive and requires recent authentication. Log in again before retrying this request."));
                        ("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL", "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address."));
                        ("ERROR_EMAIL_ALREADY_IN_USE", "The email address is already in use by another account."));
                        ("ERROR_CREDENTIAL_ALREADY_IN_USE", "This credential is already associated with a different user account."));
                        ("ERROR_USER_DISABLED", "The user account has been disabled by an administrator."));
                        ("ERROR_USER_TOKEN_EXPIRED", "The user\'s credential is no longer valid. The user must sign in again."));
                        ("ERROR_USER_NOT_FOUND", "There is no user record corresponding to this identifier. The user may have been deleted."));
                        ("ERROR_INVALID_USER_TOKEN", "The user\'s credential is no longer valid. The user must sign in again."));
                        ("ERROR_OPERATION_NOT_ALLOWED", "This operation is not allowed. You must enable this service in the console."));
                        ("ERROR_WEAK_PASSWORD", "The given password is invalid."));
                         */

                        //Toast.makeText(CreateAccount.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });



        });

    }

}