package com.optic.socialmediagamer.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.optic.socialmediagamer.R;
import com.optic.socialmediagamer.activities.ChatActivity;
import com.optic.socialmediagamer.models.Message;
import com.optic.socialmediagamer.providers.AuthProvider;
import com.optic.socialmediagamer.providers.UsersProvider;
import com.optic.socialmediagamer.utils.RelativeTime;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends FirestoreRecyclerAdapter<Message, MessagesAdapter.ViewHolder> {

    Context context;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    private android.media.MediaPlayer mMediaPlayer;
    private String mCurrentPlayingUrl = "";
    private ImageView mLastPlayBtn;
    private SeekBar mLastSeekBar;
    private android.os.Handler mAudioHandler = new android.os.Handler();

    public MessagesAdapter(FirestoreRecyclerOptions<Message> options, Context context) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Message message) {
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String messageId = document.getId();

        // 1. Manejo de tipos: imagen, audio, reto 1v1 o texto
        if (message.getType() != null && message.getType().equals("image")) {
            holder.imageViewMessage.setVisibility(View.VISIBLE);
            holder.linearLayoutAudio.setVisibility(View.GONE);
            holder.linearLayoutChallenge.setVisibility(View.GONE);
            if (message.getUrl() != null && !message.getUrl().isEmpty()) {
                Picasso.with(context).load(message.getUrl()).into(holder.imageViewMessage);
            }
            if (message.getMessage() != null && !message.getMessage().isEmpty()) {
                holder.textViewMessage.setVisibility(View.VISIBLE);
                holder.textViewMessage.setText(message.getMessage());
            } else {
                holder.textViewMessage.setVisibility(View.GONE);
            }
        } else if (message.getType() != null && message.getType().equals("audio")) {
            holder.imageViewMessage.setVisibility(View.GONE);
            holder.textViewMessage.setVisibility(View.GONE);
            holder.linearLayoutChallenge.setVisibility(View.GONE);
            holder.linearLayoutAudio.setVisibility(View.VISIBLE);
            holder.textViewAudioDuration.setText(message.getDuration() != null ? message.getDuration() : "0:00");

            holder.imageViewPlayAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playAudio(message.getUrl(), holder.imageViewPlayAudio, holder.seekBarAudio);
                }
            });
        } else if (message.getType() != null && message.getType().equals("challenge")) {
            holder.imageViewMessage.setVisibility(View.GONE);
            holder.linearLayoutAudio.setVisibility(View.GONE);
            holder.textViewMessage.setVisibility(View.GONE);
            holder.linearLayoutChallenge.setVisibility(View.VISIBLE);

            String game = message.getChallengeGame() != null ? message.getChallengeGame() : "Juego";
            holder.textViewChallengeGame.setText("Juego: " + game);

            String status = message.getChallengeStatus() != null ? message.getChallengeStatus() : "pending";
            if ("pending".equals(status)) {
                holder.textViewChallengeStatus.setText("Estado: ⏳ Pendiente de respuesta");
                holder.textViewChallengeStatus.setTextColor(Color.parseColor("#FFCA28"));
                if (!message.getIdSender().equals(mAuthProvider.getUid())) {
                    holder.linearLayoutChallengeActions.setVisibility(View.VISIBLE);
                    holder.buttonChallengeAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new com.optic.socialmediagamer.providers.MessagesProvider().updateChallengeStatus(messageId, "accepted", "");
                        }
                    });
                    holder.buttonChallengeDecline.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new com.optic.socialmediagamer.providers.MessagesProvider().updateChallengeStatus(messageId, "declined", "");
                        }
                    });
                } else {
                    holder.linearLayoutChallengeActions.setVisibility(View.GONE);
                }
            } else if ("accepted".equals(status)) {
                holder.textViewChallengeStatus.setText("Estado: ⚔️ ¡Aceptado! En combate");
                holder.textViewChallengeStatus.setTextColor(Color.parseColor("#4CAF50"));
                holder.linearLayoutChallengeActions.setVisibility(View.GONE);
            } else if ("declined".equals(status)) {
                holder.textViewChallengeStatus.setText("Estado: ❌ Reto rechazado");
                holder.textViewChallengeStatus.setTextColor(Color.parseColor("#E53935"));
                holder.linearLayoutChallengeActions.setVisibility(View.GONE);
            }
        } else {
            holder.imageViewMessage.setVisibility(View.GONE);
            holder.linearLayoutAudio.setVisibility(View.GONE);
            holder.linearLayoutChallenge.setVisibility(View.GONE);
            holder.textViewMessage.setVisibility(View.VISIBLE);
            holder.textViewMessage.setText(message.getMessage());
        }

        // 2. Reacciones con emojis
        if (message.getReaction() != null && !message.getReaction().isEmpty()) {
            holder.textViewReaction.setVisibility(View.VISIBLE);
            holder.textViewReaction.setText(message.getReaction());
        } else {
            holder.textViewReaction.setVisibility(View.GONE);
        }

        String relativeTime = RelativeTime.timeFormatAMPM(message.getTimestamp(), context);
        holder.textViewDate.setText(relativeTime);

        // 3. Estilos de burbuja (Emisor vs Receptor)
        if (message.getIdSender().equals(mAuthProvider.getUid())) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(150, 0, 0,0);
            holder.linearLayoutMessage.setLayoutParams(params);
            holder.linearLayoutMessage.setPadding(30, 20, 20, 20);
            holder.linearLayoutMessage.setBackground(context.getResources().getDrawable(R.drawable.rounded_linear_layout));
            holder.imageViewViewed.setVisibility(View.VISIBLE);
            holder.textViewMessage.setTextColor(Color.WHITE);
            holder.textViewDate.setTextColor(Color.LTGRAY);
        }
        else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.setMargins(0, 0, 150,0);
            holder.linearLayoutMessage.setLayoutParams(params);
            holder.linearLayoutMessage.setPadding(30, 20, 30, 20);
            holder.linearLayoutMessage.setBackground(context.getResources().getDrawable(R.drawable.rounded_linear_layout_grey));
            holder.imageViewViewed.setVisibility(View.GONE);
            holder.textViewMessage.setTextColor(Color.DKGRAY);
            holder.textViewDate.setTextColor(Color.LTGRAY);
        }

        // 4. Click prolongado para Reacciones o Eliminar
        holder.viewHolder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showReactionAndOptionsDialog(message, messageId);
                return true;
            }
        });

        if (message.isViewed()) {
            holder.imageViewViewed.setImageResource(R.drawable.icon_check_blue_light);
        }
        else {
            holder.imageViewViewed.setImageResource(R.drawable.icon_check_grey);
        }

    }

    private void playAudio(String audioUrl, final ImageView playBtn, final SeekBar seekBar) {
        if (audioUrl == null || audioUrl.isEmpty()) return;

        try {
            if (mMediaPlayer != null && mCurrentPlayingUrl.equals(audioUrl)) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    playBtn.setImageResource(R.drawable.ic_play_arrow);
                    return;
                } else {
                    mMediaPlayer.start();
                    playBtn.setImageResource(R.drawable.ic_pause);
                    updateSeekBar(seekBar);
                    return;
                }
            }

            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                if (mLastPlayBtn != null) {
                    mLastPlayBtn.setImageResource(R.drawable.ic_play_arrow);
                }
                if (mLastSeekBar != null) {
                    mLastSeekBar.setProgress(0);
                }
            }

            mMediaPlayer = new android.media.MediaPlayer();
            mMediaPlayer.setDataSource(audioUrl);
            mMediaPlayer.prepareAsync();
            mCurrentPlayingUrl = audioUrl;
            mLastPlayBtn = playBtn;
            mLastSeekBar = seekBar;

            mMediaPlayer.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(android.media.MediaPlayer mp) {
                    mp.start();
                    playBtn.setImageResource(R.drawable.ic_pause);
                    seekBar.setMax(mp.getDuration());
                    updateSeekBar(seekBar);
                }
            });

            mMediaPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(android.media.MediaPlayer mp) {
                    playBtn.setImageResource(R.drawable.ic_play_arrow);
                    seekBar.setProgress(0);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSeekBar(final SeekBar seekBar) {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            seekBar.setProgress(mMediaPlayer.getCurrentPosition());
            mAudioHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateSeekBar(seekBar);
                }
            }, 100);
        }
    }

    private void showReactionAndOptionsDialog(final Message message, final String messageId) {
        boolean isMine = message.getIdSender().equals(mAuthProvider.getUid());
        final CharSequence[] options;
        if (isMine) {
            options = new CharSequence[]{"❤️ Me encanta", "😂 Me divierte", "🔥 Épico", "👍 Me gusta", "😮 Sorprendente", "😢 Triste", "🗑️ Eliminar mensaje"};
        } else {
            options = new CharSequence[]{"❤️ Me encanta", "😂 Me divierte", "🔥 Épico", "👍 Me gusta", "😮 Sorprendente", "😢 Triste"};
        }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Opciones del Mensaje");
        builder.setItems(options, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                if (which < 6) {
                    String emoji = "";
                    switch (which) {
                        case 0: emoji = "❤️"; break;
                        case 1: emoji = "😂"; break;
                        case 2: emoji = "🔥"; break;
                        case 3: emoji = "👍"; break;
                        case 4: emoji = "😮"; break;
                        case 5: emoji = "😢"; break;
                    }
                    new com.optic.socialmediagamer.providers.MessagesProvider().updateReaction(messageId, emoji);
                } else if (which == 6) {
                    new com.optic.socialmediagamer.providers.MessagesProvider().delete(messageId);
                }
            }
        });
        builder.show();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_message, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;
        TextView textViewDate;
        TextView textViewReaction;
        ImageView imageViewViewed;
        ImageView imageViewMessage;
        LinearLayout linearLayoutMessage;
        LinearLayout linearLayoutAudio;
        ImageView imageViewPlayAudio;
        SeekBar seekBarAudio;
        TextView textViewAudioDuration;
        LinearLayout linearLayoutChallenge;
        TextView textViewChallengeGame;
        TextView textViewChallengeStatus;
        LinearLayout linearLayoutChallengeActions;
        Button buttonChallengeAccept;
        Button buttonChallengeDecline;
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            textViewMessage = view.findViewById(R.id.textViewMessage);
            textViewDate = view.findViewById(R.id.textViewDateMessage);
            textViewReaction = view.findViewById(R.id.textViewReaction);
            imageViewViewed = view.findViewById(R.id.imageViewViewedMessage);
            imageViewMessage = view.findViewById(R.id.imageViewMessage);
            linearLayoutMessage = view.findViewById(R.id.linearLayoutMessage);
            linearLayoutAudio = view.findViewById(R.id.linearLayoutAudio);
            imageViewPlayAudio = view.findViewById(R.id.imageViewPlayAudio);
            seekBarAudio = view.findViewById(R.id.seekBarAudio);
            textViewAudioDuration = view.findViewById(R.id.textViewAudioDuration);
            linearLayoutChallenge = view.findViewById(R.id.linearLayoutChallenge);
            textViewChallengeGame = view.findViewById(R.id.textViewChallengeGame);
            textViewChallengeStatus = view.findViewById(R.id.textViewChallengeStatus);
            linearLayoutChallengeActions = view.findViewById(R.id.linearLayoutChallengeActions);
            buttonChallengeAccept = view.findViewById(R.id.buttonChallengeAccept);
            buttonChallengeDecline = view.findViewById(R.id.buttonChallengeDecline);
            viewHolder = view;
        }
    }

}
