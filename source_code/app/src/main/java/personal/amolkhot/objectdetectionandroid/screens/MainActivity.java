package personal.amolkhot.objectdetectionandroid.screens;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.android.Utils;

import personal.amolkhot.objectdetectionandroid.R;
import personal.amolkhot.objectdetectionandroid.utils.AppPermissions;

public class MainActivity extends Activity {

    private static String TAG = "MainActivity";
    Button btn_scan;
    TextView errorText;
    boolean isPermissionAvailable=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"OnCreate()");
        setContentView(R.layout.activity_main);
        initialize();
        setClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"OnResume()");
        checkCameraPermission();
    }

    private void initialize(){
        Log.i(TAG,"initialize()");
        btn_scan = findViewById(R.id.btn_scan);
        errorText = findViewById(R.id.errorText);
    }
    private void checkCameraPermission(){
        Log.i(TAG,"checkCameraPermission()");
        setButtonStates(AppPermissions.checkCameraPermission(this));
    }
    private void setButtonStates(int currentPermission){
        Log.i(TAG,"setButtonStateFromPermission('"+currentPermission+"')");
        if (currentPermission == PackageManager.PERMISSION_DENIED){
            isPermissionAvailable=false;
            btn_scan.setText(R.string.permission_btn_label);
            errorText.setVisibility(View.VISIBLE);
        }else {
            isPermissionAvailable=true;
            btn_scan.setText(R.string.scan_btn_label);
            errorText.setVisibility(View.GONE);
        }
    }
    void launchCameraActivity(){
        Log.i(TAG,"launchCameraActivity()");
        startActivity(new Intent(MainActivity.this, ObjectDetectorActivity.class));
    }
    private void setClickListeners(){
        Log.i(TAG,"setClickListeners()");
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPermissionAvailable) {
                    launchCameraActivity();
                }else {
                    AppPermissions.requestCameraPermission(MainActivity.this);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG,"onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==AppPermissions.CAMERA_PERMISSION_REQUEST_CODE) setButtonStates(grantResults[0]);
    }
}
