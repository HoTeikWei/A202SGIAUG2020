package com.example.assignment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyRecipeFragment extends Fragment {

    private ImageView ivAdd, ivSearch;
    private Spinner sp;
    private RecyclerView recyclerView;
    private SQLiteDatabaseHelper localDb;
    private LocalRecipeAdapter adapter;
    private LoadingDialog loadingDialog;
    private List<LocalRecipe> lrList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_myrecipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingDialog = new LoadingDialog((Activity) getContext());

        //used to create adapter and link it to spinner
        sp = view.findViewById(R.id.myrecipeSpinner);
        ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(getContext(), R.array.localDisplayType, android.R.layout.simple_spinner_item);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(spAdapter);

        localDb = new SQLiteDatabaseHelper(getActivity());
        lrList = localDb.localRecipeList();

        recyclerView = view.findViewById(R.id.myrecipeRecyclerView);
        adapter = new LocalRecipeAdapter(getActivity(), localDb, 0, lrList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        ivSearch = view.findViewById(R.id.myrecipeIvSearch);
        //filter process
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchItem = sp.getSelectedItem().toString();
                if (searchItem.isEmpty() || searchItem.equals("All recipe")) {
                    lrList.clear();
                    lrList = localDb.localRecipeList();
                    adapter = new LocalRecipeAdapter(getActivity(), localDb, 0, lrList);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } else if (searchItem.equals("My recipe")) {
                    lrList.clear();
                    lrList = localDb.myLocalRecipeList();
                    adapter = new LocalRecipeAdapter(getActivity(), localDb, 1, lrList);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } else if (searchItem.equals("Downloaded recipe")) {
                    lrList.clear();
                    lrList = localDb.downloadLocalRecipeList();
                    adapter = new LocalRecipeAdapter(getActivity(), localDb, 2, lrList);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        ivAdd = view.findViewById(R.id.myrecipeIvAdd);
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CreateRecipeActivity.class));
                getActivity().finish();
            }
        });

    }
}
