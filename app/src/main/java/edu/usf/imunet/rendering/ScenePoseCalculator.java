package edu.usf.imunet.rendering;

import com.google.ar.core.Pose;

import org.rajawali3d.math.Matrix;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

public final class ScenePoseCalculator {
    private static final String TAG = ScenePoseCalculator.class.getSimpleName();

    /**
     * Transformation from the Tango Area Description or Start of Service coordinate frames
     * to the OpenGL coordinate frame.
     * NOTE: Rajawali uses column-major for matrices.
     */
    public static final Matrix4 OPENGL_T_TANGO_WORLD = new Matrix4(new double[]{
            1, 0,  0, 0,
            0, 0, -1, 0,
            0, 1,  0, 0,
            0, 0,  0, 1
    });

    /**
     *  Transformation from the Tango RGB camera coordinate frame to the OpenGL camera frame.
     */
    public static final Matrix4 COLOR_CAMERA_T_OPENGL_CAMERA = new Matrix4(new double[]{
            1,  0,  0, 0,
            0, -1,  0, 0,
            0,  0, -1, 0,
            0,  0,  0, 1
    });

    /**
     *  Transformation for device rotation on 270 degrees.
     */
    public static final Matrix4 ROTATION_270_T_DEFAULT = new Matrix4(new double[]{
            0, 1, 0, 0,
            -1, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 1
    });

    /**
     *  Transformation for device rotation on 180 degrees.
     */
    public static final Matrix4 ROTATION_180_T_DEFAULT = new Matrix4(new double[]{
            -1,  0, 0, 0,
            0, -1, 0, 0,
            0,  0, 1, 0,
            0,  0, 0, 1
    });

    /**
     *  Transformation for device rotation on 90 degrees.
     */
    public static final Matrix4 ROTATION_90_T_DEFAULT = new Matrix4(new double[]{
            0, -1, 0, 0,
            1,  0, 0, 0,
            0,  0, 1, 0,
            0,  0, 0, 1
    });

    /**
     *  Transformation for device rotation on default orientation.
     */
    public static final Matrix4 ROTATION_0_T_DEFAULT = new Matrix4(new double[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    });

    public static final Matrix4 DEPTH_CAMERA_T_OPENGL_CAMERA = new Matrix4(new double[]{
            1,  0,  0, 0,
            0, -1,  0, 0,
            0,  0, -1, 0,
            0,  0,  0, 1
    });

    /**
     * Up vector in the Tango start of Service and Area Description frame.
     */
    public static final Vector3 TANGO_WORLD_UP = new Vector3(0, 0, 1);

    /**
     * Avoid instantiating the class since it will only be used statically.
     */
    private ScenePoseCalculator() {
    }

    /**
     * Converts from TangoPoseData to a Matrix4 for transformations.
     */

    /*
    public static Matrix4 tangoPoseToMatrix(TangoPoseData tangoPose) {
        Vector3 v = new Vector3(tangoPose.translation[0],
                tangoPose.translation[1], tangoPose.translation[2]);
        Quaternion q = new Quaternion(tangoPose.rotation[3], tangoPose.rotation[0],
                tangoPose.rotation[1], tangoPose.rotation[2]);
        // NOTE: Rajawali quaternions use a left-hand rotation around the axis convention.
        q.conjugate();
        Matrix4 m = new Matrix4();
        m.setAll(v, new Vector3(1, 1, 1), q);
        return m;
    }*/

    public static Matrix4 arPoseToMatrix(Pose pose) {
        float[] translation = pose.getTranslation();
        float[] rotation = pose.getRotationQuaternion();
        Vector3 v = new Vector3(translation[0],-1*translation[2], translation[1]);
        Quaternion q = new Quaternion(rotation[3], rotation[0],
                rotation[1], rotation[2]);
        // NOTE: Rajawali quaternions use a left-hand rotation around the axis convention.
        q.conjugate();
        Matrix4 m = new Matrix4();
        m.setAll(v, new Vector3(1, 1, 1), q);
        return m;
    }


    public static Matrix4 calculateProjectionMatrix(int width, int height, double fx, double fy,
                                                    double cx, double cy) {
        // Uses frustumM to create a projection matrix taking into account calibrated camera
        // intrinsic parameter.
        // Reference: http://ksimek.github.io/2013/06/03/calibrated_cameras_in_opengl/
        double near = 0.1;
        double far = 100;

        double xScale = near / fx;
        double yScale = near / fy;
        double xOffset = (cx - (width / 2.0)) * xScale;
        // Color camera's coordinates has y pointing downwards so we negate this term.
        double yOffset = -(cy - (height / 2.0)) * yScale;

        double m[] = new double[16];
        Matrix.frustumM(m, 0,
                xScale * -width / 2.0 - xOffset,
                xScale * width / 2.0 - xOffset,
                yScale * -height / 2.0 - yOffset,
                yScale * height / 2.0 - yOffset,
                near, far);
        return new Matrix4(m);
    }







}
