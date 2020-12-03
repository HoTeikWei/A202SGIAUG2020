package com.example.assignment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {

    private Context mContext;
    private List<QuestionAnswer> mFAQs;

    public FAQAdapter(Context context, List<QuestionAnswer> faqList) {
        mContext = context;
        mFAQs = faqList;
    }

    @NonNull
    @Override
    public FAQViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.faq_item, parent, false);
        return new FAQViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final FAQViewHolder holder, final int position) {
        final QuestionAnswer current = mFAQs.get(position);
        holder.tvName.setText(current.getQuestion());
        holder.tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //used to generate an alert dialog with content
                AlertDialog.Builder messageDialog = new AlertDialog.Builder(v.getRootView().getContext());
                messageDialog.setTitle("Solution");
                messageDialog.setMessage(current.getAnswer());
                messageDialog.setPositiveButton("Ok", null);
                AlertDialog dialog = messageDialog.create();
                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFAQs.size();
    }

    public class FAQViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;

        public FAQViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.faqitemTvQuestion);
        }
    }
}