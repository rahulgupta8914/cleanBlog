package com.example.cleanblog.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.example.cleanblog.Function;
import com.example.cleanblog.R;
import com.example.cleanblog.models.Post;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;


public class UserPostAdapter extends RecyclerView.Adapter<UserPostAdapter.PostViewHolder> {

    private Context context;
    private ArrayList<Post> posts;

    public UserPostAdapter(Context context, ArrayList<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater  =  LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.user_post_container,parent,false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder  postViewHolder, int position) {
        Post post = posts.get(position);

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(5f);
        circularProgressDrawable.start();


        //setting post image
        postViewHolder.progressBar.setVisibility(View.VISIBLE);
        Picasso.get().load(Uri.parse(post.getImageUrl())).into(postViewHolder.ivPostImage, new Callback() {
            @Override
            public void onSuccess() {
                postViewHolder.progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onError(Exception e) {
            }
        });

        postViewHolder.tvDescription.setText(Function.spiltEmailString(post.getUser().getUserEmail())+" : " + post.getDescription());
        postViewHolder.tvDescription.setMovementMethod(new ScrollingMovementMethod());

        int[] color = {Color.WHITE,Color.LTGRAY};
        float[] position1 = {0, 1};
        Shader.TileMode tile_mode = Shader.TileMode.REPEAT;
        LinearGradient lin_grad = new LinearGradient(0, 0, 0, 15,color,position1, tile_mode);
        Shader shader_gradient = lin_grad;
        postViewHolder.tvCreatedOn.getPaint().setShader(shader_gradient);
        postViewHolder.tvNumOfLikes.getPaint().setShader(shader_gradient);
        postViewHolder.tvViewAllComments.getPaint().setShader(shader_gradient);

        // set timestamp on view
        String postTimeStamp = post.getTimeStamp();
        Timestamp timestamp = new Timestamp(Long.parseLong(postTimeStamp));
        Date date = new Date(timestamp.getTime());
        String s = date.toString();
        System.out.println(s);
        String[] split = s.split(" ");
        String newDate ="Posted on \n"+ split[0] + ", "+split[1]+" "+split[2]+" "+split[3];
        postViewHolder.tvCreatedOn.setText(newDate);


    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder{

        ImageView ivPostImage;
        ImageView ivLike;
        TextView  tvDescription;
        TextView  tvCreatedOn;
        TextView  tvViewAllComments;
        TextView  tvNumOfLikes;
        ProgressBar progressBar;


        public PostViewHolder(@NonNull View view) {
            super(view);
            ivPostImage = view.findViewById(R.id.ivPostImage);
            ivLike = view.findViewById(R.id.ivLike);
            tvDescription = view.findViewById(R.id.tvDescription);
            tvCreatedOn = view.findViewById(R.id.tvCreatedOn);
            tvViewAllComments = view.findViewById(R.id.tvViewAllComments);
            tvNumOfLikes = view.findViewById(R.id.tvNumOfLikes);
            progressBar  = view.findViewById(R.id.progressBar);
        }
    }
}
