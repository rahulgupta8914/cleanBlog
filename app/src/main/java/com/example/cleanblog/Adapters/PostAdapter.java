package com.example.cleanblog.Adapters;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.example.cleanblog.Function;

import com.example.cleanblog.MainActivity;
import com.example.cleanblog.MainAppFrags.EditFragment;
import com.example.cleanblog.MainAppFrags.OpenPostUserFragment;
import com.example.cleanblog.MainAppFrags.ProfileFragment;
import com.example.cleanblog.R;
import com.example.cleanblog.models.Comment;
import com.example.cleanblog.models.Like;
import com.example.cleanblog.models.Post;
import com.example.cleanblog.models.User;
import com.example.cleanblog.models.UserProfileImage;
import com.example.cleanblog.viewmodel.ProfileViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private FragmentManager fragmentManager;
    private Context context;
    private String userProfileImageUrl;
    private ArrayList<Post> posts;
    private User user;
    private StorageReference mStorageRef;
    private InputMethodManager imm;

    private ProfileViewModel profileViewModel;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public PostAdapter(FragmentManager fragmentManager, Context context, ArrayList<Post> posts, User user, String userProfileImageUrl) {
        this.fragmentManager = fragmentManager;
        this.context = context;
        this.posts = posts;
        this.user = user;
        this.userProfileImageUrl = userProfileImageUrl;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater  =  LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.post_container,viewGroup,false);

        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder postViewHolder, int position) {


        final Post post = posts.get(position);
        final String postUserId = post.getUser().getUserId();
        final String postUserEmail = post.getUser().getUserEmail();

        imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);

        postViewHolder.progressBar.setIndeterminate(true);
        postViewHolder.ivLike.setTag(R.drawable.ic_favorite_border_black_24dp);

        if(postUserId.equals(user.getUserId())){
            profileViewModel = ViewModelProviders.of((MainActivity)context, new ViewModelProvider.Factory(){
                @NonNull
                @Override
                public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                    return (T) new ProfileViewModel(user);
                }
            }).get(ProfileViewModel.class);
        }

        final CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(5f);
        circularProgressDrawable.start();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        postViewHolder.ivUserPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPostUser(postUserId,postUserEmail);
            }
        });
        postViewHolder.tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPostUser(postUserId,postUserEmail);
            }
        });

        getNumberLikes(post.getPostId(),postViewHolder.tvNumOfLikes);
        likeButtonActivity(post.getPostId(),postViewHolder.ivLike);
        getNumberOfComments(postViewHolder.tvViewAllComments,post.getPostId());
        // getting and setting current post information
        getAndSetCurrentPostInformation(post,postViewHolder.tvUsername,postViewHolder.tvDescription,postViewHolder.ivUserPost,postViewHolder.ivPostImage,postViewHolder.tvTags,postViewHolder.progressBar,circularProgressDrawable);

        // getting and setting current logged in user information
        setCurrentLoggedInUserInformation(user,postViewHolder.ivUserComment,circularProgressDrawable);
        postViewHolder.ivComment.setTag(1);
        postViewHolder.ivComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(postViewHolder.ivComment.getTag().equals(1)){
                    postViewHolder.ivUserComment.setVisibility(View.VISIBLE);
                    postViewHolder.etAddComment.setVisibility(View.VISIBLE);
                    postViewHolder.ivComment.setTag(0);
                }else{
                    postViewHolder.ivUserComment.setVisibility(View.GONE);
                    postViewHolder.etAddComment.setVisibility(View.GONE);
                    postViewHolder.ivComment.setTag(1);
                }
            }
        });

        postViewHolder.etAddComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)postViewHolder.etAddComment.getLayoutParams();
                if(s.toString().isEmpty()){
                    postViewHolder.ibAddComment.setVisibility(View.GONE);
                    params.setMarginEnd(16);
                    postViewHolder.etAddComment.setLayoutParams(params);
                }else{
                    postViewHolder.ibAddComment.setVisibility(View.VISIBLE);
                    params.setMarginEnd(0);
                    postViewHolder.etAddComment.setLayoutParams(params);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // add comment
        postViewHolder.ibAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Comment comment = new Comment(user.getUserId(),post.getPostId(),postViewHolder.etAddComment.getText().toString(),Function.getTime());
                addComment(comment,postViewHolder.etAddComment,postViewHolder.ivUserComment,postViewHolder.tvViewAllComments,post.getPostId());
            }
        });

        // add remove like
        postViewHolder.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRemoveLike(postViewHolder.ivLike,post.getPostId(),postViewHolder.tvNumOfLikes);
            }
        });
        //set how
        // set timestamp on view
        String postTimeStamp = post.getTimeStamp();
        Timestamp timestamp = new Timestamp(Long.parseLong(postTimeStamp));
        Date date = new Date(timestamp.getTime());
        String s = date.toString();
        System.out.println(s);
        String[] split = s.split(" ");
        String newDate ="Posted on \n"+ split[0] + ", "+split[1]+" "+split[2]+" "+split[3];
        postViewHolder.tvCreatedOn.setText(newDate);
        // menus
        setPostMenus(user.getUserId(),post,postViewHolder.tvPostMenu,position);
    }

    // getting and setting current logged in user information
    private void setCurrentLoggedInUserInformation(@NonNull final User user,
                                                   @NonNull final ImageView ivUserComment,
                                                   @NonNull final CircularProgressDrawable circularProgressDrawable) {
        if(!userProfileImageUrl.isEmpty()){
            ivUserComment.setImageTintMode(null);
            Glide.with(context).load(Uri.parse(userProfileImageUrl)).placeholder(circularProgressDrawable).into(ivUserComment);
        }
    }

    // getting and setting current post information
    private void getAndSetCurrentPostInformation(@NonNull final Post post,
                                                 @NonNull final TextView tvUsername,
                                                 @NonNull final TextView tvDescription,
                                                 @NonNull final ImageView ivUserPost,
                                                 @NonNull final ImageView ivPostImage,
                                                 @NonNull final TextView  tvTags,
                                                 @NonNull final ProgressBar progressBar,
                                                 @NonNull final CircularProgressDrawable circularProgressDrawable) {
        //setting post image
        progressBar.setVisibility(View.VISIBLE);
        Picasso.get().load(Uri.parse(post.getImageUrl())).into(ivPostImage, new Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onError(Exception e) {
            }
        });
        tvDescription.setText(Function.spiltEmailString(post.getUser().getUserEmail())+" : " + post.getDescription());
        tvUsername.setText(Function.spiltEmailString(post.getUser().getUserEmail()));

        if(post.getTags().isEmpty() || post.getTags().equals(null)){
            tvTags.setVisibility(View.GONE);
        }else{
            tvTags.setText(post.getTags());
        }


        DocumentReference postUserInformation = db.collection("UserProfileImages").document(post.getUser().getUserId());
        postUserInformation.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        UserProfileImage postUserProfileImage = document.toObject(UserProfileImage.class);
                        if(!postUserProfileImage.getProfileImageUrl().isEmpty() && postUserProfileImage.getProfileImageUrl()!=null){
                            Glide.with(context).load(Uri.parse(postUserProfileImage.getProfileImageUrl())).placeholder(circularProgressDrawable).into(ivUserPost);
                        }
                    } else {
                        Log.d(TAG, "No such document");;
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
    // set post menus
    private void setPostMenus(@NonNull final String userID,
                              @NonNull final Post post,
                              @NonNull final TextView tvPostMenu,
                              @NonNull final int position) {
        if(post.getUser().getUserId().equals(userID)){
            tvPostMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] strings = {"Edit","Delete","Turn off Commenting"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setItems(strings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch(which){
                                case 0:
                                    Toast.makeText(context, "Edit", Toast.LENGTH_SHORT).show();
                                    editPost(post);
                                    break;
                                case 1:
                                    Toast.makeText(context, "Delete", Toast.LENGTH_SHORT).show();
                                    deletePost(position,post);
                            }
                        }
                    });
                    builder.create();
                    AlertDialog dialog = builder.show();
                }
            });
        }else{
            tvPostMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] strings = {"Unfollow","Report","Turn on Post Notifications","Mute"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setItems(strings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch(which){
                                case 0:
                                    Toast.makeText(context, "Camera", Toast.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    Toast.makeText(context, "Gallery", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.create();
                    AlertDialog dialog = builder.show();
                }
            });
        }
    }
    // add comment
    private void addComment(@NonNull final Comment comment,
                            @NonNull final EditText etAddComment,
                            @NonNull final ImageView ivUserComment,
                            @NonNull final TextView tvViewAllComments,
                            @NonNull final String postId){
        db.collection("comments").document().set(comment)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        imm.hideSoftInputFromWindow(etAddComment.getWindowToken(), 0);
                        etAddComment.setText("");
                        ivUserComment.setVisibility(View.GONE);
                        etAddComment.setVisibility(View.GONE);
                        getNumberOfComments(tvViewAllComments,postId);
                        Toast.makeText(context, "Your comment added successfully..", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                imm.hideSoftInputFromWindow(etAddComment.getWindowToken(), 0);
                etAddComment.setText("");
                ivUserComment.setVisibility(View.GONE);
                etAddComment.setVisibility(View.GONE);
                Toast.makeText(context, "Unable to add your comment..check your connection", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getNumberOfComments(@NonNull final TextView tvViewAllComments,
                                     @NonNull final String postid){
        db.collection("comments")
                .whereEqualTo("postId",postid)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().size() > 0){
                        tvViewAllComments.setVisibility(View.VISIBLE);
                        if(task.getResult().size() == 1){
                            tvViewAllComments.setText("View " + task.getResult().size() + " comments");
                        }else{
                            tvViewAllComments.setText("View all " + task.getResult().size() + " comments");
                        }
                    }else{
                        tvViewAllComments.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
    private void addRemoveLike(@NonNull final ImageView ivLike,
                               @NonNull final String postid,
                               @NonNull final TextView tvNumOfLikes){
        if(ivLike.getTag().equals(R.drawable.ic_favorite_border_black_24dp)){
            //do work here
            Like like = new Like(user.getUserId(),postid,Function.getTime());
            db.collection("likes").document().set(like)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "You liked the post", Toast.LENGTH_SHORT).show();
                            ivLike.setImageResource(R.drawable.ic_favorite_black_24dp);
                            ivLike.setTag(R.drawable.ic_favorite_black_24dp);
                            getNumberLikes(postid,tvNumOfLikes);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Unable to perform your activity..check your connection", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            final CollectionReference docRef = db.collection("likes");
                    docRef.whereEqualTo("postId",postid)
                    .whereEqualTo("userId",user.getUserId())
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for (DocumentSnapshot document : task.getResult()) {
                            docRef.document(document.getId()).delete();
                            ivLike.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            ivLike.setTag(R.drawable.ic_favorite_border_black_24dp);
                            getNumberLikes(postid,tvNumOfLikes);
                            Toast.makeText(context, "Like removed", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });

        }
    }
    private void likeButtonActivity(@NonNull final String postId,
                                    @NonNull final ImageView imageView){
        db.collection("likes")
                .whereEqualTo("postId",postId)
                .whereEqualTo("userId",user.getUserId())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if(task.getResult().size()==1){
                        imageView.setImageResource(R.drawable.ic_favorite_black_24dp);
                        imageView.setTag(R.drawable.ic_favorite_black_24dp);
                    }
                }
            }
        });
    }
    private void getNumberLikes(@NonNull final String postid,
                                @NonNull final TextView tvNumOfLikes){
        db.collection("likes")
                .whereEqualTo("postId",postid)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if(task.getResult().size() > 0){
                        tvNumOfLikes.setVisibility(View.VISIBLE);
                        tvNumOfLikes.setText(task.getResult().size() +" likes");
                    }else{
                        tvNumOfLikes.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void openPostUser(String postUserId,String postUserEmail){
        BottomNavigationView view = ((Activity)context).findViewById(R.id.bottom_navigation);
        if(user.getUserId().equals(postUserId)) {
            ProfileFragment profileFragment = ProfileFragment.newInstance(user.getUserId(),user.getUserEmail());
            fragmentManager.beginTransaction().replace(R.id.frameMainApp, profileFragment).addToBackStack(null).commit();
        }else{
            fragmentManager.beginTransaction().replace(R.id.frameMainApp, OpenPostUserFragment
                    .newInstance(postUserId,
                            postUserEmail,
                            user.getUserId(),
                            user.getUserEmail(),
                            userProfileImageUrl))
                    .addToBackStack(null).commit();
            view.setVisibility(View.GONE);
        }
    }

    private void editPost(Post post){
        fragmentManager.beginTransaction().replace(R.id.frameMainApp,EditFragment.newInstance(user,post,userProfileImageUrl)).addToBackStack(null).commit();
    }

    private void deletePost(final int position, final Post post){

        db.collection("posts").document(post.getPostId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        deleteFromFireBaseStorage(post.getFileName());
                        profileViewModel.onRefreshPost();
                        Toast.makeText(context, "Deleted Successfully! "+Function.getEmojiByUnicode(0x1F607), Toast.LENGTH_SHORT).show();
                        posts.remove(position);
                        notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to delete! "+Function.getEmojiByUnicode(0x1F631), Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private void deleteFromFireBaseStorage(String fName){
        mStorageRef.child("imagePost/"+fName)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess delete: ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder{
        ImageView ivUserPost;
        TextView tvUsername;
        TextView tvPostMenu;
        ImageView ivPostImage;
        ImageView ivLike;
        ImageView ivComment;
        TextView tvNumOfLikes;
        TextView tvDescription;
        ImageView ivUserComment;
        EditText etAddComment;
        TextView tvViewAllComments;
        ImageButton ibAddComment;
        TextView tvCreatedOn;
        ProgressBar progressBar;
        TextView tvTags;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserPost          = itemView.findViewById(R.id.ivUserPost);
            tvUsername          = itemView.findViewById(R.id.tvUsername);
            tvPostMenu          = itemView.findViewById(R.id.tvPostMenu);
            ivPostImage         = itemView.findViewById(R.id.ivPostImage);
            ivLike              = itemView.findViewById(R.id.ivLike);
            ivComment           = itemView.findViewById(R.id.ivComment);
            tvNumOfLikes        = itemView.findViewById(R.id.tvNumOfLikes);
            tvDescription       = itemView.findViewById(R.id.tvDescription);
            ivUserComment       = itemView.findViewById(R.id.ivUserComment);
            etAddComment        = itemView.findViewById(R.id.etAddComment);
            tvViewAllComments   = itemView.findViewById(R.id.tvViewAllComments);
            ibAddComment        = itemView.findViewById(R.id.ibAddComment);
            tvCreatedOn         = itemView.findViewById(R.id.tvCreatedOn);
            progressBar         = itemView.findViewById(R.id.progressBar);
            tvTags              = itemView.findViewById(R.id.tvTags);
        }

    }
}
