package edu.usf.imunet.rendering;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.view.MotionEvent;

import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.RajawaliRenderer;

import edu.usf.imunet.helper.FrustumAxes;
import edu.usf.imunet.helper.Grid;
import edu.usf.imunet.helper.TouchViewHandler;
import edu.usf.imunet.helper.Trajectory;

public class MotionRajawaliRenderer extends RajawaliRenderer {

    private static final float CAMERA_NEAR = 0.01f;
    private static final float CAMERA_FAR = 200f;

    private TouchViewHandler mTouchViewHandler;

    private FrustumAxes mFrustumAxes;
    private Grid mGrid;
    private Trajectory mTrajectory;
    private final AssetManager assetManager;
    public MotionRajawaliRenderer(Context context, AssetManager assetManager){
        super(context);
        this.assetManager = assetManager;
        mTouchViewHandler = new TouchViewHandler(mContext, getCurrentCamera());
    }
    public AssetManager getAssets() {
        return assetManager;
    }

    @Override
    protected void initScene(){
        mGrid = new Grid(100, 1, 1, 0xFFCCCCCC);
        mGrid.setPosition(0, -1.3f, 0);
        getCurrentScene().addChild(mGrid);

        mFrustumAxes = new FrustumAxes(3);
        getCurrentScene().addChild(mFrustumAxes);

        mTrajectory = new Trajectory(Color.RED, 1.0f);
        getCurrentScene().addChild(mTrajectory);

        getCurrentScene().setBackgroundColor(Color.WHITE);
        getCurrentCamera().setNearPlane(CAMERA_NEAR);
        getCurrentCamera().setFarPlane(CAMERA_FAR);
        getCurrentCamera().setFieldOfView(37.5);
    }

    /*
    public void updateCameraPose(TangoPoseData cameraPose){
        float[] rotation = cameraPose.getRotationAsFloats();
        float[] translation = cameraPose.getTranslationAsFloats();
        Vector3 curPosition = new Vector3(translation[0], translation[1], translation[2]);
        Quaternion quaternion = new Quaternion(rotation[3], rotation[0], rotation[1], rotation[2]);
        update(curPosition, quaternion);
    }*/

    public void updateCameraPoseFromMatrix(Matrix4 cameraMatrix){
        Vector3 curPosition = cameraMatrix.getTranslation();
        Quaternion quaternion = new Quaternion();
        quaternion.fromMatrix(cameraMatrix);
        update(curPosition, quaternion);
    }

    public void update(Vector3 curPosition, Quaternion quaternion){
        mTrajectory.addSegmentTo(curPosition);

        mFrustumAxes.setPosition(curPosition.x, curPosition.y, curPosition.z);

        //Conjugating the Quaternion is needed because Rajawali uses left handed convention for quaternions
        mFrustumAxes.setOrientation(quaternion.conjugate());
        mTouchViewHandler.updateCamera(curPosition, quaternion);
    }

    @Override
    public  void onOffsetsChanged(float v, float v1, float v2, float v3, int i, int i1){

    }

    @Override
    public void onTouchEvent(MotionEvent motionEvent){
        mTouchViewHandler.onTouchEvent(motionEvent);
    }
}

