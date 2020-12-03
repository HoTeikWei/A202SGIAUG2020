package com.example.assignment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LocalRecipeAdapter extends RecyclerView.Adapter<LocalRecipeAdapter.LocalRecipeViewHolder> {

    private SQLiteDatabaseHelper mDb;
    private Context mContext;
    private LayoutInflater mInflater;
    private int mSearchCode;
    private List<LocalRecipe> mLrList;
    private LocalRecipe current;

    class LocalRecipeViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;
        public ImageView ivView;

        public LocalRecipeViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.localitemTvRecipeName);
            ivView = itemView.findViewById(R.id.localitemIvRead);
        }
    }

    public LocalRecipeAdapter(Context context, SQLiteDatabaseHelper db, int searchCode, List<LocalRecipe> list) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mDb = db;
        mSearchCode = searchCode;
        mLrList = list;
    }

    @NonNull
    @Override
    public LocalRecipeAdapter.LocalRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.localitem_list, parent, false);
        return new LocalRecipeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LocalRecipeAdapter.LocalRecipeViewHolder holder, int position) {
        current = mLrList.get(position);
        final int currId = current.getId();
        holder.tvTitle.setText(current.getName());
        holder.ivView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent readIntent = new Intent(mContext, ViewLocalRecipe.class);
                readIntent.putExtra("LocalPosition", currId);
                mContext.startActivity(readIntent);
                ((Activity)mContext).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLrList.size();
    }
}
