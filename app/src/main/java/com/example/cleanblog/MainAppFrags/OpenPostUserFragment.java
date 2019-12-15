package com.example.cleanblog.MainAppFrags;


import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cleanblog.Adapters.OpenPostUserAdapter;
import com.example.cleanblog.Function;
import com.example.cleanblog.R;
import com.example.cleanblog.models.Follow;
import com.example.cleanblog.models.Post;
import com.example.cleanblog.models.User;
import com.example.cleanblog.models.UserProfileImage;
import com.example.cleanblog.viewmodel.UserPostsViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OpenPostUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OpenPostUserFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";



    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // TODO: Rename and change types of parameters
    private String postUserId;
    private String postUserEmail;
    private String userId;
    private String userEmail;
    private String userProfileImageUrl;

    private ImageView ivProfileImage;
    private TextView tvFollowUnFollow;
    private TextView tvPostNum;
    private TextView tvFollowersNum;
    private TextView tvFollowingNum;
    private RecyclerView recyclerView;

    private UserPostsViewModel userPostsViewModel;

    private OpenPostUserAdapter adapter;

    ArrayList<Post> posts;

    public OpenPostUserFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static OpenPostUserFragment newInstance(String param1, String param2,String param3,String param4,String param5) {
        OpenPostUserFragment fragment = new OpenPostUserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        args.putString(ARG_PARAM4, param4);
        args.putString(ARG_PARAM5, param5);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postUserId          = getArguments().getString(ARG_PARAM1);
            postUserEmail       = getArguments().getString(ARG_PARAM2);
            userId              = getArguments().getString(ARG_PARAM3);
            userEmail           = getArguments().getString(ARG_PARAM4);
            userProfileImageUrl = getArguments().getString(ARG_PARAM5);

        }

        userPostsViewModel = ViewModelProviders.of(getActivity(), new ViewModelProvider.Factory(){
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new UserPostsViewModel(new User(postUserId,postUserEmail));
            }
        }).get(UserPostsViewModel.class);
        posts = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_open_post_user, container, false);
        
        getActivity().setTitle(Function.spiltEmailString(postUserEmail));
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvFollowUnFollow = view.findViewById(R.id.tvFollowUnfollow);
        tvPostNum = view.findViewById(R.id.tvPostNum);
        tvFollowersNum = view.findViewById(R.id.tvFollowersNum);
        tvFollowingNum = view.findViewById(R.id.tvFollowingNum);
        recyclerView = view.findViewById(R.id.recyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Function.disableWindow(getActivity());
        getAllInformation();
        Function.enableWindow(getActivity());

        tvFollowUnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followAndUnFollowEvent();
            }
        });

        userPostsViewModel.getAllPostsByUser().observe(this, new Observer<ArrayList<Post>>() {
            @Override
            public void onChanged(ArrayList<Post> posts) {
                adapter = new OpenPostUserAdapter(OpenPostUserFragment.this.getActivity(),posts,new User(postUserId,postUserEmail),new User(userId,userEmail),userProfileImageUrl);
                recyclerView.setAdapter(adapter);
            }
        });
        return view;
    }

    private void getAllInformation(){
        getProfileImage();
        getFollowingUnFollowingActivity();
        getFollowersNumber();
        getFollowingNumber();
        getPostNum();
    }

    private void getFollowingNumber() {
        db.collection("follow")
                .whereEqualTo("userId",postUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            tvFollowingNum.setText(String.valueOf(task.getResult().size()));
                        }
                    }
                });
    }

    private void getFollowersNumber() {
        db.collection("follow")
                .whereEqualTo("followingUserId",postUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            tvFollowersNum.setText(String.valueOf(task.getResult().size()));
                        }
                    }
                });
    }

    private void getPostNum() {
        db.collection("posts")
                .whereEqualTo("user",new User(postUserId,postUserEmail))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        tvPostNum.setText(String.valueOf(task.getResult().size()));
                    }
                });
    }

    private void getFollowingUnFollowingActivity() {
        db.collection("follow")
                .whereEqualTo("userId",userId)
                .whereEqualTo("followingUserId",postUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().size() == 1)
                            tvFollowUnFollow.setText("following");
                            tvFollowUnFollow.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void followAndUnFollowEvent(){
        if(tvFollowUnFollow.getText().toString().equals("follow")){
            Follow follow = new Follow(userId,postUserId);
            db.collection("follow")
                    .add(follow)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), "following", Toast.LENGTH_SHORT).show();
                                tvFollowUnFollow.setText("following");
                                getFollowersNumber();
                            }
                        }
                    });

        }else if(tvFollowUnFollow.getText().toString().equals("following")){
            final CollectionReference reference = db.collection("follow");
                    reference
                    .whereEqualTo("userId",userId)
                    .whereEqualTo("followingUserId",postUserId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(DocumentSnapshot documentSnapshot : task.getResult()){
                                    reference.document(documentSnapshot.getId()).delete();
                                    tvFollowUnFollow.setText("follow");
                                    getFollowersNumber();
                                }
                            }
                        }
                    });
        }
    }

    private void getProfileImage(){
        final CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getActivity());
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(5f);
        circularProgressDrawable.start();

        DocumentReference postUserInformation = db.collection("UserProfileImages").document(postUserId);
        postUserInformation.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        UserProfileImage postUserProfileImage = document.toObject(UserProfileImage.class);
                        if(!postUserProfileImage.getProfileImageUrl().isEmpty() && postUserProfileImage.getProfileImageUrl()!=null){
                            Glide.with(OpenPostUserFragment.this).load(Uri.parse(postUserProfileImage.getProfileImageUrl())).placeholder(circularProgressDrawable).into(ivProfileImage);
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

}
