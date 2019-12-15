package com.example.cleanblog.Adapters;

import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;


import com.bumptech.glide.Glide;
import com.example.cleanblog.Function;
import com.example.cleanblog.R;
import com.example.cleanblog.models.Comment;
import com.example.cleanblog.models.Like;
import com.example.cleanblog.models.Post;
import com.example.cleanblog.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class OpenPostUserAdapter extends  RecyclerView.Adapter<OpenPostUserAdapter.holder> {

    private Context context;
    private ArrayList<Post> posts;
    private User postUser;
    private User user;
    private String userProfileImageUrl;


    private InputMethodManager imm;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public OpenPostUserAdapter(Context context, ArrayList<Post> posts, User postUser, User user, String userProfileImageUrl) {
        this.context = context;
        this.posts = posts;
        this.postUser = postUser;
        this.user = user;
        this.userProfileImageUrl = userProfileImageUrl;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater  =  LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.open_post_container,parent,false);
        return new OpenPostUserAdapter.holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final holder holder, int position) {
        final Post post = posts.get(position);

        imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if(post.getTags().isEmpty() || post.getTags().equals(null)){
            holder.tvTags.setVisibility(View.GONE);
        }else{
            holder.tvTags.setText(post.getTags());
        }

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(5f);
        circularProgressDrawable.start();
        if(!userProfileImageUrl.isEmpty()){
            holder.ivUserComment.setImageTintMode(null);
            Glide.with(context).load(Uri.parse(userProfileImageUrl)).placeholder(circularProgressDrawable).into(holder.ivUserComment);
        }

        holder.ivLike.setTag(R.drawable.ic_favorite_border_black_24dp);

        holder.ivComment.setTag(1);
        holder.ivComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.ivComment.getTag().equals(1)){
                    holder.ivUserComment.setVisibility(View.VISIBLE);
                    holder.etAddComment.setVisibility(View.VISIBLE);
                    holder.ivComment.setTag(0);
                }else{
                    holder.ivUserComment.setVisibility(View.GONE);
                    holder.etAddComment.setVisibility(View.GONE);
                    holder.ivComment.setTag(1);
                }
            }
        });
        
        //set eachPost
        // set timestamp on view
        String postTimeStamp = post.getTimeStamp();
        Timestamp timestamp = new Timestamp(Long.parseLong(postTimeStamp));
        Date date = new Date(timestamp.getTime());
        String s = date.toString();
        System.out.println(s);
        String[] split = s.split(" ");
        String newDate ="Posted on \n"+ split[0] + ", "+split[1]+" "+split[2]+" "+split[3];
        holder.tvCreatedOn.setText(newDate);
        // setting description
        holder.tvDescription.setText(Function.spiltEmailString(post.getUser().getUserEmail())+" : " + post.getDescription());
        holder.progressBar.setVisibility(View.VISIBLE);
        Picasso.get().load(Uri.parse(post.getImageUrl())).into(holder.ivPostImage, new Callback() {
            @Override
            public void onSuccess() {
                holder.progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onError(Exception e) {
            }
        });


        // get and set number of likes
        getNumberLikes(post.getPostId(),holder.tvNumOfLikes);
        // set like icon if logged in user already did
        likeButtonActivity(post.getPostId(),holder.ivLike);
        // add remove like
        holder.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRemoveLike(holder.ivLike,post.getPostId(),holder.tvNumOfLikes);
            }
        });

        // get number of comments
        getNumberOfComments(holder.tvViewAllComments,post.getPostId());

         //add comment
        holder.ibAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Comment comment = new Comment(user.getUserId(),post.getPostId(),holder.etAddComment.getText().toString(),Function.getTime());
                addComment(comment,holder.etAddComment,holder.ivUserComment,holder.tvViewAllComments,post.getPostId());
            }
        });

        holder.etAddComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)holder.etAddComment.getLayoutParams();
                if(s.toString().isEmpty()){
                    holder.ibAddComment.setVisibility(View.GONE);
                    params.setMarginEnd(16);
                    holder.etAddComment.setLayoutParams(params);
                }else{
                    holder.ibAddComment.setVisibility(View.VISIBLE);
                    params.setMarginEnd(0);
                    holder.etAddComment.setLayoutParams(params);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

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

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class holder extends RecyclerView.ViewHolder{

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
        TextView tvTags;
        ProgressBar progressBar;

        public holder(@NonNull View itemView) {
            super(itemView);
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
            tvTags              = itemView.findViewById(R.id.tvTags);
            progressBar         = itemView.findViewById(R.id.progressBar);
        }
    }
}
