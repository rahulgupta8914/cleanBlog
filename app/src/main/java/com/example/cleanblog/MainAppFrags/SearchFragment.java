package com.example.cleanblog.MainAppFrags;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleanblog.Adapters.PostAdapter;
import com.example.cleanblog.R;
import com.example.cleanblog.models.Post;
import com.example.cleanblog.models.User;
import com.example.cleanblog.viewmodel.PostViewModel;
import com.example.cleanblog.viewmodel.SharedViewModel;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "userId";
    private static final String ARG_PARAM2 = "userEmail";
    private static final String ARG_PARAM3 = "userProfileImageUrl";

    // TODO: Rename and change types of parameters
    private String userId;
    private String userEmail;
    private String userProfileImageUrl;
    private ArrayList<Post> posts;

    private RecyclerView recycleView;

    private PostAdapter postAdapter;

    private PostViewModel postModel;
    private SharedViewModel sharedViewModel;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2,String param3) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId      = getArguments().getString(ARG_PARAM1);
            userEmail   = getArguments().getString(ARG_PARAM2);
            userProfileImageUrl = getArguments().getString(ARG_PARAM3);
        }
        postModel = ViewModelProviders.of(getActivity()).get(PostViewModel.class);
        posts = new ArrayList<>();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        getActivity().setTitle("Search");
        setHasOptionsMenu(true);

        recycleView = view.findViewById(R.id.recycleView);
        recycleView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu,menu);
        MenuItem item = menu.findItem(R.id.app_bar_search);
        android.widget.SearchView searchView = (android.widget.SearchView) MenuItemCompat.getActionView(item);
        SearchManager searchManager = (SearchManager)  getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchAndLoad(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                searchAndLoad(query);
                return false;
            }
        });
    }

    private void searchAndLoad(final String query) {
        postModel.getPosts().observe(this, new Observer<ArrayList<Post>>() {
            @Override
            public void onChanged(ArrayList<Post> posts) {
                String query1 = "#";
                if(query.startsWith("@")){
                    try{
                        query1 = query.split("@")[1] + "@email.com";

                    SearchFragment.this.posts.clear();
                    for(Post post:posts){
                        if(post.getUser().getUserEmail().equals(query1) || post.getUser().getUserEmail().startsWith(query.split("@")[1])){
                            SearchFragment.this.posts.add(post);
                        }
                        postAdapter = new PostAdapter(getFragmentManager(),getActivity(),SearchFragment.this.posts,new User(userId,userEmail),userProfileImageUrl);
                        recycleView.setAdapter(postAdapter);
                        postAdapter.notifyDataSetChanged();
                    }
                    }catch (Exception e){

                    }
                }else if(query.startsWith("#")){
                    SearchFragment.this.posts.clear();
                    for(Post post:posts){
                        if(post.getTags().contains(query)){
                            SearchFragment.this.posts.add(post);
                        }
                    }
                    postAdapter = new PostAdapter(getFragmentManager(),getActivity(),SearchFragment.this.posts,new User(userId,userEmail),userProfileImageUrl);
                    recycleView.setAdapter(postAdapter);
                    postAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.app_bar_search:{
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
