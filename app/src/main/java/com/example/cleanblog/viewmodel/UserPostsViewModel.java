package com.example.cleanblog.viewmodel;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cleanblog.models.Post;
import com.example.cleanblog.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class UserPostsViewModel extends ViewModel {

    private User user;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public UserPostsViewModel(User user) {
        this.user = user;
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

}
