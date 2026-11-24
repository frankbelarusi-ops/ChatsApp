package com.optic.socialmediagamer.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.UploadTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.optic.socialmediagamer.R;
import com.optic.socialmediagamer.activities.MainActivity;
import com.optic.socialmediagamer.activities.PostActivity;
import com.optic.socialmediagamer.adapters.PostsAdapter;
import com.optic.socialmediagamer.adapters.StoriesAdapter;
import com.optic.socialmediagamer.models.Post;
import com.optic.socialmediagamer.models.Story;
import com.optic.socialmediagamer.providers.AuthProvider;
import com.optic.socialmediagamer.providers.ImageProvider;
import com.optic.socialmediagamer.providers.PostProvider;
import com.optic.socialmediagamer.providers.StoriesProvider;
import com.optic.socialmediagamer.providers.UsersProvider;
import com.optic.socialmediagamer.utils.FileUtil;

import java.io.File;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements MaterialSearchBar.OnSearchActionListener {

    View mView;
    FloatingActionButton mFab;
    MaterialSearchBar mSearchBar;

    AuthProvider mAuthProvider;
    RecyclerView mRecyclerView;
    PostProvider mPostProvider;
    PostsAdapter mPostsAdapter;
    PostsAdapter mPostsAdapterSearch;

    RecyclerView mRecyclerViewStories;
    ImageView mImageViewAddStory;
    StoriesProvider mStoriesProvider;
    StoriesAdapter mStoriesAdapter;
    ImageProvider mImageProvider;
    UsersProvider mUsersProvider;

    private static final int GALLERY_STORY_REQUEST_CODE = 105;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_home, container, false);
        mFab = mView.findViewById(R.id.fab);
        mRecyclerView = mView.findViewById(R.id.recyclerViewHome);
        mSearchBar = mView.findViewById(R.id.searchBar);

        mRecyclerViewStories = mView.findViewById(R.id.recyclerViewStories);
        mImageViewAddStory = mView.findViewById(R.id.imageViewAddStory);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        LinearLayoutManager storyLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewStories.setLayoutManager(storyLayoutManager);

        setHasOptionsMenu(true);
        mAuthProvider = new AuthProvider();
        mPostProvider = new PostProvider();
        mStoriesProvider = new StoriesProvider();
        mImageProvider = new ImageProvider();
        mUsersProvider = new UsersProvider();

        mSearchBar.setOnSearchActionListener(this);
        mSearchBar.inflateMenu(R.menu.main_menu);
        mSearchBar.getMenu().setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.itemLogout) {
                    logout();
                }
                return true;
            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPost();
            }
        });

        mImageViewAddStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGalleryForStory();
            }
        });

        return mView;
    }

    private void openGalleryForStory() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_STORY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_STORY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadStoryImage(imageUri);
            }
        }
    }

    private void uploadStoryImage(Uri uri) {
        try {
            File imageFile = FileUtil.from(getContext(), uri);
            Toast.makeText(getContext(), "Subiendo historia...", Toast.LENGTH_SHORT).show();
            mImageProvider.save(getContext(), imageFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri downloadUri) {
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

                                        long now = new Date().getTime();
                                        long expire = now + (24 * 60 * 60 * 1000);

                                        Story story = new Story();
                                        story.setIdUser(currentUid);
                                        story.setUsername(username);
                                        story.setUserPhoto(userPhoto);
                                        story.setImageUrl(downloadUri.toString());
                                        story.setCaption("");
                                        story.setTimestamp(now);
                                        story.setExpireTimestamp(expire);

                                        mStoriesProvider.create(story).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getContext(), "Historia publicada (24h)", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getContext(), "Error al publicar historia", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Error al subir imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al procesar archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void searchByTitle(String title) {
        Query query = mPostProvider.getPostByTitle(title);
        FirestoreRecyclerOptions<Post> options =
                new FirestoreRecyclerOptions.Builder<Post>()
                        .setQuery(query, Post.class)
                        .build();
        mPostsAdapterSearch = new PostsAdapter(options, getContext());
        mPostsAdapterSearch.notifyDataSetChanged();
        mRecyclerView.setAdapter(mPostsAdapterSearch);
        mPostsAdapterSearch.startListening();
    }

    private void getAllPost() {
        Query query = mPostProvider.getAll();
        FirestoreRecyclerOptions<Post> options =
                new FirestoreRecyclerOptions.Builder<Post>()
                        .setQuery(query, Post.class)
                        .build();
        mPostsAdapter = new PostsAdapter(options, getContext());
        mPostsAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mPostsAdapter);
        mPostsAdapter.startListening();
    }

    private void getStories() {
        Query query = mStoriesProvider.getActiveStories();
        FirestoreRecyclerOptions<Story> options =
                new FirestoreRecyclerOptions.Builder<Story>()
                        .setQuery(query, Story.class)
                        .build();
        mStoriesAdapter = new StoriesAdapter(options, getContext());
        mRecyclerViewStories.setAdapter(mStoriesAdapter);
        mStoriesAdapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        getAllPost();
        getStories();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mPostsAdapter != null) {
            mPostsAdapter.stopListening();
        }
        if (mPostsAdapterSearch != null) {
            mPostsAdapterSearch.stopListening();
        }
        if (mStoriesAdapter != null) {
            mStoriesAdapter.stopListening();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPostsAdapter != null && mPostsAdapter.getListener() != null) {
            mPostsAdapter.getListener().remove();
        }
    }

    private void goToPost() {
        Intent intent = new Intent(getContext(), PostActivity.class);
        startActivity(intent);
    }

    private void logout() {
        mAuthProvider.logout();
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        if (!enabled) {
            getAllPost();
        }
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        searchByTitle(text.toString().toLowerCase());
    }

    @Override
    public void onButtonClicked(int buttonCode) {

    }
}
