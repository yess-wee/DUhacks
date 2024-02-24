package com.example.appelite.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appelite.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class RegistrationActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText fulln, memail, pwd, cpwd;
    Button btnregister;
    TextView btnlogin;

    FirebaseAuth fAuth;
    ProgressBar progressBar;

    FirebaseFirestore fstore;
    //String userID;

    CheckBox isFacultyBox, isStudentBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        fulln = findViewById(R.id.fullName);
        memail = findViewById(R.id.Email);
        pwd = findViewById(R.id.password);
        cpwd = findViewById(R.id.confirmPass);
        btnregister = findViewById(R.id.registerBtn);
        btnlogin = findViewById(R.id.createText);
        progressBar = findViewById(R.id.progressBar);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();


        isFacultyBox = findViewById(R.id.isTeacher);
        isStudentBox = findViewById(R.id.isStudent);

        //check box checking logic
        isStudentBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    isFacultyBox.setChecked(false);
                }
            }
        });

        isFacultyBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    isStudentBox.setChecked(false);
                }
            }
        });


        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = memail.getText().toString();
                String password = pwd.getText().toString();
                String fullname = fulln.getText().toString();
                String confirmpwd = cpwd.getText().toString();


                if(!(isFacultyBox.isChecked() || isStudentBox.isChecked())){
                    Toast.makeText(RegistrationActivity.this, "Select login type", Toast.LENGTH_SHORT).show();
                    return;
                }


                if(TextUtils.isEmpty(fullname)){
                    fulln.setError("Full name is required!");
                    return;
                }

                if(TextUtils.isEmpty(email)){
                    memail.setError("Email address is required!");
                    return;
                }


                if(TextUtils.isEmpty(password)){
                    pwd.setError("Password is required!");
                    return;
                }

                if(password.length() < 6){
                    pwd.setError("Password should be more than of six characters!");
                    return;
                }

                if(confirmpwd.isEmpty() || !password.equals(confirmpwd)){
                    cpwd.setError("Please confirm entered password!");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegistrationActivity.this, "User Created!", Toast.LENGTH_SHORT).show();
                            startActivitySecond();

                            //verification part

                            FirebaseUser fuser = fAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(RegistrationActivity.this, "Verification Email has been sent!", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Email not sent!"+ e.getMessage());
                                }
                            });

//                            //made string userID--------------------------------
                           // userID = fAuth.getCurrentUser().getUid();
                            DocumentReference df = fstore.collection("Users").document(fuser.getUid());
                            Map<String, Object> user = new HashMap<>();
                            user.put("fname",fullname);
                            user.put("email",email);

                            df.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void avoid) {
                                    if(isFacultyBox.isChecked()){
                                        user.put("isFaculty","1");
                                    }

                                    if(isStudentBox.isChecked()){
                                        user.put("isStudent","1");
                                    }

                                    df.set(user);
                                    Log.d(TAG, "onSuccess: Profile created for"+ fuser.getUid());
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: "+ e.toString());
                                }
                            });

                        }else{
                            Toast.makeText(RegistrationActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });
    }

    private void startActivitySecond(){
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}