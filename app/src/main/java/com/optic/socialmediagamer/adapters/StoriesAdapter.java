package com.optic.socialmediagamer.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.optic.socialmediagamer.R;
import com.optic.socialmediagamer.activities.StoryViewerActivity;
import com.optic.socialmediagamer.models.Story;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoriesAdapter extends FirestoreRecyclerAdapter<Story, StoriesAdapter.ViewHolder> {

    private Context context;

    public StoriesAdapter(@NonNull FirestoreRecyclerOptions<Story> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull final Story story) {
        if (story.getUsername() != null && !story.getUsername().isEmpty()) {
            holder.textViewUsername.setText(story.getUsername());
        } else {
            holder.textViewUsername.setText("Gamer");
        }

        if (story.getUserPhoto() != null && !story.getUserPhoto().isEmpty()) {
            Picasso.with(context).load(story.getUserPhoto()).into(holder.circleImageUser);
        } else {
            holder.circleImageUser.setImageResource(R.drawable.ic_person);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StoryViewerActivity.class);
                intent.putExtra("username", story.getUsername());
                intent.putExtra("userPhoto", story.getUserPhoto());
                intent.putExtra("imageUrl", story.getImageUrl());
                intent.putExtra("caption", story.getCaption());
                intent.putExtra("timestamp", story.getTimestamp());
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageUser;
        ImageView imageAddBadge;
        TextView textViewUsername;

        public ViewHolder(View itemView) {
            super(itemView);
            circleImageUser = itemView.findViewById(R.id.circleImageStoryUser);
            imageAddBadge = itemView.findViewById(R.id.imageViewAddStoryBadge);
            textViewUsername = itemView.findViewById(R.id.textViewStoryUsername);
        }
    }
}
