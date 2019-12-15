package com.example.cleanblog.LoginFrag;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cleanblog.Function;
import com.example.cleanblog.MainActivity;
import com.example.cleanblog.R;
import com.example.cleanblog.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.example.cleanblog.Function.spiltEmailString;

public class CreateAccountFragment extends Fragment {

    private ProgressBar progressBar;
    private AutoCompleteTextView act_c_email;
    private EditText et_c_password;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    public CreateAccountFragment() {
        // Required empty public constructor
    }

    public static CreateAccountFragment newInstance() {
        CreateAccountFragment fragment = new CreateAccountFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_account, container, false);

        progressBar = view.findViewById(R.id.progressBar);

        act_c_email = view.findViewById(R.id.act_c_email);
        et_c_password = view.findViewById(R.id.et_c_password);
        Button buCreateFirebaseAccount =  view.findViewById(R.id.buCreateFirebaseAccount);
        buCreateFirebaseAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = act_c_email.getText().toString();
                String password = et_c_password.getText().toString();

                if(userEmail.isEmpty() && password.isEmpty()) {
                    Toast.makeText(getActivity(), "Fields are empty", Toast.LENGTH_SHORT).show();
                }else if(password.length() <= 5){
                    Toast.makeText(getActivity(), "Password has to be at least 6 character long..", Toast.LENGTH_SHORT).show();
                }else{
                    if(userEmail.contains("@")){
                        userEmail = spiltEmailString(userEmail) + "@email.com";
                        Function.disableWindow(getActivity());
                        progressBar.setVisibility(View.VISIBLE);
                    }else{
                        userEmail = userEmail + "@email.com";
                        Function.disableWindow(getActivity());
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    mAuth.createUserWithEmailAndPassword(userEmail, password)
                            .addOnCompleteListener(getActivity(),new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
//                                    Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        //updateUI(user);
                                        Toast.makeText(getActivity(), "Successfully registered", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                        intent.putExtra("userId",user.getUid());
                                        intent.putExtra("userEmail",user.getEmail());
                                        intent.putExtra("userProfileImageUrl","");
                                        db.collection("users")
                                                .document(user.getUid())
                                                .set(new User(user.getUid(),user.getEmail()));
                                        progressBar.setVisibility(View.GONE);
                                        Function.enableWindow(getActivity());
                                        startActivity(intent);
                                        getActivity().finish();
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        Function.enableWindow(getActivity());
                                        // If sign in fails, display a message to the user.
                                        Log.w("info", "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(getActivity(), "Authentication failed.",Toast.LENGTH_SHORT).show();
                                        //updateUI(null);
                                    }

                                    // ...
                                }
                            });
                }


            }
        });
        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
