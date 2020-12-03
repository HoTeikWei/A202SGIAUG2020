package com.example.assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.view.Change;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MyProfileActivity extends AppCompatActivity {

    private ImageView ivProfileImage, ivProfileEdit;
    private TextView tvName, tvEmail, tvGender, tvMessage;

    private GridView gridview;
    private List<Recipe> recipes = new ArrayList<Recipe>();
    private List<String> recipesId = new ArrayList<String>();
    private ProfileAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseDatabase realtimeDb;
    private DocumentReference docReff;
    private StorageReference storageRef;
    private FirebaseUser user;

    private LoadingDialog loadingDialog;
    private ChangePasswordDialog cpDialog;
    private User userInfo;
    private String userEmail;

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
        setContentView(R.layout.activity_my_profile);

        if (getSupportActionBar() != null ){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        }

        gridview = findViewById(R.id.profileGridView);
        tvName = findViewById(R.id.profileTvName);
        tvEmail = findViewById(R.id.profileTvEmailAddress);
        tvGender = findViewById(R.id.profileGender);
        tvMessage = findViewById(R.id.profileTvNoContent);
        ivProfileImage = findViewById(R.id.profileIvImage);
        ivProfileEdit = findViewById(R.id.profileIvEditProfile);

        loadingDialog = new LoadingDialog(MyProfileActivity.this);
        cpDialog = new ChangePasswordDialog(MyProfileActivity.this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = user.getEmail();
        db = FirebaseFirestore.getInstance();
        docReff = db.collection("user").document(userEmail);
        storageRef = FirebaseStorage.getInstance().getReference("recipe");
        realtimeDb = FirebaseDatabase.getInstance();
        //used to get data from cloud firestore
        docReff.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc =task.getResult();
                    userInfo = doc.toObject(User.class);
                    tvName.setText(userInfo.getName());
                    tvEmail.setText(userInfo.getEmailAddress());
                    tvGender.setText(userInfo.getGender());
                    if(!userInfo.getImageUrl().isEmpty() && !userInfo.getImageUrl().equals("-")){
                        Picasso.get().load(userInfo.getImageUrl()).placeholder(R.drawable.loading_img).fit().centerCrop().into(ivProfileImage);
                    }else{
                        ivProfileImage.setImageResource(R.drawable.no_img);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Error: ", e.getMessage());
            }
        });

        ivProfileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder editDialog = new AlertDialog.Builder(MyProfileActivity.this);
                editDialog.setTitle("Edit");
                editDialog.setPositiveButton("Edit Profile", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                        finish();
                    }
                }).setNegativeButton("Edit Password", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cpDialog.callDialog();
                    }
                });
               AlertDialog eDialog = editDialog.create();
                eDialog.show();
            }
        });

        //used to retrieve all recipe with some fields is equal to current user
        db.collection("recipe").whereEqualTo("user", userEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()){
                        Recipe r = document.toObject(Recipe.class);
                        String id = document.getId();
                        recipes.add(r);
                        recipesId.add(id);
                    }
                    //used to set gridview adapter
                    adapter = new ProfileAdapter(getApplication(), recipes);
                    gridview.setAdapter(adapter);
                    if(recipes.isEmpty()){
                        tvMessage.setVisibility(View.VISIBLE);
                    }
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            final int currPosition = position;
                            AlertDialog.Builder actionDialog = new AlertDialog.Builder(MyProfileActivity.this);
                            actionDialog.setTitle("Action");
                            actionDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MyProfileActivity.this);
                                    deleteDialog.setTitle("Delete Post");
                                    deleteDialog.setMessage("Are you sure you want to delete this post?");
                                    deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            loadingDialog.startLoadingDialog();
                                            deletePost(recipesId.get(currPosition));
                                        }
                                    }).setNegativeButton("No", null);
                                    AlertDialog nDialog = deleteDialog.create();
                                    nDialog.show();
                                }
                            }).setNeutralButton("View", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent readIntent = new Intent(getApplicationContext(), ViewRecipeActivity.class);
                                    readIntent.putExtra("UniqueId", recipesId.get(currPosition));
                                    readIntent.putExtra("FragmentId", 5);
                                    startActivity(readIntent);
                                    finish();

                                }
                            }).setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent editIntent = new Intent(getApplicationContext(), EditRecipeActivity.class);
                                    editIntent.putExtra("UniqueId", recipesId.get(currPosition));
                                    startActivity(editIntent);
                                    finish();

                                }
                            });
                            AlertDialog dialog = actionDialog.create();
                            dialog.show();
                        }
                    });
                } else{
                    Log.e("Error: ", task.getException().toString());
                }
            }
        });
    }

    //used to delete post
    private void deletePost(final String uniqueId){
        //used to get selected data from cloud firestore
        db.collection("recipe").document(uniqueId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if(doc.exists()){
                        Recipe recipe = doc.toObject(Recipe.class);
                        StorageReference delStorage = storageRef.child(recipe.getImgName());
                        delStorage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //used to delete selected data in cloud firestore
                                db.collection("recipe").document(uniqueId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //used to retrieve all data that field are equal to user from cloud firestore
                                        db.collection("recipe").whereEqualTo("user", userEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful()){
                                                    //used to delete selected database in realtime database
                                                    realtimeDb.getReference().child("comment").child(uniqueId).removeValue();
                                                    realtimeDb.getReference().child("like").child(uniqueId).removeValue();
                                                    recipes.clear();
                                                    recipesId.clear();
                                                    for (QueryDocumentSnapshot document : task.getResult()){
                                                        Recipe r = document.toObject(Recipe.class);
                                                        String id = document.getId();
                                                        recipes.add(r);
                                                        recipesId.add(id);
                                                    }
                                                    //used to re-assign gridview adapter
                                                    adapter = new ProfileAdapter(getApplication(), recipes);
                                                    gridview.setAdapter(adapter);
                                                    loadingDialog.dismissDialog();
                                                    Toast.makeText(getApplicationContext(), "Delete successfully!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadingDialog.dismissDialog();
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingDialog.dismissDialog();
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        loadingDialog.dismissDialog();
                        Toast.makeText(getApplicationContext(), "No item found!", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }
}
