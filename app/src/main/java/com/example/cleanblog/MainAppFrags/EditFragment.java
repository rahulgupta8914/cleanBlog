package com.example.cleanblog.MainAppFrags;


import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cleanblog.Function;
import com.example.cleanblog.R;
import com.example.cleanblog.models.Post;
import com.example.cleanblog.models.User;
import com.example.cleanblog.viewmodel.PostViewModel;
import com.example.cleanblog.viewmodel.ProfileViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;



public class EditFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "user";
    private static final String ARG_PARAM2 = "post";
    private static final String ARG_PARAM3 = "userProfileImageUrl";

    // TODO: Rename and change types of parameters
    private User user;
    private Post post;
    private String userProfileImageUrl;

    private ImageView imageView;
    private EditText etDescription;
    private EditText etTags;
    private Button buUpdate;
    private ProgressBar progressBar;

    private PostViewModel postModel;;
    private ProfileViewModel profileViewModel;

    FirebaseFirestore db;

    public EditFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static EditFragment newInstance(User user, Post post,String userProfileImageUrl) {
        EditFragment fragment = new EditFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, user);
        args.putSerializable(ARG_PARAM2, post);
        args.putString(ARG_PARAM3,userProfileImageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(ARG_PARAM1);
            post = (Post) getArguments().getSerializable(ARG_PARAM2);
            userProfileImageUrl = getArguments().getString(ARG_PARAM3);
        }
        db = FirebaseFirestore.getInstance();

        postModel = ViewModelProviders.of(getActivity()).get(PostViewModel.class);
        profileViewModel = ViewModelProviders.of(getActivity(), new ViewModelProvider.Factory(){
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ProfileViewModel(user);
            }
        }).get(ProfileViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_edit, container, false);
        imageView = view.findViewById(R.id.imageView);
        etDescription = view.findViewById(R.id.etDescription);
        etTags = view.findViewById(R.id.etTags);
        progressBar = view.findViewById(R.id.progressBar);
        buUpdate    = view.findViewById(R.id.buUpdate);

        setCurrentPostInformations();

        buUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!etDescription.getText().toString().isEmpty()){
                    updatePostInformation();
                }
            }
        });

        etTags.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    etTags.setText(validateTags(etTags.getText().toString()));
                    handled = true;
                }
                return handled;
            }
        });

        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buUpdate.setText(R.string.update);
                if(s.toString().isEmpty()){
                    buUpdate.setEnabled(false);
                }{
                    buUpdate.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etTags.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buUpdate.setText(R.string.update);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    private String validateTags(String s){
        String result = "";
        String[] rowText = s.split(" ");

        for (String text : rowText){
            if(text.startsWith("#")){
                result += text + " ";
            }else{
                result += "#" + text + " ";
            }
        }
        return result;
    }

    private void updatePostInformation(){
        Function.disableWindow(getActivity());
        progressBar.setVisibility(View.VISIBLE);
        post.setDescription(etDescription.getText().toString());
        post.setTags(validateTags(etTags.getText().toString()));

        db.collection("posts")
                .document(post.getPostId())
                .set(post)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                        postModel.onRefresh();
                        profileViewModel.onRefreshPost();
                        progressBar.setVisibility(View.INVISIBLE);
                        Function.enableWindow(getActivity());

                        //getFragmentManager().popBackStackImmediate();
                        //getActivity().onBackPressed();
                        //getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Failed to update", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                Function.enableWindow(getActivity());
                getFragmentManager().popBackStackImmediate();
            }
        });
    }

    private void setCurrentPostInformations(){
        //setting post image
        progressBar.setVisibility(View.VISIBLE);
        etDescription.setText(post.getDescription());
        etTags.setText(post.getTags());
        Picasso.get().load(Uri.parse(post.getImageUrl())).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onError(Exception e) {
            }
        });

    }

}
