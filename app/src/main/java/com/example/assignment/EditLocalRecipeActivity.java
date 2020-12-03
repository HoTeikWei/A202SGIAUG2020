package com.example.assignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

public class EditLocalRecipeActivity extends AppCompatActivity {

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

    private int localId;

    //top navigation bar - back function
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intentFragment = new Intent(getApplicationContext(), ViewLocalRecipe.class);
                intentFragment.putExtra("LocalPosition", localId);
                startActivity(intentFragment);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_local_recipe);
        //used to create top navigation bar
        if (getSupportActionBar() != null ){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        }


        Intent itemItem = getIntent();
        localId = itemItem.getIntExtra("LocalId", -1);
        if (localId == -1) {
            Toast.makeText(getApplicationContext(), "Some error occured: could not pass previous page id", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
        sqliteDb = new SQLiteDatabaseHelper(getApplicationContext());
        final LocalRecipe localrecipe = sqliteDb.getLocalRecipe(localId);

        storageRef = FirebaseStorage.getInstance().getReference("recipe");
        db = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance().getReference().child("comment");
        realtimeDbLikes = FirebaseDatabase.getInstance().getReference().child("like");
        user = FirebaseAuth.getInstance().getCurrentUser();
        spTag = findViewById(R.id.editlocalrecipeSpTag);
        String[] foodTag = getResources().getStringArray(R.array.tagList);
        spTag.setItems(foodTag);

        loadingDialog = new LoadingDialog(EditLocalRecipeActivity.this);
        ivFoodImg = findViewById(R.id.editlocalrecipeIvFoodImg);
        btnUpload = findViewById(R.id.editlocalrecipeBtnUpload);
        btnPost = findViewById(R.id.editlocalrecipeBtnPost);
        btnSave = findViewById(R.id.editlocalrecipeBtnSave);
        etName = findViewById(R.id.editlocalrecipeEtRecipeName);
        etIngredient = findViewById(R.id.editlocalrecipeEtRecipeIngredient);
        etStep = findViewById(R.id.editlocalrecipeEtRecipeStep);

        etName.setText(localrecipe.getName());
        etIngredient.setText(localrecipe.getIngredient());
        etStep.setText(localrecipe.getStep());

        //used to gain access to gallery
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(EditLocalRecipeActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_GALLERY);
            }
        });

        //used to save current item into sqlite database
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditLocalRecipeActivity.this);
                alertDialog.setTitle("Food Image");
                alertDialog.setMessage("Food Image Will Not Save!");
                alertDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //edit function in sqlite
                        if (editLocal()) {
                            loadingDialog.dismissDialog();
                            Toast.makeText(getApplicationContext(), "Edit local recipe success!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadingDialog.dismissDialog();
                        Toast.makeText(getApplicationContext(), "Save operation cancelled!", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog dialog = alertDialog.create();
                dialog.show();
            }
        });

        //used to validation before upload into firebase
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String creator = localrecipe.getUser();
                Log.e("Name: ", creator);
                if (!creator.equals("local")) {
                    Toast.makeText(getApplicationContext(), "You cannot re-port other's people recipe!", Toast.LENGTH_SHORT).show();
                } else {
                    editLocal();
                    if (user != null) {
                        if (uploadTask != null && uploadTask.isInProgress()) {
                            Toast.makeText(getApplicationContext(), "Upload in progress!", Toast.LENGTH_SHORT).show();
                        } else {
                            String name = etName.getText().toString();
                            String cookStep = etStep.getText().toString();
                            String ingredient = etIngredient.getText().toString();
                            String tempTag = spTag.getSelectedItemsAsString();
                            String selectedTag[] = tempTag.split("/");
                            if (name.isEmpty() || cookStep.isEmpty() || ingredient.isEmpty() || tempTag.isEmpty() || ivFoodImg.getDrawable() == null) {
                                loadingDialog.dismissDialog();
                                Toast.makeText(getApplicationContext(), "Some field are empty!", Toast.LENGTH_SHORT).show();
                            } else {
                                loadingDialog.startLoadingDialog();
                                //used to upload file to storage, insert item into cloud store, and create 2 realtime database
                                uploadFile();
                            }

                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "You have to login first!", Toast.LENGTH_SHORT).show();
                    }
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

    //used to get image file type, e.g. png, jpeg...
    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    //used to upload file into storage database
    private void uploadFile() {
        if (foodUri != null) {
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(foodUri));
            //insert function in storage database
            uploadTask = fileReference.putFile(foodUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //get the download url of file upload
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            String imageName = System.currentTimeMillis() + "." + getFileExtension(foodUri);
                            String name = etName.getText().toString();
                            String cookStep = etStep.getText().toString();
                            String ingredient = etIngredient.getText().toString();
                            String tempTag = spTag.getSelectedItemsAsString();
                            String filterTag[] = tempTag.split("/");
                            String userEmail = "";
                            List<String> selectedTag = new ArrayList<>();
                            for (String tags : filterTag)
                                selectedTag.add(tags);

                            if (user != null) {
                                userEmail = user.getEmail();
                            }
                            Recipe recipeData = new Recipe(url, imageName, name, selectedTag, ingredient, cookStep, userEmail);
                            final String finalUserEmail = userEmail;
                            //used to insert item into cloud store
                            db.collection("recipe").add(recipeData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    String uniqueId = documentReference.getId();
                                    Comment commentCreation = new Comment(finalUserEmail, "Created this recipe!");
                                    //used to create realtime database
                                    realtimeDb.child(uniqueId).push().setValue(commentCreation);
                                    realtimeDbLikes.child(uniqueId).push().setValue("CREATION-ONLY");
                                    loadingDialog.dismissDialog();
                                    Toast.makeText(getApplicationContext(), "Recipe created!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadingDialog.dismissDialog();
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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

        } else {
            Toast.makeText(getApplicationContext(), "No images selected!", Toast.LENGTH_SHORT).show();
        }
    }

    //validation for item want to edit and process the edit function
    private boolean editLocal() {
        String username = "local";
        String recipeName = etName.getText().toString();
        String recipeTag = spTag.getSelectedItemsAsString();
        String recipeIngredient = etIngredient.getText().toString();
        String recipeStep = etStep.getText().toString();
        //validation
        if (recipeName.isEmpty()) {
            Toast.makeText(getApplicationContext(), "You cannot save blank recipe name!", Toast.LENGTH_SHORT).show();
        } else {
            loadingDialog.startLoadingDialog();
            LocalRecipe lr = new LocalRecipe(localId, recipeName, recipeTag, recipeIngredient, recipeStep, username);
            //edit function
            return sqliteDb.editLocalRecipe(lr);
        }
        return false;
    }
}
