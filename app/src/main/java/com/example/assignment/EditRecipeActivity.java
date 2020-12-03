package com.example.assignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditRecipeActivity extends AppCompatActivity {

    private MultiSelectionSpinner spTag;
    private ImageView ivFoodImg;
    private EditText etName, etIngredient, etStep;
    private Button btnUpload;
    private ImageButton ibSave;
    private LoadingDialog loadingDialog;
    private final int REQUEST_CODE_GALLERY = 10;

    private Uri foodUri;
    private FirebaseUser user;
    private StorageReference storageRef;
    private DatabaseReference realtimeDb, realtimeDbLikes;
    private FirebaseFirestore db;
    private StorageTask uploadTask;
    private SQLiteDatabaseHelper sqliteDb;

    private Recipe currentRecipe;
    private String uniqueId, userEmail, newImageName;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);
        //used to create top navigation bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
            getSupportActionBar().setTitle("Back");
        }

        Intent itemItem = getIntent();
        uniqueId = itemItem.getStringExtra("UniqueId");

        loadingDialog = new LoadingDialog(EditRecipeActivity.this);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("recipe");
        if (user != null)
            userEmail = user.getEmail();

        spTag = findViewById(R.id.editrecipeSpTag);
        String[] foodTag = getResources().getStringArray(R.array.tagList);
        spTag.setItems(foodTag);
        ivFoodImg = findViewById(R.id.editrecipeIvFoodImg);
        btnUpload = findViewById(R.id.editrecipeBtnUpload);
        ibSave = findViewById(R.id.editrecipeIbSave);
        etName = findViewById(R.id.editrecipeEtRecipeName);
        etIngredient = findViewById(R.id.editrecipeEtRecipeIngredient);
        etStep = findViewById(R.id.editrecipeEtRecipeStep);

        //used to retrieve data in cloud firestore
        db.collection("recipe").document(uniqueId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        currentRecipe = doc.toObject(Recipe.class);
                        Picasso.get().load(currentRecipe.getImgUrl()).placeholder(R.drawable.loading_img).fit().centerCrop().into(ivFoodImg);
                        etName.setText(currentRecipe.getName());
                        etIngredient.setText(currentRecipe.getIngredient());
                        etStep.setText(currentRecipe.getStep());
                    } else {
                        Toast.makeText(getApplicationContext(), "Recipe no found!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //used to gain permission to open gallery
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(EditRecipeActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_GALLERY);
            }
        });

        //used to validation before edit
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.startLoadingDialog();
                String name = etName.getText().toString();
                String ingredient = etIngredient.getText().toString();
                String step = etStep.getText().toString();
                String tag = spTag.getSelectedItemsAsString();
                //validation
                if (name.isEmpty() || ingredient.isEmpty() || step.isEmpty() || tag.isEmpty() || ivFoodImg.getDrawable() == null) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(getApplicationContext(), "Some field are empty!", Toast.LENGTH_SHORT).show();
                } else {
                    //upload image file
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
            ivFoodImg.setImageURI(foodUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //used to get file type, e.g. jpeg, png...
    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    //used to upload file into storage database
    private void uploadFile() {
        if (foodUri != null) {
            newImageName = System.currentTimeMillis() + "." + getFileExtension(foodUri);
            final StorageReference fileReference = storageRef.child(newImageName);
            //used to insert file into storage
            uploadTask = fileReference.putFile(foodUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //used to get uploaded file's url
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String url = uri.toString();
                            //used to delete previous/old file
                            StorageReference delRef = storageRef.child(currentRecipe.getImgName());
                            delRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //used to edit data in cloud firestore database
                                    updateRecipe(url, newImageName);
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
        } else if (ivFoodImg.getDrawable() != null) {
            updateRecipe(currentRecipe.getImgUrl(), currentRecipe.getImgName());
        } else {
            loadingDialog.dismissDialog();
            Toast.makeText(getApplicationContext(), "No images selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRecipe(String url, String imgName) {
        String name = etName.getText().toString();
        String ingredient = etIngredient.getText().toString();
        String step = etStep.getText().toString();
        String tags = spTag.getSelectedItemsAsString();
        String filterTag[] = tags.split("/");
        List<String> selectedTag = new ArrayList<>();
        for (String item : filterTag)
            selectedTag.add(item);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("ingredient", ingredient);
        updates.put("step", step);
        updates.put("tag", selectedTag);
        updates.put("imgName", imgName);
        updates.put("imgUrl", url);
        //used to update data in cloud firestore
        db.collection("recipe").document(uniqueId).update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(getApplicationContext(), "Edited!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MyProfileActivity.class));
                    finish();
                } else {
                    loadingDialog.dismissDialog();
                    Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
