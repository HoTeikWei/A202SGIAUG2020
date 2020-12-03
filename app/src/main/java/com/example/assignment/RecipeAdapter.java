package com.example.assignment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Context mContext;
    private List<Recipe> mRecipes;
    private List<String> mUniqueIds;
    private DatabaseReference dbLikes;
    private FirebaseUser currentUser;

    public RecipeAdapter(Context context, List<Recipe> recipes, List<String> uniqueIds) {
        mContext = context;
        mRecipes = recipes;
        mUniqueIds = uniqueIds;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.recipe_items, parent, false);
        return new RecipeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecipeViewHolder holder, final int position) {
        final Recipe current = mRecipes.get(position);
        holder.tvName.setText(current.getName());
        holder.tvUser.setText("Created by: " + current.getUser());
        List<String> tagList = current.getTag();
        final String currentid = mUniqueIds.get(position);
        String tag = "";
        for (String temp : tagList)
            tag = tag + temp + "/";
        tag = tag.substring(0, tag.length() - 1);

        final List<String> likesList = new ArrayList<>();
        final List<String> likesListId = new ArrayList<>();
        dbLikes = FirebaseDatabase.getInstance().getReference().child("like");
        dbLikes.child(currentid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String currentLikeId = dataSnapshot.getKey();
                likesList.clear();
                likesListId.clear();
                //used to get all data in realtime database
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    likesList.add(snapshot.getValue(String.class));
                    likesListId.add(snapshot.getKey());
                }
                holder.tvLike.setText(String.valueOf(likesList.size() - 1));
                //used to like and unlike operation
                holder.tvLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (currentUser != null) {
                            boolean checkLike = false;
                            String userEmail = currentUser.getEmail();
                            for (int i = 0; i < likesList.size(); i++) {
                                //used to check is current user like before or no
                                if (userEmail.equals(likesList.get(i))) {
                                    //delete selected data in realtime database
                                    dbLikes.child(currentid).child(likesListId.get(i)).removeValue();
                                    checkLike = true;
                                }
                            }
                            if (!checkLike) {
                                //insert data into realtime database
                                dbLikes.child(currentid).push().setValue(userEmail);
                            }
                            dbLikes.child(currentid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    likesList.clear();
                                    likesListId.clear();
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        likesList.add(snapshot.getValue(String.class));
                                        likesListId.add(snapshot.getKey());
                                    }
                                    //reset likes
                                    holder.tvLike.setText(String.valueOf(likesList.size() - 1));
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        } else {
                            Toast.makeText(mContext, "You need to login before like this post!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.tvTag.setText(tag);
        Picasso.get().

                load(current.getImgUrl()).

                placeholder(R.drawable.loading_img).

                fit().

                centerCrop().

                into(holder.ivImage);

        holder.ivAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent readIntent = new Intent(mContext, ViewRecipeActivity.class);
                readIntent.putExtra("UniqueId", currentid);
                readIntent.putExtra("FragmentId", 1);
                mContext.startActivity(readIntent);
                ((Activity) mContext).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName, tvTag, tvUser, tvLike;
        public ImageView ivImage, ivAction;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.recipeitemTvRecipeName);
            tvTag = itemView.findViewById(R.id.recipeitemTvRecipeTag);
            tvUser = itemView.findViewById(R.id.recipeitemTvRecipeUser);
            tvLike = itemView.findViewById(R.id.recipeitemTvRecipeLikes);
            ivImage = itemView.findViewById(R.id.recipeitemIvRecipeImg);
            ivAction = itemView.findViewById(R.id.recipeitemIvAction);
        }
    }
}
