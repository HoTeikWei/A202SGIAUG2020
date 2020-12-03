package com.example.assignment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.icu.util.TimeZone;
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

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context mContext;
    private LinkedList<Comment> mComments;

    public CommentAdapter(Context context, LinkedList<Comment> comments) {
        mContext = context;
        mComments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //used to link item view xml
        View v = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, final int position) {
        //assign value to item view content
        final Comment currentComment = mComments.get(position);
        holder.tvUserName.setText(currentComment.getEmailAddress());
        holder.tvComment.setText(currentComment.getText());
        SimpleDateFormat DateFor = new SimpleDateFormat("h:mm a, dd/MM/yyyy");
        Date convertDate = currentComment.getDate();
        convertDate.setTime(convertDate.getTime());
        String date = DateFor.format(convertDate);
        holder.tvDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        public TextView tvComment, tvUserName, tvDate;

        public CommentViewHolder(View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.commentitemTvUsername);
            tvComment = itemView.findViewById(R.id.commentitemTvComments);
            tvDate = itemView.findViewById(R.id.commentitemTvDate);
        }
    }
}
