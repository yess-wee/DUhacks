package com.example.appelite.auth;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appelite.Admin.Admin;
import com.example.appelite.MainActivity;
import com.example.appelite.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    boolean valid = true;

    EditText memail, pwd;
    Button btnLogin;
    TextView createBtn, foregetTxtLink;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        memail = findViewById(R.id.Email);
        pwd = findViewById(R.id.password);
        createBtn = findViewById(R.id.createText);
        btnLogin = findViewById(R.id.loginBtn);
        foregetTxtLink = findViewById(R.id.forgotPassword);
        progressBar = findViewById(R.id.progressBar);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkField(memail);
                checkField(pwd);

                Log.d("TAG","onClick"+memail.getText().toString());

                if(valid){

                    fAuth.signInWithEmailAndPassword(memail.getText().toString(), pwd.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(LoginActivity.this, "Logged in!", Toast.LENGTH_SHORT).show();
                            checkUserAcessLevel(authResult.getUser().getUid());
                            progressBar.setVisibility(View.GONE);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }

//                String email = memail.getText().toString();
//                String password = pwd.getText().toString();
//
//                if(TextUtils.isEmpty(email)){
//                    memail.setError("Email address is required!");
//                    return;
//                }
//
//
//                if(TextUtils.isEmpty(password)){
//                    pwd.setError("Password is required!");
//                    return;
//                }
//
//                if(password.length() < 6){
//                    pwd.setError("Password should be more than of six characters!");
//                    return;
//                }
//                progressBar.setVisibility(View.VISIBLE);
//
//                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if(task.isSuccessful()){
//                            //verification part
//                            FirebaseUser fuser = fAuth.getCurrentUser();
//                            assert fuser != null;
//
//                            if(fuser.isEmailVerified()){
//                                progressBar.setVisibility(View.GONE);
//                                startActivitySecond();
//                                finish();
//
//                            }
//                            else {
//                                Toast.makeText(LoginActivity.this, "Please verify your email!", Toast.LENGTH_SHORT).show();
//                                progressBar.setVisibility(View.GONE);
//                            }
//
//                        }else{
//                            Toast.makeText(LoginActivity.this, "Error Occured!", Toast.LENGTH_SHORT).show();
//                            progressBar.setVisibility(View.GONE);
//                        }
//                    }
//                });
            }
        });


        foregetTxtLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText resetMail = new EditText(v.getContext());
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password ?");
                passwordResetDialog.setMessage("Enter Your Email To Received Reset Link.");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // extract the email and send reset link
                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this, "Reset Link Sent To Your Email.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Error ! Reset Link is Not Sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });

                passwordResetDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close the dialog
                    }
                });

                passwordResetDialog.create().show();

            }
        });



        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
            }
        });
    }

//    private void startActivitySecond(){
//        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//    }

    private void checkUserAcessLevel(String uid){
        DocumentReference df = fStore.collection("Users").document(uid);
        //exctraction of the data from the doc
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("TAG","onSuccess: "+ documentSnapshot.getData());
                //user access level identification

                if(documentSnapshot.getString("isFaculty") != null){
                    startActivity(new Intent(getApplicationContext(), Admin.class));
                    finish();
                }

                if(documentSnapshot.getString("isStudent") != null){
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        });
    }

    public boolean checkField(EditText textField){
        if(textField.getText().toString().isEmpty()){
            textField.setError("Error");
            valid = false;
        }else {
            valid = true;
        }

        return valid;
    }

}