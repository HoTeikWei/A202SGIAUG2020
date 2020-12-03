package com.example.assignment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment {

    private ProgressBar pb;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;

    private FirebaseFirestore db;
    private List<Recipe> recipes;
    private List<String> uniqueIds;
    private boolean filterChecked = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pb = view.findViewById(R.id.homeProgressBar);
        recyclerView = view.findViewById(R.id.homeRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recipes = new ArrayList<>();
        uniqueIds = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        //used to retrieve all data from cloud firestore
        db.collection("recipe").orderBy("date", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Recipe r = document.toObject(Recipe.class);
                        recipes.add(r);
                        uniqueIds.add(document.getId());
                    }

                    adapter = new RecipeAdapter(getContext(), recipes, uniqueIds);
                    recyclerView.setAdapter(adapter);
                    pb.setVisibility(View.INVISIBLE);
                } else {
                    Log.e("Error: ", String.valueOf(task.getException()));
                    pb.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
