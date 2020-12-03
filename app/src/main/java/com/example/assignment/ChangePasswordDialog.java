package com.example.assignment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class ChangePasswordDialog {

    private Activity activity;
    private AlertDialog dialog;

    ChangePasswordDialog(Activity myActivity) {
        activity = myActivity;
    }

    void callDialog() {
        //create an alert dialog with custom dialog view
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View lView = inflater.inflate(R.layout.cp_dialog, null);
        builder.setView(lView);

        //used to link the variable in custom dialog view
        final EditText etPass = lView.findViewById(R.id.cpDialogEtPassword);
        final Button btnCP = lView.findViewById(R.id.cpDialogBtnChange);
        btnCP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(activity);
                confirmDialog.setTitle("Comfirmation");
                confirmDialog.setMessage("Are you sure you want to change password?");
                confirmDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        String nPass = etPass.getText().toString();
                        //validation
                        if (nPass.isEmpty()) {
                            Toast.makeText(activity, "You cannot empty your new password!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (nPass.length() < 6) {
                                Toast.makeText(activity, "Password must more than 6 character!", Toast.LENGTH_SHORT).show();
                            } else {
                                final LoadingDialog loadingDialog = new LoadingDialog(activity);
                                loadingDialog.startLoadingDialog();
                                //connect to firebase auth
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                //update password
                                user.updatePassword(nPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            loadingDialog.dismissDialog();
                                            Toast.makeText(activity, "Password changed!", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        } else {
                                            loadingDialog.dismissDialog();
                                            Toast.makeText(activity, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }).setNegativeButton("No", null);
                AlertDialog cDialog = confirmDialog.create();
                cDialog.show();

            }
        });
        dialog = builder.create();
        dialog.show();
    }
}
