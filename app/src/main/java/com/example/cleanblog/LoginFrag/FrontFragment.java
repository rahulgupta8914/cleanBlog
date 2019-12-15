package com.example.cleanblog.LoginFrag;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cleanblog.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FrontFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FrontFragment extends Fragment {


    public FrontFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment FrontFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FrontFragment newInstance() {
        FrontFragment fragment = new FrontFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_front, container, false);
        Button buCreateAccount = view.findViewById(R.id.buCreateAccount);
        buCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                CreateAccountFragment createAccountFragment = CreateAccountFragment.newInstance();
                fragmentTransaction.replace(R.id.frameLogin,createAccountFragment).addToBackStack(null).commit();
            }
        });
        Button buLogIn = view.findViewById(R.id.buLogIn);
        buLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                LoginFragment loginFragment = LoginFragment.newInstance();
                fragmentTransaction.replace(R.id.frameLogin,loginFragment).addToBackStack(null).commit();
            }
        });

        return view;
    }

}
