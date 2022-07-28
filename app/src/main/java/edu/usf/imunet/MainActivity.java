package edu.usf.imunet;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Range;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import edu.usf.imunet.R;

import edu.usf.imunet.helper.ArIMUConfig;
import edu.usf.imunet.helper.CameraPermissionHelper;
import edu.usf.imunet.helper.OutputDirectoryManager;
import edu.usf.imunet.helper.PeriodicScan;
import edu.usf.imunet.helper.PoseIMURecorder;
import edu.usf.imunet.helper.SnackbarHelper;
import edu.usf.imunet.rendering.BackgroundRenderer;
import edu.usf.imunet.rendering.MotionRajawaliRenderer;
import edu.usf.imunet.rendering.ScenePoseCalculator;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.CameraConfig;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import org.rajawali3d.math.Matrix4;
import org.rajawali3d.scene.ASceneFrameCallback;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.flex.FlexDelegate;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String LOG_TAG = MainActivity.class.getName();
    public static final String INTENT_EXTRA_CONFIG = "config";
    public static final String INTENT_EXTRA_ADF_NAME = "adf_name";
    public static final String INTENT_EXTRA_ADF_UUID = "adf_uuid";

    private static final int REQUEST_CODE_WRITE_EXTERNAL = 1001;
    private static final int REQUEST_CODE_CAMERA = 1002;
    private static final int REQUEST_CODE_ACCESS_WIFI = 1003;
    private static final int REQUEST_CODE_CHANGE_WIFI = 1004;
    private static final int REQUEST_CODE_COARSE_LOCATION = 1005;
    private static final int REQUEST_CODE_AREA_LEARNING = 1006;
    private static final int REQUEST_CODE_FINE_LOCATION = 1007;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RESULT_CODE_PICK_ADF = 2001;

    private static final int INVALID_TEXTURE_ID = 0;
//    int mRenderedTexture = TangoCameraIntrinsics.TANGO_CAMERA_FISHEYE;

    int mRenderedTexture = -1;

    private com.google.ar.core.Camera camera;

    private ArrayList<String> mAdfNames = new ArrayList<>();
    private ArrayList<String> mAdfUuids = new ArrayList<>();

    private Session session;
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    private boolean installRequested;
    private PoseIMURecorder mRecorder;
    private OutputDirectoryManager mOutputDirectoryManager;
    private MotionRajawaliRenderer mRenderer;
    private org.rajawali3d.surface.RajawaliSurfaceView mSurfaceView;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mOrientation;

    private float mInitialStepCount = -1;

    private PeriodicScan wifi_scanner_;
    private WifiManager mWifiMangerRef;
    private BroadcastReceiver mWifiScanReceiverRef;

    // Gyroscope
    private TextView mLabelRx;
    private TextView mLabelRy;
    private TextView mLabelRz;
    // Accelerometer
    private TextView mLabelAx;
    private TextView mLabelAy;
    private TextView mLabelAz;


    private boolean isAccessingGyro = false;
    private boolean isAccessingAcce = false;
    private boolean isAccessingOri = false;

    static final int ROTATION_SENSOR = Sensor.TYPE_GAME_ROTATION_VECTOR;

    private Button mStartStopButton;

    private int mCameraToDisplayRotation = 0;



    private AtomicBoolean mIsConnected = new AtomicBoolean(false);
    private AtomicBoolean mIsTangoInitialized = new AtomicBoolean(false);
    private AtomicBoolean mIsRecording = new AtomicBoolean(false);
    private AtomicBoolean mIsLocalizedToADF = new AtomicBoolean(false);

    private boolean mStoragePermissionGranted = false;
    private boolean mCameraPermissionGranted = false;
    private boolean mAccessWifiPermissionGranted = false;
    private boolean mChangeWifiPermissionGranted = false;
    private boolean mCoarseLocationPermissionGranted = false;
    private boolean mFineLocationPermissionGranted = false;
    private boolean hasSetTextureNames = false;

    private AtomicInteger mLocalizeCounter = new AtomicInteger(0);
    private BackgroundRenderer backgroundRenderer;
    Frame frame;

    private int gyro_sensors_call = 0;
    private int acce_sensors_call = 0;
    private int ori_sensors_call = 0;
    private int acce_down_sample_rate = 13;
    private int ori_down_sample_rate = 7;



    ArIMUConfig mConfig = new ArIMUConfig();
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView =  findViewById(R.id.gl_surface_view);
        mRenderer = new MotionRajawaliRenderer(this , getAssets());
        installRequested = false;
        FlexDelegate delegate = new FlexDelegate();
        Interpreter.Options options = new Interpreter.Options().addDelegate(delegate);


        setupRenderer();

        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if (displayManager != null) {
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {

                }

                @Override
                public void onDisplayRemoved(int displayId) {
                    synchronized (this) {
                        setAndroidOrientation();
                    }
                }

                @Override
                public void onDisplayChanged(int displayId) {

                }
            }, null);
        }



        // initialize IMU sensor
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mOrientation = mSensorManager.getDefaultSensor(ROTATION_SENSOR);

        // initialize UI widgets
        mLabelRx = (TextView) findViewById(R.id.label_rx);
        mLabelRy = (TextView) findViewById(R.id.label_ry);
        mLabelRz = (TextView) findViewById(R.id.label_rz);
        mLabelAx = (TextView) findViewById(R.id.label_ax);
        mLabelAy = (TextView) findViewById(R.id.label_ay);
        mLabelAz = (TextView) findViewById(R.id.label_az);



        mStartStopButton = (Button) findViewById(R.id.button_start_stop);



        updateConfig();


    }
    /*
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.option_menu, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_setting:
                    if (mIsRecording.get()) {
                        break;
                    }
                    Intent intent = new Intent(this, PrefActivity.class);
                    intent.putExtra(INTENT_EXTRA_ADF_NAME, mAdfNames);
                    intent.putExtra(INTENT_EXTRA_ADF_UUID, mAdfUuids);
                    startActivity(intent);
                    break;
            }
            return false;
        }

        @Override
        public boolean onPrepareOptionsMenu(Menu menu) {
            if (mIsRecording.get()) {
                menu.getItem(0).setEnabled(false);
            } else {
                menu.getItem(0).setEnabled(true);
            }
            return true;
        }
    */
    @Override
    protected void onPause() {
        if (session != null) {
            session.pause();
        }
        super.onPause();
        if (mIsRecording.get()) {
            stopRecording();
        }
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mGyroscope);

        mSensorManager.unregisterListener(this, mOrientation);


        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStoragePermissionGranted = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CODE_WRITE_EXTERNAL);
        mCameraPermissionGranted = checkPermission(Manifest.permission.CAMERA, REQUEST_CODE_CAMERA);
        mAccessWifiPermissionGranted = checkPermission(Manifest.permission.ACCESS_WIFI_STATE, REQUEST_CODE_ACCESS_WIFI);
        mChangeWifiPermissionGranted = checkPermission(Manifest.permission.CHANGE_WIFI_STATE, REQUEST_CODE_CHANGE_WIFI);
        mCoarseLocationPermissionGranted = checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_CODE_COARSE_LOCATION);

        updateConfig();
        /*
        if (mIsTangoInitialized.get()) {
            updateADFList();
        }*/

        mStartStopButton.setText(R.string.start_title);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_FASTEST);

        registerReceiver(mWifiScanReceiverRef, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        // prevent screen lock
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }

                // Create the session.
                session = new Session(/* context= */ this);
            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (message != null) {
                messageSnackbarHelper.showError(this, message);
                Log.e(TAG, "Exception creating session", exception);
                return;
            }
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            configureSession();
            // To record a live camera session for later playback, call
            // `session.startRecording(recorderConfig)` at anytime. To playback a previously recorded AR
            // session instead of using the live camera feed, call
            // `session.setPlaybackDataset(playbackDatasetPath)` before calling `session.resume()`. To
            // learn more about recording and playback, see:
            // https://developers.google.com/ar/develop/java/recording-and-playback
            session.resume();
        } catch (CameraNotAvailableException e) {
            messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
            session = null;
            return;
        }
    }

    private void updateConfig() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mConfig.setPoseEnabled(pref.getBoolean("pref_pose_enabled", true));
        mConfig.setFileEnabled(pref.getBoolean("pref_file_enabled", true));
        mConfig.setFolderPrefix(pref.getString("pref_folder_prefix", ""));
        mConfig.setWifiEnabled(pref.getBoolean("pref_wifi_enabled", true));
        mConfig.setContinuesWifiScan(pref.getBoolean("pref_auto_wifi_enabled", false));
        mConfig.setADFEnabled(pref.getBoolean("pref_adf_enabled", false));
        mConfig.setAreaLearningMode(pref.getBoolean("pref_al_mode", false));
        mConfig.setADFUuid(pref.getString("pref_adf_uuid", ""));
        mConfig.setNumRequestsPerScan(Integer.valueOf(pref.getString("pref_num_requests", "1")));
        mConfig.setWifiScanInterval(Integer.valueOf(pref.getString("pref_scan_interval", "1")));

        int index = mAdfUuids.indexOf(mConfig.getADFUuid());
        if (index >= 0 && index < mAdfNames.size()) {
            mConfig.setADFName(mAdfNames.get(index));
        } else {
            mConfig.setADFName(mConfig.getADFUuid());
        }
    }

    private void showAlertAndStop(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(text)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stopRecording();
                            }
                        }).show();
            }
        });
    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startNewRecording() {
        if (!mStoragePermissionGranted) {
            showAlertAndStop("Storage permission not granted");
            return;
        }
        if (!mCameraPermissionGranted) {
            showAlertAndStop("Camera permission not granted");
            return;
        }
        if (mConfig.getWifiEnabled() && (!mAccessWifiPermissionGranted || !mChangeWifiPermissionGranted)) {
            showAlertAndStop("Wifi permission not granted");
            return;
        }
        if (mConfig.getWifiEnabled() && !mCoarseLocationPermissionGranted) {
            showAlertAndStop("Location permission not granted");
            return;
        }

//        if (!mFineLocationPermissionGranted){
//            showAlertAndStop("Fine location permissions not granted");
//            return;
//        }

        // initialize Wifi


        if (mConfig.getPoseEnabled()) {

            // initialize tango service
            synchronized (this) {
                startupTango();
                mIsConnected.set(true);
            }
        }
        // initialize recorder
        if (mConfig.getFileEnabled()) {
            try {
                mOutputDirectoryManager = new OutputDirectoryManager(mConfig.getFolderPrefix());
                mRecorder = new PoseIMURecorder(mOutputDirectoryManager.getOutputDirectory() , this);
            } catch (FileNotFoundException e) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.alert_title)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stopRecording();
                            }
                        }).show();
                e.printStackTrace();
            }
        }
        mInitialStepCount = -1.0f;
        mIsRecording.set(true);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStartStopButton.setText(R.string.stop_title);
            }
        });
    }

    private void stopRecording() {
        mIsRecording.set(false);

        if (mRecorder != null) {
            mRecorder.endFiles();
        }

        if (mConfig.getWifiEnabled()) {
            if (mConfig.getContinuesWifiScan()) {
                wifi_scanner_.terminate();
            }

        }

        if (mConfig.getPoseEnabled()) {
            synchronized (this) {
                try {

                    session.stopRecording();
                    mIsConnected.set(false);
                    mIsLocalizedToADF.set(false);
                    mLocalizeCounter.set(0);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        resetUI();
        showToast("Stopped");
    }

    public void startStopRecording(View view) {
        if (!mIsRecording.get()) {
            startNewRecording();
        } else {
            stopRecording();
        }
    }

    private void setupRenderer() {
        // motion renderer
        mSurfaceView.setEGLContextClientVersion(2);
        mRenderer.getCurrentScene().registerFrameCallback(new ASceneFrameCallback() {
            @Override
            public void onPreFrame(long sceneTime, double deltaTime) {
                synchronized (MainActivity.this) {

                    // Don't execute any tango API actions if we're not connected to the service
                    if (!mIsConnected.get()) {
                        return;
                    }



                    // Update current camera pose

                    try {

                        if (!hasSetTextureNames) {
                            backgroundRenderer = new BackgroundRenderer(mRenderer);
                            session.setCameraTextureNames(
                                    new int[] {backgroundRenderer.getCameraColorTexture().getTextureId()});
                            hasSetTextureNames = true;
                        }



                        frame = session.update();


                    } catch (CameraNotAvailableException e) {
                        Log.e(TAG, "Camera not available during onDrawFrame", e);
                        messageSnackbarHelper.showError(MainActivity.this, "Camera not available. Try restarting the app.");
                        return;
                    }
                    camera = frame.getCamera();


                    TrackingState state = camera.getTrackingState();

                    if (state == TrackingState.TRACKING){

                        if (mIsRecording.get()){
                            if (mRecorder != null){

                                long time = frame.getTimestamp();
                                mRecorder.addAndroidPoseRecord(frame.getAndroidSensorPose() , time);
                                mRecorder.addDisplayPoseRecord(camera.getDisplayOrientedPose() , time);
                                mRecorder.addPoseRecord(camera.getPose() , time);
                            }
                            Pose lastFramePose = frame.getAndroidSensorPose();
                            Matrix4 lastFrameMatrix = ScenePoseCalculator.arPoseToMatrix(lastFramePose);

                            lastFrameMatrix.leftMultiply(ScenePoseCalculator.OPENGL_T_TANGO_WORLD);
                            mRenderer.updateCameraPoseFromMatrix(lastFrameMatrix);
                        }
                    }else{
                        //  Toast.makeText(MainActivity.this , state.toString() , Toast.LENGTH_SHORT).show();
                    }



                }
            }

            @Override
            public boolean callPreFrame() {
                return true;
            }

            @Override
            public void onPreDraw(long sceneTime, double deltaTime) {

            }

            @Override
            public void onPostFrame(long sceneTime, double deltaTime) {

            }
        });

        mSurfaceView.setSurfaceRenderer(mRenderer);
    }

    private void resetUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStartStopButton.setText(R.string.start_title);

            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        mRenderer.onTouchEvent(motionEvent);
        return true;
    }

    private void setAndroidOrientation() {
        Display display = getWindowManager().getDefaultDisplay();
        Camera.CameraInfo depthCameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(1, depthCameraInfo);

        int depthCameraRotation = Surface.ROTATION_0;
        switch (depthCameraInfo.orientation) {
            case 90:
                depthCameraRotation = Surface.ROTATION_90;
                break;
            case 180:
                depthCameraRotation = Surface.ROTATION_180;
                break;
            case 270:
                depthCameraRotation = Surface.ROTATION_270;
                break;
        }

        mCameraToDisplayRotation = display.getRotation() - depthCameraRotation;
        if (mCameraToDisplayRotation < 0) {
            mCameraToDisplayRotation += 4;
        }
    }

    private void startupTango() {


        mIsLocalizedToADF.set(false);
        mLocalizeCounter.set(0);


    }

    // receive IMU data
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        long timestamp = event.timestamp;
        float[] values = {0.0f, 0.0f, 0.0f, 0.0f};
        final Boolean mIsWriteFile = mConfig.getFileEnabled();
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (mIsRecording.get() && mIsWriteFile) {
                System.arraycopy(event.values, 0, values, 0, 3);
                if (acce_sensors_call % acce_down_sample_rate == 0){
                    if (!isAccessingAcce){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mLabelAx.setText(String.format(Locale.US, "%.6f", event.values[0]));
                                mLabelAy.setText(String.format(Locale.US, "%.6f", event.values[1]));
                                mLabelAz.setText(String.format(Locale.US, "%.6f", event.values[2]));
                            }
                        });
                        mRecorder.addIMURecord(timestamp, values, PoseIMURecorder.ACCELEROMETER, true); }
                    //Log.d(TAG, "ori_sensors_call: " + ori_sensors_call);
                }
                mRecorder.addIMURecord(timestamp, values, PoseIMURecorder.ACCELEROMETER, false);
                acce_sensors_call = acce_sensors_call +1;
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if (mIsRecording.get() && mIsWriteFile) {
                System.arraycopy(event.values, 0, values, 0, 3);
                if (gyro_sensors_call % acce_down_sample_rate == 0){
                    if(!isAccessingGyro){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mLabelRx.setText(String.format(Locale.US, "%.6f", event.values[0]));
                                mLabelRy.setText(String.format(Locale.US, "%.6f", event.values[1]));
                                mLabelRz.setText(String.format(Locale.US, "%.6f", event.values[2]));
                            }
                        });
                        mRecorder.addIMURecord(timestamp, values, PoseIMURecorder.GYROSCOPE, true);}
                }
                gyro_sensors_call = gyro_sensors_call +1;
                mRecorder.addIMURecord(timestamp, values, PoseIMURecorder.GYROSCOPE, false);
            }
        } else if (event.sensor.getType() == ROTATION_SENSOR) {
            if (mIsRecording.get() && mIsWriteFile) {
                System.arraycopy(event.values, 0, values, 0, 4);
                if (ori_sensors_call % ori_down_sample_rate == 0){
                    if(!isAccessingOri){
                        mRecorder.addIMURecord(timestamp, values, PoseIMURecorder.ROTATION_VECTOR, true);}
                }
                ori_sensors_call = ori_sensors_call +1;
                mRecorder.addIMURecord(timestamp, values, PoseIMURecorder.ROTATION_VECTOR, false);
            }
        }
    }




    private boolean checkPermission(String permission, int request_code) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission},
                    request_code);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_WRITE_EXTERNAL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mStoragePermissionGranted = true;
                }
                break;
            case REQUEST_CODE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCameraPermissionGranted = true;
                }
                break;
            case REQUEST_CODE_ACCESS_WIFI:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mAccessWifiPermissionGranted = true;
                }
                break;
            case REQUEST_CODE_CHANGE_WIFI:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mChangeWifiPermissionGranted = true;
                }
                break;
            case REQUEST_CODE_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCoarseLocationPermissionGranted = true;
                }
                break;
            case REQUEST_CODE_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mFineLocationPermissionGranted = true;
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_AREA_LEARNING) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Area learning permission required.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void configureSession() {
        /*
    CameraConfigFilter filter = new CameraConfigFilter(session);

  // Return only camera configs that target 30 fps camera capture frame rate.
    filter.setTargetFps(EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_60));

  // Return only camera configs that will not use the depth sensor.
    filter.setDepthSensorUsage(EnumSet.of(CameraConfig.DepthSensorUsage.DO_NOT_USE));

  // Get list of configs that match filter settings.
  // In this case, this list is guaranteed to contain at least one element,
  // because both TargetFps.TARGET_FPS_30 and DepthSensorUsage.DO_NOT_USE
  // are supported on all ARCore supported devices.
    List<CameraConfig> cameraConfigList = session.getSupportedCameraConfigs(filter);

  // Use element 0 from the list of returned camera configs. This is because
  // it contains the camera config that best matches the specified filter
  // settings.
    session.setCameraConfig(cameraConfigList.get(0));
*/
        CameraConfig config_ = session.getCameraConfig();

        Range<Integer> c = config_.getFpsRange();
        session.setCameraConfig(config_);
        Config config = session.getConfig();
        //config.set
        config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        } else {
            config.setDepthMode(Config.DepthMode.DISABLED);
        }
        session.configure(config);
    }

}