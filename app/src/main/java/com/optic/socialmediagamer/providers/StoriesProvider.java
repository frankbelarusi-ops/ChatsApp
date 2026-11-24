package com.optic.socialmediagamer.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.optic.socialmediagamer.models.Story;

public class StoriesProvider {

    private CollectionReference mCollection;

    public StoriesProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Stories");
    }

    public Task<Void> create(Story story) {
        DocumentReference doc = mCollection.document();
        story.setId(doc.getId());
        return doc.set(story);
    }

    public Query getActiveStories() {
        long now = System.currentTimeMillis();
        return mCollection.whereGreaterThan("expireTimestamp", now).orderBy("expireTimestamp", Query.Direction.DESCENDING);
    }

    public Task<Void> delete(String id) {
        return mCollection.document(id).delete();
    }
}
