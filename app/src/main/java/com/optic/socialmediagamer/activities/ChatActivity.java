package com.optic.socialmediagamer.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.optic.socialmediagamer.R;
import com.optic.socialmediagamer.adapters.MessagesAdapter;
import com.optic.socialmediagamer.models.Chat;
import com.optic.socialmediagamer.models.FCMBody;
import com.optic.socialmediagamer.models.FCMResponse;
import com.optic.socialmediagamer.models.Message;
import com.optic.socialmediagamer.providers.AuthProvider;
import com.optic.socialmediagamer.providers.ChatsProvider;
import com.optic.socialmediagamer.providers.MessagesProvider;
import com.optic.socialmediagamer.providers.NotificationProvider;
import com.optic.socialmediagamer.providers.TokenProvider;
import com.optic.socialmediagamer.providers.UsersProvider;
import com.optic.socialmediagamer.utils.RelativeTime;
import com.optic.socialmediagamer.utils.ViewedMessageHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    String mExtraIdUser1;
    String mExtraIdUser2;
    String mExtraIdChat;

    long mIdNotificationChat;

    ChatsProvider mChatsProvider;
    MessagesProvider mMessagesProvider;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    NotificationProvider mNotificationProvider;
    TokenProvider mTokenProvider;

    EditText mEditTextMessage;
    ImageView mImageViewSendMessage;
    ImageView mImageViewSelectImage;
    ImageView mImageViewSendChallenge;
    ImageView mImageViewMic;
    com.optic.socialmediagamer.providers.ImageProvider mImageProvider;
    android.app.AlertDialog mDialog;
    private final int GALLERY_REQUEST_CODE = 100;
    private final int RECORD_AUDIO_REQUEST_CODE = 200;
    java.io.File mImageFile;
    private android.media.MediaRecorder mMediaRecorder;
    private String mAudioFileName = "";
    private long mRecordStartTime = 0;

    CircleImageView mCircleImageProfile;
    TextView mTextViewUsername;
    TextView mTextViewRelativeTime;
    ImageView mImageViewBack;
    RecyclerView mRecyclerViewMessage;

    MessagesAdapter mAdapter;

    View mActionBarView;

    LinearLayoutManager mLinearLayoutManager;

    ListenerRegistration mListener;

    String mMyUsername;
    String mUsernameChat;
    String mImageReceiver = "";
    String mImageSender = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatsProvider = new ChatsProvider();
        mMessagesProvider = new MessagesProvider();
        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();
        mNotificationProvider = new NotificationProvider();
        mTokenProvider = new TokenProvider();
        mImageProvider = new com.optic.socialmediagamer.providers.ImageProvider();
        mDialog = new dmax.dialog.SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Enviando imagen...")
                .setCancelable(false).build();

        mEditTextMessage = findViewById(R.id.editTextMessage);
        mImageViewSendMessage = findViewById(R.id.imageViewSendMessage);
        mImageViewSelectImage = findViewById(R.id.imageViewSelectImage);
        mImageViewSendChallenge = findViewById(R.id.imageViewSendChallenge);
        mImageViewMic = findViewById(R.id.imageViewMic);
        mRecyclerViewMessage = findViewById(R.id.recyclerViewMessage);

        mLinearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerViewMessage.setLayoutManager(mLinearLayoutManager);

        mExtraIdUser1 = getIntent().getStringExtra("idUser1");
        mExtraIdUser2 = getIntent().getStringExtra("idUser2");
        mExtraIdChat  = getIntent().getStringExtra("idChat");

        showCustomToolbar(R.layout.custom_chat_toolbar);
        getMyInfoUser();

        mImageViewSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        mImageViewSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        mImageViewSendChallenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChallengeDialog();
            }
        });

        mEditTextMessage.addTextChangedListener(new android.text.TextWatcher() {
            private android.os.Handler handler = new android.os.Handler();
            private Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (mExtraIdChat != null && !mExtraIdChat.isEmpty()) {
                        mChatsProvider.updateWriting(mExtraIdChat, "");
                    }
                }
            };

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    mImageViewSendMessage.setVisibility(View.VISIBLE);
                    mImageViewMic.setVisibility(View.GONE);
                } else {
                    mImageViewSendMessage.setVisibility(View.GONE);
                    mImageViewMic.setVisibility(View.VISIBLE);
                }

                if (mExtraIdChat != null && !mExtraIdChat.isEmpty()) {
                    mChatsProvider.updateWriting(mExtraIdChat, mAuthProvider.getUid());
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 2000);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        mImageViewMic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(ChatActivity.this, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    androidx.core.app.ActivityCompat.requestPermissions(ChatActivity.this, new String[]{android.Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
                    return false;
                }

                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    startRecordingAudio();
                    Toast.makeText(ChatActivity.this, "Grabando audio... Suelta para enviar", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (event.getAction() == android.view.MotionEvent.ACTION_UP || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                    stopRecordingAudio();
                    return true;
                }
                return false;
            }
        });

        checkIfChatExist();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
        ViewedMessageHelper.updateOnline(true, ChatActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, ChatActivity.this);
    }


    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mListener.remove();
        }
    }

    private void getMessageChat() {
        Query query = mMessagesProvider.getMessageByChat(mExtraIdChat);
        FirestoreRecyclerOptions<Message> options =
                new FirestoreRecyclerOptions.Builder<Message>()
                        .setQuery(query, Message.class)
                        .build();
        mAdapter = new MessagesAdapter(options, ChatActivity.this);
        mRecyclerViewMessage.setAdapter(mAdapter);
        mAdapter.startListening();
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateViewed();
                int numberMessage = mAdapter.getItemCount();
                int lastMessagePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastMessagePosition == -1 || (positionStart >= (numberMessage -1) && lastMessagePosition == (positionStart - 1))) {
                    mRecyclerViewMessage.scrollToPosition(positionStart);
                }
            }
        });
    }

    private void sendMessage() {
        String textMessage = mEditTextMessage.getText().toString();
        if (!textMessage.isEmpty()) {
            final Message message = new Message();
            message.setIdChat(mExtraIdChat);
            if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
                message.setIdSender(mExtraIdUser1);
                message.setIdReceiver(mExtraIdUser2);
            }
            else {
                message.setIdSender(mExtraIdUser2);
                message.setIdReceiver(mExtraIdUser1);
            }
            message.setTimestamp(new Date().getTime());
            message.setViewed(false);
            message.setIdChat(mExtraIdChat);
            message.setMessage(textMessage);

            mMessagesProvider.create(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mEditTextMessage.setText("");
                        mAdapter.notifyDataSetChanged();
                        getToken(message);
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "El mensaje no se pudo crear", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void showCustomToolbar(int resource) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActionBarView = inflater.inflate(resource, null);
        actionBar.setCustomView(mActionBarView);
        mCircleImageProfile = mActionBarView.findViewById(R.id.circleImageProfile);
        mTextViewUsername = mActionBarView.findViewById(R.id.textViewUsername);
        mTextViewRelativeTime = mActionBarView.findViewById(R.id.textViewRelativeTime);
        mImageViewBack = mActionBarView.findViewById(R.id.imageViewBack);

        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getUserInfo();

    }

    private void getUserInfo() {
        if (mExtraIdUser2 == null || mExtraIdUser2.isEmpty()) {
            if (mExtraIdChat != null) {
                mChatsProvider.getChatById(mExtraIdChat).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        if (doc != null && doc.exists()) {
                            if (doc.contains("groupName")) {
                                mTextViewUsername.setText(doc.getString("groupName"));
                                mTextViewRelativeTime.setText("Grupo");
                            }
                        }
                    }
                });
            }
            return;
        }

        String idUserInfo = "";
        if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
            idUserInfo = mExtraIdUser2;
        }
        else {
            idUserInfo = mExtraIdUser1;
        }

        mListener = mUsersProvider.getUserRealtime(idUserInfo).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("username")) {
                        mUsernameChat = documentSnapshot.getString("username");
                        mTextViewUsername.setText(mUsernameChat);
                    }
                    if (documentSnapshot.contains("online")) {
                        boolean online = documentSnapshot.getBoolean("online");
                        if (online) {
                            mTextViewRelativeTime.setText("En linea");
                        }
                        else if (documentSnapshot.contains("lastConnect")) {
                            long lastConnect = documentSnapshot.getLong("lastConnect");
                            String relativeTime = RelativeTime.getTimeAgo(lastConnect, ChatActivity.this);
                            mTextViewRelativeTime.setText(relativeTime);
                        }
                    }
                    if (documentSnapshot.contains("image_profile")) {
                        mImageReceiver = documentSnapshot.getString("image_profile");
                        if (mImageReceiver != null) {
                            if (!mImageReceiver.equals("")) {
                                Picasso.with(ChatActivity.this).load(mImageReceiver).into(mCircleImageProfile);
                            }
                        }
                    }
                }
            }
        });
    }

    private void checkIfChatExist() {
        mChatsProvider.getChatByUser1AndUser2(mExtraIdUser1, mExtraIdUser2).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int size = queryDocumentSnapshots.size();
                if (size == 0) {
                    createChat();
                }
                else {
                    mExtraIdChat = queryDocumentSnapshots.getDocuments().get(0).getId();
                    mIdNotificationChat = queryDocumentSnapshots.getDocuments().get(0).getLong("idNotification");
                    getMessageChat();
                    updateViewed();
                    listenToWriting();
                }
            }
        });
    }

    private void updateViewed() {
        String idSender = "";

        if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
            idSender = mExtraIdUser2;
        }
        else {
            idSender = mExtraIdUser1;
        }

        mMessagesProvider.getMessagesByChatAndSender(mExtraIdChat, idSender).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    mMessagesProvider.updateViewed(document.getId(), true);
                }
            }
        });

    }

    private void createChat() {
        Chat chat = new Chat();
        chat.setIdUser1(mExtraIdUser1);
        chat.setIdUser2(mExtraIdUser2);
        chat.setWriting(false);
        chat.setTimestamp(new Date().getTime());
        chat.setId(mExtraIdUser1 + mExtraIdUser2);
        Random random = new Random();
        int n = random.nextInt(1000000);
        chat.setIdNotification(n);
        mIdNotificationChat = n;

        ArrayList<String> ids = new ArrayList<>();
        ids.add(mExtraIdUser1);
        ids.add(mExtraIdUser2);
        chat.setIds(ids);
        mChatsProvider.create(chat);
        mExtraIdChat = chat.getId();
        getMessageChat();
        listenToWriting();
    }

    private void getToken(final Message message) {
        String idUser = "";
        if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
            idUser = mExtraIdUser2;
        }
        else {
            idUser = mExtraIdUser1;
        }
        mTokenProvider.getToken(idUser).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("token")) {
                        String token = documentSnapshot.getString("token");
                        getLastThreeMessages(message, token);
                    }
                }
                else {
                    Toast.makeText(ChatActivity.this, "El token de notificaciones del usuario no existe", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getLastThreeMessages(final Message message, final String token) {
        mMessagesProvider.getLastThreeMessagesByChatAndSender(mExtraIdChat, mAuthProvider.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList<Message> messageArrayList = new ArrayList<>();

                for (DocumentSnapshot d: queryDocumentSnapshots.getDocuments()) {
                    if (d.exists()) {
                        Message message = d.toObject(Message.class);
                        messageArrayList.add(message);
                    }
                }

                if (messageArrayList.size() == 0) {
                    messageArrayList.add(message);
                }

                Collections.reverse(messageArrayList);

                Gson gson = new Gson();
                String messages = gson.toJson(messageArrayList);

               sendNotification(token, messages, message);
            }
        });
    }

    private void sendNotification(final String token, String messages, Message message) {
        final Map<String, String> data = new HashMap<>();
        data.put("title", "NUEVO MENSAJE");
        data.put("body", message.getMessage());
        data.put("idNotification", String.valueOf(mIdNotificationChat));
        data.put("messages", messages);
        data.put("usernameSender", mMyUsername.toUpperCase());
        data.put("usernameReceiver", mUsernameChat.toUpperCase());
        data.put("idSender", message.getIdSender());
        data.put("idReceiver", message.getIdReceiver());
        data.put("idChat", message.getIdChat());

        if (mImageSender.equals("")) {
            mImageSender = "IMAGEN_NO_VALIDA";
        }
        if (mImageReceiver.equals("")) {
            mImageReceiver = "IMAGEN_NO_VALIDA";
        }

        data.put("imageSender", mImageSender);
        data.put("imageReceiver", mImageReceiver);

        String idSender = "";
        if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
            idSender = mExtraIdUser2;
        }
        else {
            idSender = mExtraIdUser1;
        }

        mMessagesProvider.getLastMessageSender(mExtraIdChat, idSender).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int size = queryDocumentSnapshots.size();
                String lastMessage = "";
                if (size > 0) {
                    lastMessage = queryDocumentSnapshots.getDocuments().get(0).getString("message");
                    data.put("lastMessage", lastMessage);
                }
                FCMBody body = new FCMBody(token, "high", "4500s", data);
                mNotificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body() != null) {
                            if (response.body().getSuccess() == 1) {
                                //Toast.makeText(ChatActivity.this, "La notificacion se envio correcatemente", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(ChatActivity.this, "La notificacion no se pudo enviar", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(ChatActivity.this, "La notificacion no se pudo enviar", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
            }
        });

    }

    private void getMyInfoUser() {
        mUsersProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("username")) {
                        mMyUsername = documentSnapshot.getString("username");
                    }
                    if (documentSnapshot.contains("image_profile")) {
                        mImageSender = documentSnapshot.getString("image_profile");
                    }
                }
            }
        });
    }

    private void listenToWriting() {
        if (mExtraIdChat != null && !mExtraIdChat.isEmpty()) {
            mChatsProvider.getChatById(mExtraIdChat).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        if (documentSnapshot.contains("writing")) {
                            String writing = documentSnapshot.getString("writing");
                            if (writing != null && !writing.isEmpty() && !writing.equals(mAuthProvider.getUid())) {
                                mTextViewRelativeTime.setText("Escribiendo...");
                                return;
                            } else {
                                getUserInfo();
                            }
                        }
                    }
                }
            });
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                mImageFile = com.optic.socialmediagamer.utils.FileUtil.from(this, data.getData());
                sendImageMessage();
            } catch (Exception e) {
                Toast.makeText(this, "Error al seleccionar imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendImageMessage() {
        if (mImageFile != null && mExtraIdChat != null && !mExtraIdChat.isEmpty()) {
            mDialog.show();
            mImageProvider.save(ChatActivity.this, mImageFile).addOnCompleteListener(new OnCompleteListener<com.google.firebase.storage.UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<com.google.firebase.storage.UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<android.net.Uri>() {
                            @Override
                            public void onSuccess(android.net.Uri uri) {
                                mDialog.dismiss();
                                String downloadUrl = uri.toString();
                                final Message message = new Message();
                                message.setIdChat(mExtraIdChat);
                                if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
                                    message.setIdSender(mExtraIdUser1);
                                    message.setIdReceiver(mExtraIdUser2);
                                } else {
                                    message.setIdSender(mExtraIdUser2);
                                    message.setIdReceiver(mExtraIdUser1);
                                }
                                message.setTimestamp(new Date().getTime());
                                message.setViewed(false);
                                message.setType("image");
                                message.setUrl(downloadUrl);
                                message.setMessage("📷 Foto");

                                mMessagesProvider.create(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> taskMessage) {
                                        if (taskMessage.isSuccessful()) {
                                            mAdapter.notifyDataSetChanged();
                                            getToken(message);
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        mDialog.dismiss();
                        Toast.makeText(ChatActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void startRecordingAudio() {
        try {
            java.io.File cacheDir = getExternalCacheDir() != null ? getExternalCacheDir() : getCacheDir();
            mAudioFileName = cacheDir.getAbsolutePath() + "/audio_" + System.currentTimeMillis() + ".3gp";
            mMediaRecorder = new android.media.MediaRecorder();
            mMediaRecorder.setAudioSource(android.media.MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(android.media.MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setOutputFile(mAudioFileName);
            mMediaRecorder.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            mRecordStartTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecordingAudio() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
                long durationMs = System.currentTimeMillis() - mRecordStartTime;
                int seconds = (int) (durationMs / 1000);
                if (seconds < 1) {
                    Toast.makeText(this, "Audio muy corto", Toast.LENGTH_SHORT).show();
                    new java.io.File(mAudioFileName).delete();
                } else {
                    String durationStr = String.format(java.util.Locale.US, "%d:%02d", seconds / 60, seconds % 60);
                    uploadAndSendAudio(durationStr);
                }
            } catch (Exception e) {
                mMediaRecorder = null;
            }
        }
    }

    private void uploadAndSendAudio(final String durationStr) {
        if (mAudioFileName == null || mAudioFileName.isEmpty()) return;
        final java.io.File audioFile = new java.io.File(mAudioFileName);
        if (!audioFile.exists()) return;

        mDialog.setMessage("Enviando nota de voz...");
        mDialog.show();

        android.net.Uri fileUri = android.net.Uri.fromFile(audioFile);
        final com.google.firebase.storage.StorageReference audioRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                .getReference().child("chat_audios/" + System.currentTimeMillis() + ".3gp");

        audioRef.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<com.google.firebase.storage.UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<com.google.firebase.storage.UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    audioRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<android.net.Uri>() {
                        @Override
                        public void onSuccess(android.net.Uri uri) {
                            mDialog.dismiss();
                            String audioUrl = uri.toString();
                            final Message message = new Message();
                            message.setIdChat(mExtraIdChat);
                            if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
                                message.setIdSender(mExtraIdUser1);
                                message.setIdReceiver(mExtraIdUser2);
                            } else {
                                message.setIdSender(mExtraIdUser2);
                                message.setIdReceiver(mExtraIdUser1);
                            }
                            message.setTimestamp(new Date().getTime());
                            message.setViewed(false);
                            message.setType("audio");
                            message.setUrl(audioUrl);
                            message.setDuration(durationStr);
                            message.setMessage("🎙️ Nota de voz (" + durationStr + ")");

                            mMessagesProvider.create(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> taskMessage) {
                                    if (taskMessage.isSuccessful()) {
                                        mAdapter.notifyDataSetChanged();
                                        getToken(message);
                                    }
                                }
                            });
                        }
                    });
                } else {
                    mDialog.dismiss();
                    Toast.makeText(ChatActivity.this, "Error al enviar nota de voz", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showChallengeDialog() {
        final String[] games = new String[]{
                "EA Sports FC 24 / FIFA",
                "Call of Duty: Warzone Gulag",
                "Valorant 1v1 Custom",
                "Free Fire PvP Sala",
                "Mortal Kombat / Tekken",
                "Clash Royale 1v1",
                "Fortnite Box Fight",
                "Rocket League 1v1",
                "Otro juego"
        };

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("⚔️ Retar a un 1v1 Gamer");
        builder.setItems(games, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                sendChallenge(games[which]);
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void sendChallenge(String game) {
        if (mExtraIdChat == null || mExtraIdChat.isEmpty()) {
            Toast.makeText(this, "Espera a que se inicie el chat...", Toast.LENGTH_SHORT).show();
            return;
        }

        final Message message = new Message();
        message.setIdChat(mExtraIdChat);
        if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
            message.setIdSender(mExtraIdUser1);
            message.setIdReceiver(mExtraIdUser2);
        } else {
            message.setIdSender(mExtraIdUser2);
            message.setIdReceiver(mExtraIdUser1);
        }
        message.setTimestamp(new Date().getTime());
        message.setViewed(false);
        message.setType("challenge");
        message.setChallengeGame(game);
        message.setChallengeStatus("pending");
        message.setMessage("⚔️ ¡Reto 1v1 en " + game + "!");

        mMessagesProvider.create(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> taskMessage) {
                if (taskMessage.isSuccessful()) {
                    mAdapter.notifyDataSetChanged();
                    getToken(message);
                    Toast.makeText(ChatActivity.this, "¡Reto 1v1 enviado!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
