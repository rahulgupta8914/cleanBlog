package com.example.cleanblog;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.cleanblog.models.UserProfileImage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Function {

    public static String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    public static String spiltEmailString(@NonNull String email){
        String[] split = email.split("@");
        return split[0];
    }

    public static String getTime( ){
        Date date= new Date();
        Long time = date.getTime();
        return time.toString();
    }

    public static Bitmap scaleDown(@NonNull Bitmap bitmap){
        int maxWidth = 1500;
        int maxHeight = 1500;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int)(height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int)(width / ratio);
        } else {
            // square
            height = maxHeight;
            width = maxWidth;
        }
        Bitmap newbit = Bitmap.createScaledBitmap(bitmap,width,height,true);
        return newbit;
    }

    public static Bitmap scaleDown2(@NonNull Bitmap bitmap){
        int maxWidth = 500;
        int maxHeight = 500;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int)(height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int)(width / ratio);
        } else {
            // square
            height = maxHeight;
            width = maxWidth;
        }
        Bitmap newbit = Bitmap.createScaledBitmap(bitmap,width,height,true);
        return newbit;
    }
    public static Bitmap scaleDownProfilePicture(@NonNull Bitmap bitmap){

        Bitmap dstBmp;
        if (bitmap.getWidth() >= bitmap.getHeight()){
            dstBmp = Bitmap.createBitmap(
                    bitmap,
                    bitmap.getWidth()/2 - bitmap.getHeight()/2,
                    0,
                    bitmap.getHeight(),
                    bitmap.getHeight()
            );
        }else{
            dstBmp = Bitmap.createBitmap(
                    bitmap,
                    0,
                    bitmap.getHeight()/2 - bitmap.getWidth()/2,
                    bitmap.getWidth(),
                    bitmap.getWidth()
            );
        }
        Bitmap bitmap1 = scaleDown2(dstBmp);

        Bitmap circleBitmap = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight(), Bitmap.Config.ARGB_8888);

        BitmapShader shader = new BitmapShader (bitmap1,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);
        paint.setAntiAlias(true);
        Canvas c = new Canvas(circleBitmap);
        c.drawCircle(bitmap1.getWidth()/2, bitmap1.getHeight()/2, bitmap1.getWidth()/2, paint);

        return circleBitmap;
    }

    public static void disableWindow(Activity activity){
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    public static void enableWindow(Activity activity){
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
