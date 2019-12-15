package com.example.cleanblog.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cleanblog.models.Post;
import com.example.cleanblog.models.User;
import com.example.cleanblog.models.UserProfileImage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ProfileViewModel extends ViewModel {

    private User user;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public ProfileViewModel(User user) {
        this.user = user;
    }

    private MutableLiveData<String> profileImageUrl;
    public LiveData<String> getProfileImageUrl(){
        if(profileImageUrl == null){
            profileImageUrl = new MutableLiveData<>();
            loadProfileImageUrl();
        }
        return profileImageUrl;
    }
    private void loadProfileImageUrl() {
        DocumentReference docRef = db.collection("UserProfileImages").document(user.getUserId());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        UserProfileImage image = document.toObject(UserProfileImage.class);
                        profileImageUrl.postValue(image.getProfileImageUrl());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
    public void refreshProfileImageUrl(){
        loadProfileImageUrl();
    }

    private MutableLiveData<String> numberOfPosts;
    public LiveData<String> getNumberOfPosts(){
        if(numberOfPosts == null){
            numberOfPosts = new MutableLiveData<>();
            loadNumberPosts();
        }
        return numberOfPosts;
    }
    private void loadNumberPosts() {
        db.collection("posts")
                .whereEqualTo("user",user)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        numberOfPosts.setValue(String.valueOf(task.getResult().size()));
                    }
                });
    }


    private MutableLiveData<String> numberOfFollowers;
    public LiveData<String> getNumberOfFollowers(){
        if(numberOfFollowers == null){
            numberOfFollowers = new MutableLiveData<>();
            loadNumberFollowers();
        }
        return numberOfFollowers;
    }
    private void loadNumberFollowers() {
        db.collection("follow")
                .whereEqualTo("followingUserId",user.getUserId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            numberOfFollowers.setValue(String.valueOf(task.getResult().size()));
                        }
                    }
                });
    }

    private MutableLiveData<String> numberOfFollowings;
    public LiveData<String> getNumberOfFollowings(){
        if(numberOfFollowings == null){
            numberOfFollowings = new MutableLiveData<>();
            loadNumberOfFollowings();
        }
        return numberOfFollowings;
    }

    private void loadNumberOfFollowings() {
        db.collection("follow")
                .whereEqualTo("userId",user.getUserId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            numberOfFollowings.setValue(String.valueOf(task.getResult().size()));
                        }
                    }
                });
    }

    private MutableLiveData<ArrayList<Post>> posts;
    public LiveData<ArrayList<Post>> getAllPostsByUser(){
        if(posts == null){
            posts = new MutableLiveData<>();
            loadPosts();
        }
        return posts;
    }
    private void loadPosts() {
        final ArrayList<Post> postsList = new ArrayList<>();
        db.collection("posts")
                .whereEqualTo("user",user)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Post post = document.toObject(Post.class);
                                post.setPostId(document.getId());
                                postsList.add(post);
                            }
                            Collections.sort(postsList, new Comparator<Post>() {
                                @Override
                                public int compare(Post o1, Post o2) {
                                    Long post1 = Long.parseLong(o1.getTimeStamp());
                                    Long post2 = Long.parseLong(o2.getTimeStamp());
                                    return post2.compareTo(post1);
                                }
                            });
                            posts.postValue(postsList);
                        }
                    }
                });
    }
    public void onRefreshPost(){
        if(posts != null){
            posts.getValue().clear();
            loadPosts();
        }
    }


    public void onRefresh(){
        if(numberOfPosts != null)
            loadNumberPosts();
        if(profileImageUrl != null)
            loadProfileImageUrl();
        if(numberOfFollowings != null)
            loadNumberOfFollowings();
        if(numberOfFollowers != null)
            loadNumberFollowers();
    }
}
