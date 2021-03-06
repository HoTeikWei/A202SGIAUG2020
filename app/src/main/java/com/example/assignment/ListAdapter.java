package com.example.assignment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Recipe> recipeList;

    public ListAdapter(Context c, List<Recipe> recipe){
        mContext = c;
        this.recipeList = recipe;
    }

    @Override
    public int getCount() {
        if (recipeList.isEmpty()){
            return 0;
        }
        else{
            return recipeList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(mInflater == null){
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null){
            convertView = mInflater.inflate(R.layout.list_item, null);
        }

        Recipe recipe = recipeList.get(position);
        ImageView ivImg = convertView.findViewById(R.id.listitemivRecipeImg);
        TextView tvTitle = convertView.findViewById(R.id.listitemTvRecipeName);

        Picasso.get().load(recipe.getImgUrl()).placeholder(R.drawable.loading_img).fit().centerCrop().into(ivImg);
        tvTitle.setText(recipe.getName());
        return convertView;
    }
}
