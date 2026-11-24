package com.optic.socialmediagamer.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.optic.socialmediagamer.R;
import com.optic.socialmediagamer.activities.ChatActivity;
import com.optic.socialmediagamer.models.LFGPost;
import com.optic.socialmediagamer.providers.AuthProvider;
import com.optic.socialmediagamer.providers.LFGProvider;
import com.optic.socialmediagamer.utils.RelativeTime;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class LFGAdapter extends FirestoreRecyclerAdapter<LFGPost, LFGAdapter.ViewHolder> {

    private Context context;
    private AuthProvider mAuthProvider;
    private LFGProvider mLFGProvider;

    public LFGAdapter(@NonNull FirestoreRecyclerOptions<LFGPost> options, Context context) {
        super(options);
        this.context = context;
        this.mAuthProvider = new AuthProvider();
        this.mLFGProvider = new LFGProvider();
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull final LFGPost post) {
        if (post.getUsername() != null && !post.getUsername().isEmpty()) {
            holder.textViewUsername.setText(post.getUsername());
        } else {
            holder.textViewUsername.setText("Gamer");
        }

        holder.textViewTime.setText(RelativeTime.getTimeAgo(post.getTimestamp(), context));

        if (post.getUserPhoto() != null && !post.getUserPhoto().isEmpty()) {
            Picasso.with(context).load(post.getUserPhoto()).into(holder.circleImageUser);
        } else {
            holder.circleImageUser.setImageResource(R.drawable.ic_person);
        }

        holder.textViewGame.setText(post.getGame() != null ? post.getGame() : "Juego");
        holder.textViewPlatform.setText(post.getPlatform() != null ? post.getPlatform() : "PC");
        holder.textViewRank.setText(post.getRank() != null ? post.getRank() : "Casual");

        if (post.isMicRequired()) {
            holder.textViewMic.setText("🎙️ Con Micro");
            holder.textViewMic.setTextColor(context.getResources().getColor(R.color.colorBlue));
        } else {
            holder.textViewMic.setText("🔇 Sin Micro");
            holder.textViewMic.setTextColor(context.getResources().getColor(R.color.colorBlack));
        }

        holder.textViewDescription.setText(post.getDescription() != null ? post.getDescription() : "");

        final String currentUid = mAuthProvider.getUid();
        if (post.getIdUser() != null && post.getIdUser().equals(currentUid)) {
            holder.imageViewDelete.setVisibility(View.VISIBLE);
            holder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setTitle("Eliminar solicitud")
                            .setMessage("¿Deseas retirar esta búsqueda de escuadrón?")
                            .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (post.getId() != null) {
                                        mLFGProvider.delete(post.getId());
                                        Toast.makeText(context, "Solicitud eliminada", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                }
            });
        } else {
            holder.imageViewDelete.setVisibility(View.GONE);
        }

        holder.buttonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post.getIdUser() != null && post.getIdUser().equals(currentUid)) {
                    Toast.makeText(context, "Esta es tu propia búsqueda de escuadrón", Toast.LENGTH_SHORT).show();
                } else if (post.getIdUser() != null) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("idUser1", currentUid);
                    intent.putExtra("idUser2", post.getIdUser());
                    context.startActivity(intent);
                }
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_lfg, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageUser;
        TextView textViewUsername;
        TextView textViewTime;
        TextView textViewGame;
        TextView textViewPlatform;
        TextView textViewRank;
        TextView textViewMic;
        TextView textViewDescription;
        Button buttonJoin;
        ImageView imageViewDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            circleImageUser = itemView.findViewById(R.id.circleImageLFGUser);
            textViewUsername = itemView.findViewById(R.id.textViewLFGUsername);
            textViewTime = itemView.findViewById(R.id.textViewLFGTime);
            textViewGame = itemView.findViewById(R.id.textViewLFGGame);
            textViewPlatform = itemView.findViewById(R.id.textViewLFGPlatform);
            textViewRank = itemView.findViewById(R.id.textViewLFGRank);
            textViewMic = itemView.findViewById(R.id.textViewLFGMic);
            textViewDescription = itemView.findViewById(R.id.textViewLFGDescription);
            buttonJoin = itemView.findViewById(R.id.buttonLFGJoin);
            imageViewDelete = itemView.findViewById(R.id.imageViewLFGDelete);
        }
    }
}
