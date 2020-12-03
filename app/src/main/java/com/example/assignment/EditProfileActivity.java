package com.example.assignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import io.grpc.internal.LogExceptionRunnable;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName;
    private Spinner spGender;
    private Button btnUpload;
    private ImageView ivProfileImage;
    private ImageButton ibEdit;
    private final int REQUEST_CODE_GALLERY = 25;

    private FirebaseFirestore db;
    private DocumentReference docReff;
    private FirebaseUser user;
    private StorageReference storageRef;
    private StorageTask uploadTask;

    private Uri foodUri;
    private User userInfo;
    private LoadingDialog loadingDialog;

    private String newImageName;

    //top navigation bar - back function
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(getApplicationContext(), MyProfileActivity.class));
                finish();
                break;
            default:
                return true;
        }
        return true;
    }
    //used to create top navigation bar
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
            getSupportActionBar().setTitle("Back");
        }

        etName = findViewById(R.id.editprofileEtName);
        ivProfileImage = findViewById(R.id.editprofileIvProfileImage);
        ibEdit = findViewById(R.id.editprofileIbSave);
        btnUpload = findViewById(R.id.editprofileBtnUpload);
        spGender = findViewById(R.id.editprofileSpGender);

        //create adapter for spinner
        final ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.gender, android.R.layout.simple_spinner_item);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(spAdapter);

        loadingDialog = new LoadingDialog(EditProfileActivity.this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = user.getEmail();
        storageRef = FirebaseStorage.getInstance().getReference("profile");
        db = FirebaseFirestore.getInstance();
        docReff = db.collection("user").document(userEmail);
        //used to get user information data from cloud store
        docReff.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    userInfo = doc.toObject(User.class);
                    etName.setText(userInfo.getName());
                    String tempGender = userInfo.getGender();
                    switch (tempGender) {
                        case "-":
                            spGender.setSelection(0);
                            break;
                        case "Male":
                            spGender.setSelection(1);
                            break;
                        case "Female":
                            spGender.setSelection(2);
                            break;
                        case "Other":
                            spGender.setSelection(3);
                            break;
                        default:
                            Log.e("Error: ", "Gender is out of bound!");
                            break;
                    }
                    //used to set profile image for user is exisit
                    if (!userInfo.getImageUrl().isEmpty() && !userInfo.getImageUrl().equals("-"))
                        Picasso.get().load(userInfo.getImageUrl()).placeholder(R.drawable.loading_img).fit().centerCrop().into(ivProfileImage);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Error: ", e.getMessage());
            }
        });

        //used to get the permission for open gallery
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_GALLERY);
            }
        });

        //used to do validation before edit
        ibEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.startLoadingDialog();
                if(ivProfileImage.getDrawable() == null){
                    String url = "";
                    String imageName = "";
                    //update information without image url/ image name
                    updateUserInfo(url, imageName);
                }else{
                    //update image
                    uploadFile();
                }
            }
        });


    }

    //use for check permission to open gallery success or no
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intentPic = new Intent(Intent.ACTION_PICK);
                intentPic.setType("image/*");
                startActivityForResult(intentPic, REQUEST_CODE_GALLERY);
            } else {
                Toast.makeText(getApplicationContext(), "No Permission to open gallery", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //used to set the image view in layout, by using gallery image.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            foodUri = data.getData();
            ivProfileImage.setImageURI(foodUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    //used to upload image file to firebase storage database
    private void uploadFile() {
        if (foodUri != null) {
            newImageName = System.currentTimeMillis() + "." + getFileExtension(foodUri);
            final StorageReference fileReference = storageRef.child(newImageName);
            //insert function in storage database
            uploadTask = fileReference.putFile(foodUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //used to get download url link for uploaded file
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String url = uri.toString();
                            StorageReference delRef = storageRef.child(userInfo.getImageName());
                            delRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //update information with provided url, image name
                                    updateUserInfo(url, newImageName);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Error: ", e.getMessage());
                                }
                            });

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    loadingDialog.dismissDialog();
                    Log.e("Progress:", taskSnapshot.toString());
                }
            });
        } else if(ivProfileImage.getDrawable() != null) {
            updateUserInfo(userInfo.getImageUrl(), userInfo.getImageName());
        } else{
            loadingDialog.dismissDialog();
            Toast.makeText(getApplicationContext(), "No images selected!", Toast.LENGTH_SHORT).show();
        }
    }

    //update information
    private void updateUserInfo(String url, String imageName) {
        String name = etName.getText().toString();
        String gender = spGender.getSelectedItem().toString();
        if (gender.equals("Select Gender")) {
            gender = "-";
        }
        String userEmail = user.getEmail();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("imageUrl", url);
        updates.put("gender", gender);
        updates.put("imageName", imageName);
        //used to edit cloud firestore data
        db.collection("user").document(userEmail).update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    loadingDialog.dismissDialog();
                    Toast.makeText(getApplicationContext(), "Edited!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MyProfileActivity.class));
                    finish();
                }else{
                    loadingDialog.dismissDialog();
                    Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
