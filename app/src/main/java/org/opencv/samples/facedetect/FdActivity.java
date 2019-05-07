package org.opencv.samples.facedetect;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.samples.utils.OpenCVUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;
import premar.tech.facerecognitionapp.BuildConfig;
import premar.tech.facerecognitionapp.R;
import premar.tech.facerecognitionapp.api.APIClient;
import premar.tech.facerecognitionapp.api.APIInterface;
import premar.tech.facerecognitionapp.api.model.FacialLogin;
import premar.tech.facerecognitionapp.api.model.ResponseMessage;
import premar.tech.facerecognitionapp.api.model.User;
import premar.tech.facerecognitionapp.ui.login.LoginActivity;
import premar.tech.facerecognitionapp.utils.UserDialogs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class FdActivity extends Activity implements CvCameraViewListener2 {

    private ImageView ivSelectedImagePreview;
    private Mat firstFoundFace;

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(150, 70, 164, 255); // RGBA
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mItemType;

    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    private DetectionBasedTracker  mNativeDetector;

    private int                    mDetectorType       = NATIVE_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private CameraBridgeViewBase   mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);

        Nammu.init(getApplicationContext());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }


        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        int num = Camera.getNumberOfCameras();
        if (num > 1) {
            mOpenCvCameraView.setCameraIndex(1);
        }
        mOpenCvCameraView.setCvCameraViewListener(this);

        ivSelectedImagePreview = findViewById(R.id.iv_selected_image_preview);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Nammu.askForPermission(this, Manifest.permission.CAMERA, new PermissionCallback() {

            @Override
            public void permissionGranted() {
                if (!OpenCVLoader.initDebug()) {
                    Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, FdActivity.this, mLoaderCallback);
                } else {
                    Log.d(TAG, "OpenCV library found inside package. Using it!");
                    mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                }
            }

            @Override
            public void permissionRefused() {
                Toast.makeText(FdActivity.this, "Cannot do face recognition without camera permissions!", Toast.LENGTH_SHORT).show();
                FdActivity.this.finish();
            }
        });

    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        // these lines of code rotate the opencv image in the right direction for portrait mode using
        // front facing camera
        Mat rotImage = Imgproc.getRotationMatrix2D(new Point(mRgba.cols() / 2,
                mRgba.rows() / 2), 90, 1.0);
        Imgproc.warpAffine(mRgba, mRgba, rotImage, mRgba.size());
        Imgproc.warpAffine(mGray, mGray, rotImage, mRgba.size());



        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null)
                mNativeDetector.detect(mGray, faces);
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        firstFoundFace = null;

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
            try {
                firstFoundFace = new Mat(mRgba, facesArray[i]);//mRgba.submat(facesArray[i].x, facesArray[i].y, facesArray[i].width, facesArray[i].height);

            } catch (Exception e) {}
        }

        onFaceFound(firstFoundFace);

        return mRgba;
    }

    private void onFaceFound(final Mat firstFoundFace) {
        if (firstFoundFace != null) {

            final Bitmap faceImageBitmap = OpenCVUtils.convertMatToBitMap(firstFoundFace);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ivSelectedImagePreview.setImageBitmap(faceImageBitmap);
                }
            });

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            faceImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            String faceImageBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP); // DEFAULT flag adds newline after every 76 xters, wasted my day!

            faceImageBase64 = "data:image/png;base64," + faceImageBase64;

            FacialLogin facialLogin = new FacialLogin();
            facialLogin.username = "kay";
            facialLogin.image = faceImageBase64;
            teachPeople(facialLogin);
        }
    }

    boolean called = false;

    private void teachPeople(FacialLogin facialLogin) {
        if(called) {
            return;
        }
        called = true;
        final SweetAlertDialog dialog = UserDialogs.showProgressDialog(this, "Loading...");
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseMessage> users = apiInterface.authenticateUser(facialLogin);
        users.enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                UserDialogs.hideProgressDialog(FdActivity.this);
//                Toast.makeText(SignupActivity.this, "Success: Eureka! \n" + response.message(), Toast.LENGTH_SHORT).show();
                ResponseMessage responseMessage = response.body();
                if (responseMessage != null && responseMessage.success) {
                    Toast.makeText(FdActivity.this, "Message : : " + responseMessage.message, Toast.LENGTH_SHORT).show();
                    Timber.d("RESPONSE MESSAGE : : " + responseMessage.message);
                } else {
                    Toast.makeText(FdActivity.this, "Yeeeeaah! These are the kinda crashes your mama used to warn you about.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                UserDialogs.hideProgressDialog(FdActivity.this);
                Toast.makeText(FdActivity.this, "Failed miserably!\n"+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mItemType   = menu.add(mDetectorName[mDetectorType]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        else if (item == mItemType) {
            int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
            item.setTitle(mDetectorName[tmpDetectorType]);
            setDetectorType(tmpDetectorType);
        }
        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                Log.i(TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }
}
