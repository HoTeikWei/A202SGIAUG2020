package com.example.assignment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ListFragment extends Fragment {

    private Spinner spTag;
    private ImageView ivSearch;

    private LoadingDialog loadingDialog;

    private GridView gridview;
    private List<Recipe> recipes = new ArrayList<Recipe>();
    private List<String> recipesId = new ArrayList<String>();
    private  List<String> selectedTag = new ArrayList<>();
    private ListAdapter adapter;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        loadingDialog = new LoadingDialog((Activity) getContext());

        gridview = view.findViewById(R.id.listGridView);
        spTag = view.findViewById(R.id.listSpTag);
        ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(getContext(), R.array.tagList, android.R.layout.simple_spinner_item);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTag.setAdapter(spAdapter);
        ivSearch = view.findViewById(R.id.listIvSearch);
        db = FirebaseFirestore.getInstance();
        //used to get all data from cloud firestore database
        db.collection("recipe").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Recipe r = document.toObject(Recipe.class);
                        String id = document.getId();
                        recipes.add(r);
                        recipesId.add(id);
                    }
                    adapter = new ListAdapter(getActivity(), recipes);
                    gridview.setAdapter(adapter);
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent readIntent = new Intent(getContext(), ViewRecipeActivity.class);
                            readIntent.putExtra("UniqueId", recipesId.get(position));
                            readIntent.putExtra("FragmentId", 3);
                            startActivity(readIntent);
                            ((Activity) getContext()).finish();
                        }
                    });
                } else {
                    Log.e("Error: ", task.getException().toString());
                }
            }
        });
        //used to filter data in cloud firestore
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.startLoadingDialog();
                String filterTag = spTag.getSelectedItem().toString();
                if (filterTag.isEmpty() || filterTag.equals("No Tag Selected")) {
                    loadingDialog.dismissDialog();
                    //used to get all data from cloud firestore database
                    db.collection("recipe").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            recipes.clear();
                            recipesId.clear();
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Recipe r = document.toObject(Recipe.class);
                                    String id = document.getId();
                                    recipes.add(r);
                                    recipesId.add(id);
                                }
                                adapter = new ListAdapter(getActivity(), recipes);
                                gridview.setAdapter(adapter);
                                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent readIntent = new Intent(getContext(), ViewRecipeActivity.class);
                                        readIntent.putExtra("UniqueId", recipesId.get(position));
                                        startActivity(readIntent);
                                        ((Activity) getContext()).finish();
                                    }
                                });
                            } else {
                                Log.e("Error: ", task.getException().toString());
                            }
                        }
                    });
                } else {
                    //used to filter data from cloud firestore database and get the filtered data
                    db.collection("recipe").whereArrayContains("tag", filterTag).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            recipes.clear();
                            recipesId.clear();
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Recipe r = document.toObject(Recipe.class);
                                    String id = document.getId();
                                    recipes.add(r);
                                    recipesId.add(id);
                                }
                                if(recipes.isEmpty()) {
                                    Toast.makeText(getActivity(), "Current there is not recipe match selected tag!", Toast.LENGTH_SHORT).show();
                                }
                                adapter = new ListAdapter(getActivity(), recipes);
                                gridview.setAdapter(adapter);
                                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent readIntent = new Intent(getContext(), ViewRecipeActivity.class);
                                        readIntent.putExtra("UniqueId", recipesId.get(position));
                                        startActivity(readIntent);
                                        ((Activity) getContext()).finish();
                                    }
                                });
                                loadingDialog.dismissDialog();
                            } else {
                                loadingDialog.dismissDialog();
                                Log.e("Error: ", task.getException().toString());
                            }
                        }
                    });
                }
            }
        });


    }

}
