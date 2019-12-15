package com.example.cleanblog.MainAppFrags;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cleanblog.Adapters.PostAdapter;
import com.example.cleanblog.R;
import com.example.cleanblog.models.Post;
import com.example.cleanblog.models.User;
import com.example.cleanblog.models.UserProfileImage;
import com.example.cleanblog.viewmodel.PostViewModel;
import com.example.cleanblog.viewmodel.ProfileViewModel;
import com.example.cleanblog.viewmodel.SharedViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "userId";
    private static final String ARG_PARAM2 = "userEmail";
    private static final String ARG_PARAM3 = "userProfileImageUrl";

    // TODO: Rename and change types of parameters
    private User user;
    private String userProfileImageUrl;

    private RecyclerView postListsView;
    private SwipeRefreshLayout swipeUp;
    private ProgressBar progressBar;
    TextView noInternet;

    ArrayList<Post> posts;
    PostAdapter postAdapter;

    PostViewModel postModel;
    SharedViewModel sharedViewModel;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2,String param3) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = new User(getArguments().getString(ARG_PARAM1),getArguments().getString(ARG_PARAM2));
            userProfileImageUrl = getArguments().getString(ARG_PARAM3);
        }

        postModel = ViewModelProviders.of(getActivity()).get(PostViewModel.class);

        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        if(sharedViewModel.data != null){
            sharedViewModel.data.observe(getActivity(), new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    if(!s.isEmpty() && s != null)
                        userProfileImageUrl = s;
                }
            });
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setRetainInstance(true);
        getActivity().setTitle(R.string.app_name);


        noInternet = view.findViewById(R.id.noInternet);
        postListsView = view.findViewById(R.id.recyclerview);
        postListsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);


        postModel.getPosts().observe(this, new Observer<ArrayList<Post>>() {
            @Override
            public void onChanged(ArrayList<Post> posts) {
                HomeFragment.this.posts = posts;
                postAdapter = new PostAdapter(getFragmentManager(),HomeFragment.this.getActivity(), HomeFragment.this.posts, user,userProfileImageUrl);
                postListsView.setAdapter(postAdapter);
                postAdapter.notifyDataSetChanged();
            }
        });


        swipeUp =view.findViewById(R.id.swipeUp);
        swipeUp.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeUp.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        postModel.onRefresh();
                        swipeUp.setRefreshing(false);
                    }
                },3000);

            }
        });

        return view;
    }

}
