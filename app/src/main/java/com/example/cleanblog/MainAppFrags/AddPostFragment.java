package com.example.cleanblog.MainAppFrags;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.cleanblog.Function;
import com.example.cleanblog.R;
import com.example.cleanblog.models.Post;
import com.example.cleanblog.models.User;
import com.example.cleanblog.viewmodel.PostViewModel;
import com.example.cleanblog.viewmodel.ProfileViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddPostFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "userId";
    private static final String ARG_PARAM2 = "userEmail";

    // TODO: Rename and change types of parameters
    private User user;

    private String description;

    private StorageReference mStorageRef;
    FirebaseFirestore db;

    private Bitmap selectBitmap;

    PostViewModel postModel;
    ProfileViewModel profileViewModel;

    // Views
    private ImageView ivGetImage;
    private TextView etDescription;
    private ProgressBar progressBar;
    private EditText etTags;
    private TextView tvSuggestions;

    // Permission and result codes
    public static final int REQUEST_PERMISSION = 200;
    private String imageFilePath = "";
    private int PICK_IMAGE_CODE=123;
    private int CAPTURE_IMAGE_Code=124;


    public AddPostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *@return A new instance of fragment AddPostFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddPostFragment newInstance(String userId, String userEmail) {
        AddPostFragment fragment = new AddPostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, userId);
        args.putString(ARG_PARAM2, userEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = new User(getArguments().getString(ARG_PARAM1),getArguments().getString(ARG_PARAM2));
        }
        mStorageRef = FirebaseStorage.getInstance().getReference();
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
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);
        getActivity().setTitle("Add Post");

        etTags = view.findViewById(R.id.etTags);
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

        etTags.setImeOptions(EditorInfo.IME_ACTION_DONE);
        tvSuggestions = view.findViewById(R.id.tvSuggestions);

        progressBar = view.findViewById(R.id.progressBar);
        etDescription = view.findViewById(R.id.etDescription);
        ivGetImage = view.findViewById(R.id.ivGetImage);
        ivGetImage.setOnClickListener(new View.OnClickListener() {
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
                                        loadImageFromCamera();
                                        Toast.makeText(getActivity(), "Camera", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        Toast.makeText(getActivity(), "Gallery", Toast.LENGTH_SHORT).show();
                                        loadImageFromGallery();
                                }
                            }
                        });
                builder.create();
                AlertDialog dialog = builder.show();
            }
        });
        etDescription.addTextChangedListener(new TextWatcher() {
            String s1 = etDescription.getText().toString();
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().isEmpty()){
                    etTags.setVisibility(View.GONE);
                    tvSuggestions.setVisibility(View.GONE);
                }else{
                    etTags.setVisibility(View.VISIBLE);
                    tvSuggestions.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(checkReports()){
                    setHasOptionsMenu(true);
                }else {
                    setHasOptionsMenu(false);
                }

            }
        });


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.post_share_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.shearMenu:
                description = etDescription.getText().toString();
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etDescription.getWindowToken(), 0);
                postUpdate(Function.scaleDown(selectBitmap),description,validateTags(etTags.getText().toString()));

        }
        return super.onOptionsItemSelected(item);
    }

    public void loadImageFromGallery(){
        //TODO: load image
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,PICK_IMAGE_CODE);
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

    private void postUpdate(Bitmap bitmap, final String description,final String tags){

        progressBar.setVisibility(View.VISIBLE);
        Function.disableWindow(getActivity());
        String email    = spiltEmailString(user.getUserEmail());
        final String imagePath = email + "." + Function.getTime() +".jpg";
        final StorageReference imageReference = mStorageRef.child("imagePost/"+imagePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = imageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Function.enableWindow(getActivity());
                Toast.makeText(getActivity().getApplicationContext(),"Couldn't Upload!",Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUri = uri.toString();
                        Post post = new Post(imagePath,downloadUri,description,user, Function.getTime(),tags);
                        db.collection("posts")
                                .document()
                                .set(post)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        etDescription.setText("");
                                        etTags.setText("");
                                        ivGetImage.setImageDrawable(null);
                                        progressBar.setVisibility(View.GONE);
                                        Function.enableWindow(getActivity());
                                        Toast.makeText(getActivity(), "Successfully Posted "+Function.getEmojiByUnicode(0x1F607), Toast.LENGTH_SHORT).show();
                                        ivGetImage.setBackgroundResource(R.drawable.ic_upload_photo_black_24dp);
                                        postModel.onRefresh();
                                        profileViewModel.onRefreshPost();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        etDescription.setText("");
                                        etTags.setText("");
                                        progressBar.setVisibility(View.GONE);
                                        Function.enableWindow(getActivity());
                                        Toast.makeText(getActivity(), "Unable to post! "+Function.getEmojiByUnicode(0x1F631), Toast.LENGTH_SHORT).show();
                                        ivGetImage.setBackgroundResource(R.drawable.ic_upload_photo_black_24dp);
                                    }
                                });
                    }
                });
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //from camera
        if (requestCode == CAPTURE_IMAGE_Code) {
            if (resultCode == RESULT_OK) {
                selectBitmap = getBitmap(imageFilePath);
                //ivGetImage.setImageURI(Uri.parse(imageFilePath));
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getActivity(), "You cancelled the operation", Toast.LENGTH_SHORT).show();
            }
        }

        //from gallery
        if((requestCode == PICK_IMAGE_CODE) && (data!=null)){
            Uri selectedImage = data.getData();
            selectBitmap = null;
            try {
                selectBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                ivGetImage.setBackground(null);
                ivGetImage.setImageBitmap(selectBitmap);



            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
    private boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);
        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable)drawable).getBitmap() != null;
        }
        return hasImage;
    }
    private Bitmap getBitmap(@NonNull String path) {
        try {
            Bitmap bitmap=null;
            File f= new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            ivGetImage.setImageBitmap(bitmap);
            ivGetImage.setBackground(null);
            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }}

    private Boolean checkReports(){
        if((hasImage(ivGetImage)==true) &&(!etDescription.getText().toString().isEmpty())){
            return true;
        }else {
            return false;
        }
    }
    private String spiltEmailString(@NonNull String email){
        String[] split = email.split("@");
        return split[0];
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Thanks for granting Permission", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(), "Can't access your images!!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
