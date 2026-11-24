package com.optic.socialmediagamer.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.optic.socialmediagamer.R;
import com.optic.socialmediagamer.activities.FiltersActivity;
import com.optic.socialmediagamer.adapters.LFGAdapter;
import com.optic.socialmediagamer.models.LFGPost;
import com.optic.socialmediagamer.providers.AuthProvider;
import com.optic.socialmediagamer.providers.LFGProvider;
import com.optic.socialmediagamer.providers.UsersProvider;

import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class FiltersFragment extends Fragment {

    View mView;
    CardView mCardViewPS4;
    CardView mCardViewXBOX;
    CardView mCardViewNINTENDO;
    CardView mCardViewPC;

    RecyclerView mRecyclerViewLFG;
    FloatingActionButton mFabCreateLFG;

    LFGProvider mLFGProvider;
    LFGAdapter mLFGAdapter;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;

    public FiltersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_filters, container, false);
        mCardViewPS4 = mView.findViewById(R.id.cardViewPs4);
        mCardViewXBOX = mView.findViewById(R.id.cardViewXBOX);
        mCardViewNINTENDO = mView.findViewById(R.id.cardViewNINTENDO);
        mCardViewPC = mView.findViewById(R.id.cardViewPC);

        mRecyclerViewLFG = mView.findViewById(R.id.recyclerViewLFG);
        mFabCreateLFG = mView.findViewById(R.id.fabCreateLFG);

        mLFGProvider = new LFGProvider();
        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerViewLFG.setLayoutManager(layoutManager);

        mCardViewPS4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToFilterActivity("PS4");
            }
        });

        mCardViewXBOX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToFilterActivity("XBOX");
            }
        });

        mCardViewNINTENDO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToFilterActivity("NINTENDO");
            }
        });

        mCardViewPC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToFilterActivity("PC");
            }
        });

        mFabCreateLFG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateLFGDialog();
            }
        });

        setupLFGRecycler();

        return mView;
    }

    private void setupLFGRecycler() {
        Query query = mLFGProvider.getAll();
        FirestoreRecyclerOptions<LFGPost> options =
                new FirestoreRecyclerOptions.Builder<LFGPost>()
                        .setQuery(query, LFGPost.class)
                        .build();
        mLFGAdapter = new LFGAdapter(options, getContext());
        mRecyclerViewLFG.setAdapter(mLFGAdapter);
    }

    private void showCreateLFGDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_lfg, null);
        builder.setView(dialogView);

        final Spinner spinnerGame = dialogView.findViewById(R.id.spinnerLFGGame);
        final Spinner spinnerPlatform = dialogView.findViewById(R.id.spinnerLFGPlatform);
        final Spinner spinnerRank = dialogView.findViewById(R.id.spinnerLFGRank);
        final CheckBox checkMic = dialogView.findViewById(R.id.checkBoxLFGMic);
        final EditText editDescription = dialogView.findViewById(R.id.editTextLFGDescription);

        String[] games = new String[]{"Warzone", "Valorant", "Free Fire", "League of Legends", "Fortnite", "Apex Legends", "Minecraft", "CS:GO 2", "Rocket League", "Otro"};
        String[] platforms = new String[]{"PC Master Race", "PlayStation 5", "PlayStation 4", "Xbox Series X/S", "Xbox One", "Nintendo Switch", "Móvil / Android / iOS"};
        String[] ranks = new String[]{"Casual / Chill", "Competitivo / Rango", "Torneo / Scrims", "Subir de Nivel"};

        ArrayAdapter<String> gameAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, games);
        ArrayAdapter<String> platformAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, platforms);
        ArrayAdapter<String> rankAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, ranks);

        spinnerGame.setAdapter(gameAdapter);
        spinnerPlatform.setAdapter(platformAdapter);
        spinnerRank.setAdapter(rankAdapter);

        builder.setPositiveButton("Publicar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String game = spinnerGame.getSelectedItem().toString();
                final String platform = spinnerPlatform.getSelectedItem().toString();
                final String rank = spinnerRank.getSelectedItem().toString();
                final boolean mic = checkMic.isChecked();
                final String description = editDescription.getText().toString().trim();

                final String currentUid = mAuthProvider.getUid();
                mUsersProvider.getUser(currentUid).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String username = "Gamer";
                        String userPhoto = "";
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            if (documentSnapshot.contains("username")) {
                                username = documentSnapshot.getString("username");
                            }
                            if (documentSnapshot.contains("image_profile")) {
                                userPhoto = documentSnapshot.getString("image_profile");
                            }
                        }

                        LFGPost post = new LFGPost();
                        post.setIdUser(currentUid);
                        post.setUsername(username);
                        post.setUserPhoto(userPhoto);
                        post.setGame(game);
                        post.setPlatform(platform);
                        post.setRank(rank);
                        post.setMicRequired(mic);
                        post.setDescription(description.isEmpty() ? "Buscamos miembros para escuadrón." : description);
                        post.setTimestamp(new Date().getTime());

                        mLFGProvider.create(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "¡Búsqueda de escuadrón publicada!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Error al publicar solicitud", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mLFGAdapter != null) {
            mLFGAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mLFGAdapter != null) {
            mLFGAdapter.stopListening();
        }
    }

    private void goToFilterActivity(String category) {
        Intent intent = new Intent(getContext(), FiltersActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }
}
