package personal.amolkhot.objectdetectionandroid.utils;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by amol.khot on 25-Nov-18.
 */

public class RectangleFinder implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG = "RectangleFinder";

    private static RectangleFinder thisInstance;

    Context thisContext;
    CameraBridgeViewBase cameraViewer;

    BaseLoaderCallback baseLoaderCallback;
    Mat blackAndWhiteImg, hsvIMG, lrrIMG, urrIMG, zoomOutImg, zoomInImg, cIMG, hovIMG;
    MatOfPoint2f approxCurve;

    private static int threshold=100;

    public static void init(RectangleFinder rectangleFinder){
        Log.i(TAG,"init()");
        thisInstance=rectangleFinder;
        thisInstance.initialize();
    }
    public static RectangleFinder getInstance(){return thisInstance;}

    private RectangleFinder(){}

    public RectangleFinder(Context context,JavaCameraView cameraBridgeViewBase){
        Log.i(TAG,"RectangleFinder()");
        thisContext=context;
        cameraViewer=cameraBridgeViewBase;
    }
    private void initialize(){
        Log.i(TAG,"initialize()");
        cameraViewer.setVisibility(SurfaceView.VISIBLE);
        cameraViewer.setCvCameraViewListener(this);
        baseLoaderCallback = new BaseLoaderCallback(thisContext) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        Log.v("aashari-log", "Loader interface success");
                        blackAndWhiteImg = new Mat();
                        zoomOutImg = new Mat();
                        hsvIMG = new Mat();
                        lrrIMG = new Mat();
                        urrIMG = new Mat();
                        zoomInImg = new Mat();
                        cIMG = new Mat();
                        hovIMG = new Mat();
                        approxCurve = new MatOfPoint2f();
                        cameraViewer.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.i(TAG,"onCameraViewStarted()");
    }

    @Override
    public void onCameraViewStopped() {
        Log.i(TAG,"onCameraViewStopped()");
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.i(TAG,"onCameraFrame()");
        return processFrame(inputFrame);
    }

    public void onPause(){
        Log.i(TAG,"onPause()");
        if(thisInstance.cameraViewer != null){
            thisInstance.cameraViewer.disableView();
        }
    }
    public void onDestroy(){
        Log.i(TAG,"onDestroy()");
        if(thisInstance.cameraViewer != null){
            thisInstance.cameraViewer.disableView();
        }
    }
    public void onResume(){
        Log.i(TAG,"onResume()");
        if (OpenCVLoader.initDebug()) thisInstance.baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
    }

    private static double getAngleBetweenPoints(Point point1, Point point2, Point point0) {
        Log.i(TAG,"getAngleBetweenPoints()");
        double dx1 = point1.x - point0.x;
        double dy1 = point1.y - point0.y;
        double dx2 = point2.x - point0.x;
        double dy2 = point2.y - point0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    private void drawRectangle(Mat outputImage, MatOfPoint contour) {
        Log.i(TAG,"drawRectangle()");
        Rect r = Imgproc.boundingRect(contour);
        Imgproc.rectangle(outputImage,new Point(r.x,r.y),new Point(r.x+r.width,r.y+r.height),new Scalar(0, 255, 0),2);
    }

    private Mat processFrame(CameraBridgeViewBase.CvCameraViewFrame thisFrame){
        Log.i(TAG,"processFrame()");

        // Convert frame to Gray Color for processing
        Mat grayImg = thisFrame.gray();
        // Keep the Original frame to draw
        Mat outputImg = thisFrame.rgba();
        // Zoom Out & Zoom In image to get clear edges
        Imgproc.pyrDown(grayImg, zoomOutImg, new Size(grayImg.cols() / 2, grayImg.rows() / 2));
        Imgproc.pyrUp(zoomOutImg, zoomInImg, grayImg.size());

        // Canny is used to detect Edges [Generates a mask (bright lines representing the edges on a black background).]
        Imgproc.Canny(zoomInImg, blackAndWhiteImg, 0, threshold);

        // Dilate expand the edges of the images
        Imgproc.dilate(blackAndWhiteImg, blackAndWhiteImg, new Mat(), new Point(-1, 1), 1);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        cIMG = blackAndWhiteImg.clone();

        Imgproc.findContours(cIMG, contours, hovIMG, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        int totalRectangleFound =0;
        for (MatOfPoint cnt : contours) {

            MatOfPoint2f curve = new MatOfPoint2f(cnt.toArray());

            Imgproc.approxPolyDP(curve, approxCurve, 0.02 * Imgproc.arcLength(curve, true), true);

            int numberVertices = (int) approxCurve.total();

            double contourArea = Imgproc.contourArea(cnt);

            if (Math.abs(contourArea) < 100) {
                continue;
            }

            //Rectangle detected
            if (numberVertices >= 4 && numberVertices <= 6) {
                totalRectangleFound++;
                List<Double> cos = new ArrayList<>();

                for (int j = 2; j < numberVertices + 1; j++) {
                    cos.add(getAngleBetweenPoints(approxCurve.toArray()[j % numberVertices], approxCurve.toArray()[j - 2], approxCurve.toArray()[j - 1]));
                }

                Collections.sort(cos);

                double mincos = cos.get(0);
                double maxcos = cos.get(cos.size() - 1);

                if (numberVertices == 4 && mincos >= -0.1 && maxcos <= 0.3) {
                    drawRectangle(outputImg, cnt);
                }
            }
        }
        return outputImg;
    }
}
