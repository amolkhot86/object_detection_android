package personal.amolkhot.objectdetectionandroid.screens;

import android.app.Activity;
import android.os.Bundle;

import org.opencv.android.JavaCameraView;

import personal.amolkhot.objectdetectionandroid.R;
import personal.amolkhot.objectdetectionandroid.utils.RectangleFinder;

public class ObjectDetectorActivity extends Activity {
    private static String TAG = "ObjectDetectorActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detector);
        RectangleFinder.init(new RectangleFinder(this,(JavaCameraView)findViewById(R.id.cameraViewer)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        RectangleFinder.getInstance().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RectangleFinder.getInstance().onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RectangleFinder.getInstance().onResume();
    }
}
