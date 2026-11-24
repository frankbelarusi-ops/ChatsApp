package com.optic.socialmediagamer.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.optic.socialmediagamer.models.LFGPost;

public class LFGProvider {

    private CollectionReference mCollection;

    public LFGProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("LFG");
    }

    public Task<Void> create(LFGPost post) {
        DocumentReference document = mCollection.document();
        post.setId(document.getId());
        return document.set(post);
    }

    public Query getAll() {
        return mCollection.orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public Query getByGame(String game) {
        return mCollection.whereEqualTo("game", game).orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public Task<Void> delete(String id) {
        return mCollection.document(id).delete();
    }
}
