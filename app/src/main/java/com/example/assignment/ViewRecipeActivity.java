package com.example.assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ViewRecipeActivity extends AppCompatActivity {

    private TextView tvName, tvUser, tvDate, tvIngredient, tvStep, tvTag, tvLike, tvDownload;
    private ImageView ivRecipeImage, ivSendComment;
    private EditText etComment;
    private RecyclerView recyclerView;
    private CommentAdapter adapter;

    private LoadingDialog loadingDialog;

    private FirebaseFirestore db;
    private FirebaseUser user;
    private DatabaseReference realtimeDb, dbLikes;
    private SQLiteDatabaseHelper localDb;

    private LinkedList<Comment> comments;

    private int fragmentId;
    private String selectedId;
    private Recipe selectedRecipe;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(fragmentId == 5){
                    startActivity(new Intent(getApplicationContext(), MyProfileActivity.class));
                    finish();
                    break;
                }
                Intent intentFragment = new Intent(getApplicationContext(), MainActivity.class);
                intentFragment.putExtra("FragmentId", fragmentId);
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
        setContentView(R.layout.activity_view_recipe);

        if (getSupportActionBar() != null ){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        }


        Intent itemPass = getIntent();
        final String uniqueId = itemPass.getStringExtra("UniqueId");
        fragmentId = itemPass.getIntExtra("FragmentId", -1);
        if (fragmentId == -1){
            Toast.makeText(getApplicationContext(), "Fragment ID didnt pass!", Toast.LENGTH_SHORT).show();
        }

        recyclerView = findViewById(R.id.viewrecipeRecyclerViewComment);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        comments = new LinkedList<>();

        loadingDialog = new LoadingDialog(ViewRecipeActivity.this);

        localDb = new SQLiteDatabaseHelper(getApplicationContext());
        db = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance().getReference().child("comment");
        user = FirebaseAuth.getInstance().getCurrentUser();

        tvName = findViewById(R.id.viewrecipeTvRecipeName);
        tvIngredient = findViewById(R.id.viewrecipeTvRecipeIngredient);
        tvStep = findViewById(R.id.viewrecipeTvRecipeStep);
        tvDate = findViewById(R.id.viewrecipeTvRecipeCreatedDate);
        tvTag = findViewById(R.id.viewrecipeTvRecipeTag);
        tvUser = findViewById(R.id.viewrecipeTvRecipeUsername);
        tvLike = findViewById(R.id.viewrecipeTvRecipeLike);
        tvDownload = findViewById(R.id.viewrecipeTvRecipeDownload);
        etComment = findViewById(R.id.viewrecipeEtComment);
        ivSendComment = findViewById(R.id.viewrecipeIvSend);
        ivRecipeImage = findViewById(R.id.viewrecipeIvRecipeImage);

        //used to get data from cloud firestore database
        db.collection("recipe").document(uniqueId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        selectedRecipe = doc.toObject(Recipe.class);
                        realtimeDb.child(uniqueId).orderByChild("date").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                comments.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    Comment tempComment = snapshot.getValue(Comment.class);
                                    comments.push(tempComment);
                                }
                                adapter = new CommentAdapter(getApplicationContext(), comments);
                                recyclerView.setAdapter(adapter);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        SimpleDateFormat DateFor = new SimpleDateFormat("h:mm a, z dd/MM/yyyy");
                        Date convertDate = selectedRecipe.getDate();
                        convertDate.setTime(convertDate.getTime());
                        String date = DateFor.format(convertDate);
                        tvDate.setText(date);
                        tvName.setText(selectedRecipe.getName());
                        tvIngredient.setText(selectedRecipe.getIngredient());
                        tvStep.setText(selectedRecipe.getStep());
                        tvUser.setText(selectedRecipe.getUser());
                        Picasso.get().load(selectedRecipe.getImgUrl()).placeholder(R.drawable.loading_img).fit().centerCrop().into(ivRecipeImage);
                        List<String> tagList = selectedRecipe.getTag();
                        String tag = "";
                        for (String temp : tagList)
                            tag = tag + temp + "/";
                        tag = tag.substring(0, tag.length() - 1);
                        tvTag.setText(tag);
                    } else {
                        Toast.makeText(getApplicationContext(), "Item no found!", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
        final List<String> likesList = new ArrayList<>();
        final List<String> likesListId = new ArrayList<>();
        dbLikes = FirebaseDatabase.getInstance().getReference().child("like");
        //used to get data in realtime database
        dbLikes.child(uniqueId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String currentLikeId = dataSnapshot.getKey();
                likesList.clear();
                likesListId.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    likesList.add(snapshot.getValue(String.class));
                    likesListId.add(snapshot.getKey());
                }
                tvLike.setText(String.valueOf(likesList.size()-1));
            }
            @Override
            public void onCancelled (@NonNull DatabaseError databaseError){

            }
        });
        //like and unlike function
        tvLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    boolean checkLike = false;
                    String userEmail = user.getEmail();
                    for (int i = 0; i < likesList.size(); i++) {
                        if (userEmail.equals(likesList.get(i))) {
                            dbLikes.child(uniqueId).child(likesListId.get(i)).removeValue();
                            checkLike = true;
                        }
                    }
                    if(!checkLike){
                        dbLikes.child(uniqueId).push().setValue(userEmail);
                    }
                    dbLikes.child(uniqueId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            likesList.clear();
                            likesListId.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                likesList.add(snapshot.getValue(String.class));
                                likesListId.add(snapshot.getKey());
                            }
                            tvLike.setText(String.valueOf(likesList.size()-1));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else{
                    Toast.makeText(getApplicationContext(), "You need to login before like this post!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder downloadDialog = new AlertDialog.Builder(ViewRecipeActivity.this);
                downloadDialog.setTitle("Download Recipe");
                downloadDialog.setMessage("Are you sure you want to download this recipe?");
                downloadDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<String> tagList = selectedRecipe.getTag();
                        String tag = "";
                        for (String temp : tagList)
                            tag = tag + temp + "/";
                        tag = tag.substring(0, tag.length() - 1);
                        LocalRecipe lr = new LocalRecipe(0, selectedRecipe.getName(), tag, selectedRecipe.getIngredient(), selectedRecipe.getStep(), selectedRecipe.getUser());
                        loadingDialog.startLoadingDialog();
                        //used to insert data into sqlite database
                        if(localDb.saveRecipe(lr)){
                            loadingDialog.dismissDialog();
                            Toast.makeText(getApplicationContext(), "Recipe downloaded!", Toast.LENGTH_SHORT).show();
                        }else{
                            loadingDialog.dismissDialog();
                            Toast.makeText(getApplicationContext(), "Recipe failed to download!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("No", null);
                AlertDialog dialog = downloadDialog.create();
                dialog.show();
            }
        });

        ivSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    String commenttxt = etComment.getText().toString();
                    if (!commenttxt.isEmpty()) {
                        loadingDialog.startLoadingDialog();
                        DatabaseReference commentDb = FirebaseDatabase.getInstance().getReference().child("comment");
                        Comment commentCreation = new Comment(user.getEmail(), commenttxt);
                        //used to insert data in realtime database
                        realtimeDb.child(uniqueId).push().setValue(commentCreation).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                loadingDialog.dismissDialog();
                                etComment.setText("");
                                realtimeDb.child(uniqueId).orderByChild("date").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        comments.clear();
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            Comment tempComment = snapshot.getValue(Comment.class);
                                            comments.push(tempComment);
                                        }
                                        adapter = new CommentAdapter(getApplicationContext(), comments);
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

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

                    } else {
                        Toast.makeText(getApplicationContext(), "Comment field is empty!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You need to login before comment!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
