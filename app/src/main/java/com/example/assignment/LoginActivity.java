package com.example.assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassord;
    private Button btnLog;
    private TextView tvReg, tvFP;
    private FirebaseAuth auth;
    private LoadingDialog loadingDialog;
    private ForgetPasswordDialog fpDialog;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
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
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        }
        fpDialog = new ForgetPasswordDialog(LoginActivity.this);
        loadingDialog = new LoadingDialog(LoginActivity.this);
        etEmail = findViewById(R.id.logEtEmail);
        etPassord = findViewById(R.id.logEtPass);
        btnLog = findViewById(R.id.logBtnLogin);
        tvReg = findViewById(R.id.logTvRegister);
        tvFP = findViewById(R.id.logTvForgetPass);
        auth = FirebaseAuth.getInstance();

        //validation
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Toast.makeText(getApplicationContext(), "You already login!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        //validation before login process
        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;
                email = etEmail.getText().toString().trim();
                password = etPassord.getText().toString();
                //validation
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Email or Password field is empty!", Toast.LENGTH_SHORT).show();
                } else {
                    loadingDialog.startLoadingDialog();
                    login(email, password);
                }
            }
        });

        tvReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                finish();
            }
        });

        tvFP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fpDialog.callDialog();
            }
        });
    }

    //login function
    private void login(String email, String pass) {
        //login query in firebase auth
        auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
                loadingDialog.dismissDialog();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Error: ", e.getMessage());
                Toast.makeText(getApplicationContext(), "Email or Password entered is no exist or incorrect!", Toast.LENGTH_SHORT).show();
                loadingDialog.dismissDialog();
                etPassord.setText("");
            }
        });
    }
}
