package com.optic.socialmediagamer.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.optic.socialmediagamer.R;
import com.optic.socialmediagamer.adapters.ChatsAdapter;
import com.optic.socialmediagamer.models.Chat;
import com.optic.socialmediagamer.providers.AuthProvider;
import com.optic.socialmediagamer.providers.ChatsProvider;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    ChatsAdapter mAdapter;
    RecyclerView mRecyclerView;
    View mView;

    ChatsProvider mChatsProvider;
    AuthProvider mAuthProvider;

    Toolbar mToolbar;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_chats, container, false);
        mRecyclerView = mView.findViewById(R.id.recyclerViewChats);
        mToolbar = mView.findViewById(R.id.toolbar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Chats");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mChatsProvider = new ChatsProvider();
        mAuthProvider = new AuthProvider();

        com.google.android.material.floatingactionbutton.FloatingActionButton fab = mView.findViewById(R.id.fabCreateGroup);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGroupDialog();
            }
        });

        return mView;
    }

    private void showCreateGroupDialog() {
        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("Nombre del grupo (ej. Clan Gaming, Squad)");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Crear Nuevo Grupo");
        builder.setMessage("Ingresa el nombre del grupo para tu comunidad o clan:");
        builder.setView(input);

        builder.setPositiveButton("Crear", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                String groupName = input.getText().toString().trim();
                if (!groupName.isEmpty()) {
                    createGroup(groupName);
                } else {
                    android.widget.Toast.makeText(getContext(), "Ingresa un nombre para el grupo", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void createGroup(final String groupName) {
        final String groupId = java.util.UUID.randomUUID().toString();
        Chat chat = new Chat();
        chat.setId(groupId);
        chat.setGroup(true);
        chat.setGroupName(groupName);
        chat.setIdAdmin(mAuthProvider.getUid());
        chat.setIdUser1(mAuthProvider.getUid());
        chat.setIdUser2("");
        chat.setTimestamp(new java.util.Date().getTime());

        java.util.ArrayList<String> ids = new java.util.ArrayList<>();
        ids.add(mAuthProvider.getUid());
        chat.setIds(ids);

        java.util.Random random = new java.util.Random();
        chat.setIdNotification(random.nextInt(1000000));

        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("Chats").document(groupId).set(chat)
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        android.widget.Toast.makeText(getContext(), "¡Grupo " + groupName + " creado!", android.widget.Toast.LENGTH_SHORT).show();
                        android.content.Intent intent = new android.content.Intent(getContext(), com.optic.socialmediagamer.activities.ChatActivity.class);
                        intent.putExtra("idChat", groupId);
                        intent.putExtra("idUser1", mAuthProvider.getUid());
                        intent.putExtra("idUser2", "");
                        startActivity(intent);
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = mChatsProvider.getAll(mAuthProvider.getUid());
        FirestoreRecyclerOptions<Chat> options =
                new FirestoreRecyclerOptions.Builder<Chat>()
                        .setQuery(query, Chat.class)
                        .build();
        mAdapter = new ChatsAdapter(options, getContext());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter.getListener() != null) {
            mAdapter.getListener().remove();
        }
        if (mAdapter.getListenerLastMessage() != null) {
            mAdapter.getListenerLastMessage().remove();
        }
    }

}
