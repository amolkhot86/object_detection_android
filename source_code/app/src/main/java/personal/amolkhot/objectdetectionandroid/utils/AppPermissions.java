package personal.amolkhot.objectdetectionandroid.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by amol.khot on 25-Nov-18.
 */

public class AppPermissions {
    public final static int CAMERA_PERMISSION_REQUEST_CODE=1;
    public static int checkCameraPermission(Context context){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
    }
    public static void requestCameraPermission(Context context){
        ActivityCompat.requestPermissions((Activity)context, new String[] {Manifest.permission.CAMERA},CAMERA_PERMISSION_REQUEST_CODE);
    }
}
