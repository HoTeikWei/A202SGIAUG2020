package com.example.assignment;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;

public class ForgetPasswordDialog {
    private Activity activity;
    private AlertDialog dialog;

    ForgetPasswordDialog(Activity myActivity){
        activity = myActivity;
    }

    void callDialog(){
        //used to create an alert dialog with custom view
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View lView = inflater.inflate(R.layout.fp_dialog, null);
        builder.setView(lView);
        //used to connect field in custom view
        final EditText etEmail = lView.findViewById(R.id.fpDialogEtEmail);
        final Button btnFp = lView.findViewById(R.id.fpDialogBtnSend);
        btnFp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                //validation
                if(email.isEmpty()){
                    Toast.makeText(activity, "Email fields is empty!", Toast.LENGTH_SHORT).show();
                }else{
                    //validation
                    if(isEmailValid(email)){
                        final LoadingDialog loadingDialog = new LoadingDialog(activity);
                        loadingDialog.startLoadingDialog();
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        //used to send reset password message to selected email
                        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    loadingDialog.dismissDialog();
                                    Toast.makeText(activity, "Email sent!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    loadingDialog.dismissDialog();
                                    Toast.makeText(activity, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else{
                        Toast.makeText(activity, "This is not a valid email!", Toast.LENGTH_SHORT).show();

                    }
                }


            }
        });
        dialog = builder.create();
        dialog.show();
    }

    private boolean isEmailValid(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
