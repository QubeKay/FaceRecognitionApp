package org.opencv.samples.utils;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class OpenCVUtils {

    private static String TAG = "OpenCVUtils";

    public static Mat convertBitMapToMat(Bitmap input){
        Mat mat = new Mat();
        Bitmap bmp32 = input.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);

        return mat;
    }

    public static Bitmap convertMatToBitMap(Mat input){
        Bitmap bmp = null;
        Mat rgb = new Mat();
        Imgproc.cvtColor(input, rgb, Imgproc.COLOR_BGR2RGB);

        try {
            bmp = Bitmap.createBitmap(rgb.cols(), rgb.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgb, bmp);
        }
        catch (CvException e){
            Log.d(TAG,e.getMessage());
        }
        return bmp;
    }
}
