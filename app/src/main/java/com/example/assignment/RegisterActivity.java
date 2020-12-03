package com.example.assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPass1, etPass2;
    private Button btnReg;
    private TextView tvLogin;
    private LoadingDialog loadingDialog;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                break;
            default:
                return false;
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null ){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        }

        loadingDialog = new LoadingDialog(RegisterActivity.this);
        etEmail = findViewById(R.id.regEtEmail);
        etPass1 = findViewById(R.id.regEtPass1);
        etPass2 = findViewById(R.id.regEtPass2);
        btnReg = findViewById(R.id.regBtnReg);
        tvLogin = findViewById(R.id.regTvLog);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, pass1, pass2;
                email = etEmail.getText().toString().trim();
                pass1 = etPass1.getText().toString();
                pass2 = etPass2.getText().toString();
                //validation
                if (email.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Some fields are empty!", Toast.LENGTH_SHORT).show();
                } else {
                    //validation
                    if (!isEmailValid(email)) {
                        Toast.makeText(getApplicationContext(), "Email entered is not an valid email!", Toast.LENGTH_SHORT).show();
                    } else {
                        //validation
                        if (!pass1.equals(pass2)) {
                            Toast.makeText(getApplicationContext(), "Both password are difference!", Toast.LENGTH_SHORT).show();

                        } else {
                            //validation
                            if (pass1.length() < 6 || pass2.length() < 6) {
                                Toast.makeText(getApplicationContext(), "Password length must more than 6!", Toast.LENGTH_SHORT).show();
                            } else {
                                loadingDialog.startLoadingDialog();
                                //register activity
                                register(email, pass1);
                            }
                        }
                    }
                }
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });
    }

    private void register(String email, String pass) {
        final String userEmail = email;
        //register query in firebase auth
         auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    User newUser = new User("User", userEmail, "-", "-", "-");
                    //used to create data in firebase cloud
                    db.collection("user").document(userEmail).set(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getApplicationContext(), "Registered!", Toast.LENGTH_SHORT).show();
                            btnReg.setEnabled(true);
                            loadingDialog.dismissDialog();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            btnReg.setEnabled(true);
                            loadingDialog.dismissDialog();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Email used by other people!", Toast.LENGTH_SHORT).show();
                    btnReg.setEnabled(true);
                    loadingDialog.dismissDialog();
                }
            }
        });
    }

    private boolean isEmailValid(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
