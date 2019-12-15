package com.example.cleanblog;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.cleanblog.MainAppFrags.AddPostFragment;
import com.example.cleanblog.MainAppFrags.HomeFragment;
import com.example.cleanblog.MainAppFrags.NotificationFragment;
import com.example.cleanblog.MainAppFrags.OpenPostUserFragment;
import com.example.cleanblog.MainAppFrags.ProfileFragment;
import com.example.cleanblog.MainAppFrags.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    TextView noInternet;
    HomeFragment homeFragment;
    SearchFragment searchFragment;
    AddPostFragment addPostFragment;
    NotificationFragment notificationFragment;
    ProfileFragment profileFragment;

    public BottomNavigationView bottomNavigationView;

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.navHome:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameMainApp,homeFragment).commit();
                    break;
                case R.id.navSearch:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameMainApp,searchFragment).commit();
                    break;
                case R.id.navPost:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameMainApp,addPostFragment).commit();
                    break;
                case R.id.navFavorite:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameMainApp,notificationFragment).commit();
                    break;
                case R.id.navProfile:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameMainApp,profileFragment).commit();
                    break;
            }
            return true;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();
        String userId = bundle.getString("userId");
        String userEmail = bundle.getString("userEmail");
        String userProfileImageUrl = bundle.getString("userProfileImageUrl");

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        noInternet           = findViewById(R.id.noInternet);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        // Initialize all fragments
        homeFragment            = HomeFragment.newInstance(userId,userEmail,userProfileImageUrl);
        searchFragment          = SearchFragment.newInstance(userId,userEmail,userProfileImageUrl);
        addPostFragment         = AddPostFragment.newInstance(userId,userEmail);
        notificationFragment    = NotificationFragment.newInstance(userId,userEmail);
        profileFragment         = ProfileFragment.newInstance(userId,userEmail);




        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.frameMainApp,homeFragment).commit();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(bottomNavigationView.getVisibility() == View.GONE){
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
        setTitle(R.string.app_name);
//        Fragment f = getVisibleFragment();
//        if(f instanceof OpenPostUserFragment){
//            getSupportFragmentManager().popBackStackImmediate();
//        }
    }

    public Fragment getVisibleFragment(){
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if(fragments != null){
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }
}
