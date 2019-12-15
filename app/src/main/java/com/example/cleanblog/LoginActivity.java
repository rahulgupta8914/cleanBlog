package com.example.cleanblog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.cleanblog.LoginFrag.CreateAccountFragment;
import com.example.cleanblog.LoginFrag.FrontFragment;
import com.example.cleanblog.LoginFrag.LoginFragment;
import com.example.cleanblog.models.User;
import com.example.cleanblog.models.UserProfileImage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class LoginActivity extends AppCompatActivity {

    FrontFragment frontFragment;

    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        //Initialization...
        frontFragment = FrontFragment.newInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            getCurrentUserInfo(currentUser.getUid(),currentUser.getEmail());
        }else{
            //calling default fragment
                getSupportFragmentManager().beginTransaction().add(R.id.frameLogin,frontFragment).commit();
        }
    }

    private void getCurrentUserInfo(String uid, final String userEmail){
        final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("userId",uid);
        intent.putExtra("userEmail",userEmail);

        DocumentReference userProfileImage = db.collection("UserProfileImages").document(uid);
        userProfileImage.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                UserProfileImage userProfileImage = document.toObject(UserProfileImage.class);
                                if(!userProfileImage.getProfileImageUrl().isEmpty() && userProfileImage.getProfileImageUrl()!=null){
                                intent.putExtra("userProfileImageUrl",userProfileImage.getProfileImageUrl());
                                }else{
                                    intent.putExtra("userProfileImageUrl","");
                                }
                                startActivity(intent);
                                finish();
                            }else{
                                Log.i(TAG, "no images is found");
                                intent.putExtra("userProfileImageUrl","");
                                startActivity(intent);
                                finish();
                            }
                        }else{
                            Log.i(TAG, "Task was unsuccessful..");
                        }
                    }
                });
    }

//    @Override
//    public void onBackPressed() {
//        Fragment f = getVisibleFragment();
//        if (f instanceof CreateAccountFragment) {
//            getSupportFragmentManager().beginTransaction().remove(f).commit();
//            getSupportFragmentManager().beginTransaction().replace(R.id.frameLogin,frontFragment).commit();
//        }else if(f instanceof LoginFragment){
//            getSupportFragmentManager().beginTransaction().remove(f).commit();
//            getSupportFragmentManager().beginTransaction().replace(R.id.frameLogin,frontFragment).commit();
//        } else{
//            super.onBackPressed();
//        }
//    }
//    public Fragment getVisibleFragment(){
//        List<Fragment> fragments = getSupportFragmentManager().getFragments();
//        if(fragments != null){
//            for(Fragment fragment : fragments){
//                if(fragment != null && fragment.isVisible())
//                    return fragment;
//            }
//        }
//        return null;
//    }


}