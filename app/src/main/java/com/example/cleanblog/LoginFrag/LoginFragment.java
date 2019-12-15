package com.example.cleanblog.LoginFrag;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cleanblog.Function;
import com.example.cleanblog.MainActivity;
import com.example.cleanblog.R;
import com.example.cleanblog.models.User;
import com.example.cleanblog.models.UserProfileImage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class LoginFragment extends Fragment {


    private ProgressBar progressBar;
    private AutoCompleteTextView act_l_email;
    private TextView et_l_password;

    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
           //...
        }
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        act_l_email = view.findViewById(R.id.act_l_email);
        et_l_password = view.findViewById(R.id.et_l_password);
        Button bu_l_LogIn = view.findViewById(R.id.bu_l_LogIn);
        progressBar = view.findViewById(R.id.progressBar);

        bu_l_LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Function.disableWindow(getActivity());
                progressBar.setVisibility(View.VISIBLE);
                String password = et_l_password.getText().toString();
                String userEmail = Function.spiltEmailString(act_l_email.getText().toString());
                if(userEmail.isEmpty() && password.isEmpty()){
                    Toast.makeText(getActivity(), "Fields are empty", Toast.LENGTH_SHORT).show();
                }else{
                    userEmail = userEmail + "@email.com";
                    mAuth.signInWithEmailAndPassword(userEmail, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("info", "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        getCurrentUserInfo(user.getUid(),user.getEmail());
                                        //updateUI(user);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        act_l_email.setText("");
                                        et_l_password.setText("");
                                        Log.w("info", "signInWithEmail:failure", task.getException());
                                        Toast.makeText(getActivity(), "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                        Function.enableWindow(getActivity());
                                    }

                                    // ...
                                }
                            });
                }
            }
        });
        return view;
    }

    private void getCurrentUserInfo(final String userId,final String userEmail){
        final Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("userId",userId);
        intent.putExtra("userEmail",userEmail);
        DocumentReference userProfileImage = db.collection("UserProfileImages").document(userId);
        userProfileImage.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                UserProfileImage userProfileImage = document.toObject(UserProfileImage.class);
                                intent.putExtra("userProfileImageUrl",userProfileImage.getProfileImageUrl());
                                progressBar.setVisibility(View.GONE);
                                Function.enableWindow(getActivity());
                                startActivity(intent);
                                getActivity().finish();
                            }else{
                                intent.putExtra("userProfileImageUrl","");
                                progressBar.setVisibility(View.GONE);
                                Function.enableWindow(getActivity());
                                startActivity(intent);
                                getActivity().finish();
                            }
                        }else{
                            progressBar.setVisibility(View.GONE);
                            Function.enableWindow(getActivity());
                            act_l_email.setText("");
                            et_l_password.setText("");
                            Toast.makeText(getActivity(), "Something went wrong..", Toast.LENGTH_SHORT).show();
                           FrontFragment frontFragment = FrontFragment.newInstance();
                           getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameLogin,frontFragment).addToBackStack(null).commit();
                        }
                    }
                });
    }

}
