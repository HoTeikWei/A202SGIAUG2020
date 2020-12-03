package com.example.assignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CreateRecipeActivity extends AppCompatActivity {

    private MultiSelectionSpinner spTag;
    private ImageView ivFoodImg;
    private EditText etName, etIngredient, etStep;
    private Button btnPost, btnUpload, btnSave;
    private LoadingDialog loadingDialog;
    private final String TAG = CreateRecipeActivity.class.getSimpleName();
    private final int REQUEST_CODE_GALLERY = 10;

    private Uri foodUri;
    private FirebaseUser user;
    private StorageReference storageRef;
    private DatabaseReference realtimeDb, realtimeDbLikes;
    private FirebaseFirestore db;
    private StorageTask uploadTask;
    private SQLiteDatabaseHelper sqliteDb;

    private String newImageName;

    //used to create top navigation bar - back function
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intentFragment = new Intent(getApplicationContext(), MainActivity.class);
                intentFragment.putExtra("FragmentId", 2);
                startActivity(intentFragment);
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
        setContentView(R.layout.activity_create_recipe);

        //used to generate top navigation bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        }

        storageRef = FirebaseStorage.getInstance().getReference("recipe");
        db = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance().getReference().child("comment");
        realtimeDbLikes = FirebaseDatabase.getInstance().getReference().child("like");
        user = FirebaseAuth.getInstance().getCurrentUser();
        sqliteDb = new SQLiteDatabaseHelper(getApplicationContext());
        spTag = findViewById(R.id.createrecipeSpTag);
        String[] foodTag = getResources().getStringArray(R.array.tagList);
        spTag.setItems(foodTag);

        loadingDialog = new LoadingDialog(CreateRecipeActivity.this);

        ivFoodImg = findViewById(R.id.createrecipeIvFoodImg);
        btnUpload = findViewById(R.id.createrecipeBtnUpload);
        btnPost = findViewById(R.id.createrecipeBtnPost);
        btnSave = findViewById(R.id.createrecipeBtnSave);
        etName = findViewById(R.id.createrecipeEtRecipeName);
        etIngredient = findViewById(R.id.createrecipeEtRecipeIngredient);
        etStep = findViewById(R.id.createrecipeEtRecipeStep);

        //used to open gallery
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(CreateRecipeActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_GALLERY);
            }
        });
        //used to save recipe into sqlite database
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CreateRecipeActivity.this);
                alertDialog.setTitle("Food Image");
                alertDialog.setMessage("Food Image Will Not Save!");
                alertDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String username = "local";
                        String recipeName = etName.getText().toString();
                        String recipeTag = spTag.getSelectedItemsAsString();
                        String recipeIngredient = etIngredient.getText().toString();
                        String recipeStep = etStep.getText().toString();
                        //validation
                        if (recipeName.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "You cannot save recipe without name!", Toast.LENGTH_SHORT).show();
                        } else {
                            loadingDialog.startLoadingDialog();
                            LocalRecipe lr = new LocalRecipe(0, recipeName, recipeTag, recipeIngredient, recipeStep, username);
                            //insert function
                            if (sqliteDb.saveRecipe(lr)) {
                                loadingDialog.dismissDialog();
                                Toast.makeText(getApplicationContext(), "Recipe saved", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            } else {
                                loadingDialog.dismissDialog();
                                Toast.makeText(getApplicationContext(), "Fail to save!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                alertDialog.setNegativeButton("Cancel", null);
                AlertDialog dialog = alertDialog.create();
                dialog.show();
            }
        });
        //used to validation checking before upload item into firebase
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validation of user
                if (user != null) {
                    //validation of process, to prevent duplication item
                    if (uploadTask != null && uploadTask.isInProgress()) {
                        Toast.makeText(getApplicationContext(), "Upload in progress!", Toast.LENGTH_SHORT).show();
                    } else {
                        String name = etName.getText().toString();
                        String cookStep = etStep.getText().toString();
                        String ingredient = etIngredient.getText().toString();
                        String tempTag = spTag.getSelectedItemsAsString();
                        String selectedTag[] = tempTag.split("/");
                        if (name.isEmpty() || cookStep.isEmpty() || ingredient.isEmpty() || tempTag.isEmpty() || ivFoodImg.getDrawable() == null) {
                            Toast.makeText(getApplicationContext(), "Some field are empty!", Toast.LENGTH_SHORT).show();
                        } else {
                            loadingDialog.startLoadingDialog();
                            //upload function
                            uploadFile();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You have to login first!", Toast.LENGTH_SHORT).show();
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

    //used to get file type, e.g. png, jpeg ...
    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    //function to upload item into storage databases
    private void uploadFile() {
        newImageName = System.currentTimeMillis() + "." + getFileExtension(foodUri);
        //image checking
        if (foodUri != null) {
            //upload to specific child (file path)
            final StorageReference fileReference = storageRef.child(newImageName);
            uploadTask = fileReference.putFile(foodUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //get the file url
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            //another upload function
                            uploadRecipe(url, newImageName);
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

        } else {
            Toast.makeText(getApplicationContext(), "No images selected!", Toast.LENGTH_SHORT).show();
        }
    }

    //used to upload item into cloud firestore and realtime database
    private void uploadRecipe(String url, String imageName) {
        String name = etName.getText().toString();
        String cookStep = etStep.getText().toString();
        String ingredient = etIngredient.getText().toString();
        String tempTag = spTag.getSelectedItemsAsString();
        String filterTag[] = tempTag.split("/");
        String userEmail = "";
        List<String> selectedTag = new ArrayList<>();
        for (String tags : filterTag)
            selectedTag.add(tags);

        //used to get current user email
        if (user != null) {
            userEmail = user.getEmail();
        }
        Recipe recipeData = new Recipe(url, imageName, name, selectedTag, ingredient, cookStep, userEmail);
        final String finalUserEmail = userEmail;
        //insert function for cloud firestore
        db.collection("recipe").add(recipeData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                String uniqueId = documentReference.getId();
                //used to create realtime database
                createCommentDb(finalUserEmail, uniqueId);
                createLikesDb(uniqueId);
                loadingDialog.dismissDialog();
                Toast.makeText(getApplicationContext(), "Recipe created!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingDialog.dismissDialog();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createCommentDb(String finalUserEmail, String uniqueId) {
        Comment commentCreation = new Comment(finalUserEmail, "Created this recipe!");
        //insert function for realtime database
        realtimeDb.child(uniqueId).push().setValue(commentCreation);
    }

    private void createLikesDb(String uniqueId) {
        //insert function for realtime database
        realtimeDbLikes.child(uniqueId).push().setValue("CREATION-ONLY");
    }

}
