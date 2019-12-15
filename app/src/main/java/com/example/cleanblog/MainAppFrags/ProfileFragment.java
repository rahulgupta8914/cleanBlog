package com.example.cleanblog.MainAppFrags;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.cleanblog.Adapters.UserPostAdapter;
import com.example.cleanblog.Function;
import com.example.cleanblog.LoginActivity;
import com.example.cleanblog.R;
import com.example.cleanblog.models.Post;
import com.example.cleanblog.models.User;
import com.example.cleanblog.models.UserProfileImage;
import com.example.cleanblog.viewmodel.ProfileViewModel;
import com.example.cleanblog.viewmodel.SharedViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "userId";
    private static final String ARG_PARAM2 = "userEmail";

    // TODO: Rename and change types of parameters
    private String userId;
    private String userEmail;

    private String imageFilePath;

    private Intent galIntent;
    //
    private ImageView ivProfileImage;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ArrayList<Post> posts;
    private UserPostAdapter adapter;

    private Bitmap selectBitmap;

    final int REQUEST_STORAGE_PERMISSION = 200;
    final int REQUEST_CAMERA_PERMISSION = 100;

    private int PICK_IMAGE_CODE=123;
    private int CAPTURE_IMAGE_Code=124;

    private StorageReference mStorageRef;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private SharedViewModel sharedViewModel;

    private TextView followingNum;
    private TextView followersNum;
    private TextView tvPostNum;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ProfileViewModel profileViewModel;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
            userId      = getArguments().getString(ARG_PARAM1);
            userEmail   = getArguments().getString(ARG_PARAM2);
        }
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        profileViewModel = ViewModelProviders.of(getActivity(), new ViewModelProvider.Factory(){
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ProfileViewModel(new User(userId,userEmail));
            }
        }).get(ProfileViewModel.class);





    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        setRetainInstance(true);
        setHasOptionsMenu(true);
        getActivity().setTitle(Function.spiltEmailString(userEmail));
        followingNum = view.findViewById(R.id.followingNum);
        followersNum = view.findViewById(R.id.tvFollowersNum);
        tvPostNum = view.findViewById(R.id.tvPostNum);
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setNestedScrollingEnabled(false);
        profileViewModel.getAllPostsByUser().observe(this, new Observer<ArrayList<Post>>() {
            @Override
            public void onChanged(ArrayList<Post> posts) {
                ProfileFragment.this.posts = posts;
                adapter = new UserPostAdapter(getActivity(),ProfileFragment.this.posts);
                recyclerView.setAdapter(adapter);
            }
        });



//        Function.disableWindow(getActivity());
        getAllInformation();
//        Function.enableWindow(getActivity());

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        profileViewModel.onRefresh();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },3000);

            }
        });



        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] strings = {"Camera","Gallery"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.select_image_from)
                        .setItems(strings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch(which){
                                    case 0:
                                        //checkCameraPermissionInRuntime();
                                        loadImageFromCamera();
                                        Toast.makeText(getActivity(), "Camera", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        checkStoragePermissionInRuntime();
                                        Toast.makeText(getActivity(), "Gallery", Toast.LENGTH_SHORT).show();
                                        loadImageFromGallery();
                                }
                            }
                        });
                builder.create();
                AlertDialog dialog = builder.show();
            }
        });
        return view;
    }


    private void getAllInformation(){



        profileViewModel.getProfileImageUrl().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(!s.isEmpty()){
                    final CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getActivity().getApplicationContext());
                    circularProgressDrawable.setStrokeWidth(5f);
                    circularProgressDrawable.setCenterRadius(5f);
                    circularProgressDrawable.start();
                    Glide.with(ProfileFragment.this).load(Uri.parse(s)).placeholder(circularProgressDrawable).into(ivProfileImage);
                }
            }
        });

        profileViewModel.getNumberOfPosts().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tvPostNum.setText(s);
            }
        });

        profileViewModel.getNumberOfFollowers().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                followersNum.setText(s);
            }
        });

        profileViewModel.getNumberOfFollowings().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                followingNum.setText(s);
            }
        });

    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.finish_menu,menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.buLogout:{
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    public void loadImageFromCamera(){
        {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    //...
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(getContext(),
                            "com.example.cleanblog.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAPTURE_IMAGE_Code);
                }
            }
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //from camera
        if (requestCode == CAPTURE_IMAGE_Code) {
            if (resultCode == RESULT_OK) {
                selectBitmap = getBitmap(imageFilePath);
                updateProfileImage(Function.scaleDownProfilePicture(selectBitmap));
                //ivGetImage.setImageURI(Uri.parse(imageFilePath));
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getActivity(), "You cancelled the operation", Toast.LENGTH_SHORT).show();
            }
        }

        //from gallery
        if((requestCode == PICK_IMAGE_CODE) && (data!=null) && (resultCode == RESULT_OK)){
            Uri selectedImage = data.getData();
            selectBitmap = null;
            try {
                selectBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                ivProfileImage.setBackground(null);
                ivProfileImage.setImageBitmap(selectBitmap);
                updateProfileImage(Function.scaleDownProfilePicture(selectBitmap));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(getActivity(), "You cancelled the operation", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfileImage(Bitmap bitmap) {
        Function.disableWindow(getActivity());
        progressBar.setVisibility(View.VISIBLE);
//        String imagePath = Function.spiltEmailString(userEmail) + "." + Function.getTime() +".png";
        String imagePath = Function.spiltEmailString(userEmail)+".png";
        final StorageReference imageReference = mStorageRef.child("profileImages/"+imagePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,60,baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = imageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Couldn't updated..", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {
                        UserProfileImage userProfileImage = new UserProfileImage(uri.toString());
                        db.collection("UserProfileImages")
                                .document(userId)
                                .set(userProfileImage)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getActivity().getApplicationContext(), "Profile picture has updated..", Toast.LENGTH_SHORT).show();
                                        sharedViewModel.data(uri.toString());
                                    }
                                });
                        Function.enableWindow(getActivity());
                        progressBar.setVisibility(View.GONE);
                        profileViewModel.refreshProfileImageUrl();

                    }
                });
            }
        });

    }

    private Bitmap getBitmap(@NonNull String path) {
        try {
            Bitmap bitmap=null;
            File f= new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            ivProfileImage.setImageBitmap(bitmap);
            ivProfileImage.setBackground(null);
            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }}
    private void loadImageFromGallery() {
        galIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galIntent,PICK_IMAGE_CODE);
    }

    public void checkStoragePermissionInRuntime(){
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        }
    }

//    public void checkCameraPermissionInRuntime(){
//        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA},
//                    REQUEST_CAMERA_PERMISSION);
//        }
//    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_STORAGE_PERMISSION:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getActivity(), "Thanks for granting Permission", Toast.LENGTH_SHORT).show();
                }else{
                    checkStoragePermissionInRuntime();
                    Toast.makeText(getActivity(), "Please try again", Toast.LENGTH_SHORT).show();
                }
            }
            break;
//            case REQUEST_CAMERA_PERMISSION:{
//                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    Toast.makeText(getActivity(), "Thanks for granting Permission", Toast.LENGTH_SHORT).show();
//                }else{
//                    checkCameraPermissionInRuntime();
//                    Toast.makeText(getActivity(), "Please try again", Toast.LENGTH_SHORT).show();
//                }
//            }
        }
    }

}
