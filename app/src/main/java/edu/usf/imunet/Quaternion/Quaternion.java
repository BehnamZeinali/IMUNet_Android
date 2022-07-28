package edu.usf.imunet.Quaternion;

public class Quaternion extends Vector4f {

    /**
     * Rotation matrix that contains the same rotation as the Quaternion in a 4x4 homogenised rotation matrix.
     * Remember that for performance reasons, this matrix is only updated, when it is accessed and not on every change
     * of the quaternion-values.
     */


    /**
     * This variable is used to synchronise the rotation matrix with the current quaternion values. If someone has
     * changed the
     * quaternion numbers then the matrix will need to be updated. To save on processing we only really want to update
     * the matrix when someone wants to fetch it, instead of whenever someone sets a quaternion value.
     */
    private boolean dirty = false;

    private Vector4f tmpVector = new Vector4f();
    private Quaternion tmpQuaternion;

    /**
     * Creates a new Quaternion object and initialises it with the identity Quaternion
     */
    public Quaternion() {
        super();
        loadIdentityQuat();
    }

    public Quaternion(float x, float y, float z, float w) {
        super();
        points[0] = x;
        points[1] = y;
        points[2] = z;
        points[3] = w;
    }

    /**
     * Normalise this Quaternion into a unity Quaternion.
     */
    public void normalise() {
        this.dirty = true;
        float mag = (float) Math.sqrt(points[3] * points[3] + points[0] * points[0] + points[1] * points[1] + points[2]
                * points[2]);
        points[3] = points[3] / mag;
        points[0] = points[0] / mag;
        points[1] = points[1] / mag;
        points[2] = points[2] / mag;
    }

    @Override
    public void normalize() {
        normalise();
    }

    /**
     * Copies the values from the given quaternion to this one
     *
     * @param quat The quaternion to copy from
     */
    public void set(Quaternion quat) {
        this.dirty = true;
        copyVec4(quat);
    }

    /**
     * Multiply this quaternion by the input quaternion and store the result in the out quaternion
     *
     * @param input
     * @param output
     */
    public void multiplyByQuat(Quaternion input, Quaternion output) {

        if (input != output) {
            output.points[3] = (points[3] * input.points[3] - points[0] * input.points[0] - points[1] * input.points[1] - points[2]
                    * input.points[2]); //w = w1w2 - x1x2 - y1y2 - z1z2
            output.points[0] = (points[3] * input.points[0] + points[0] * input.points[3] + points[1] * input.points[2] - points[2]
                    * input.points[1]); //x = w1x2 + x1w2 + y1z2 - z1y2
            output.points[1] = (points[3] * input.points[1] + points[1] * input.points[3] + points[2] * input.points[0] - points[0]
                    * input.points[2]); //y = w1y2 + y1w2 + z1x2 - x1z2
            output.points[2] = (points[3] * input.points[2] + points[2] * input.points[3] + points[0] * input.points[1] - points[1]
                    * input.points[0]); //z = w1z2 + z1w2 + x1y2 - y1x2
        } else {
            tmpVector.points[0] = input.points[0];
            tmpVector.points[1] = input.points[1];
            tmpVector.points[2] = input.points[2];
            tmpVector.points[3] = input.points[3];

            output.points[3] = (points[3] * tmpVector.points[3] - points[0] * tmpVector.points[0] - points[1]
                    * tmpVector.points[1] - points[2] * tmpVector.points[2]); //w = w1w2 - x1x2 - y1y2 - z1z2
            output.points[0] = (points[3] * tmpVector.points[0] + points[0] * tmpVector.points[3] + points[1]
                    * tmpVector.points[2] - points[2] * tmpVector.points[1]); //x = w1x2 + x1w2 + y1z2 - z1y2
            output.points[1] = (points[3] * tmpVector.points[1] + points[1] * tmpVector.points[3] + points[2]
                    * tmpVector.points[0] - points[0] * tmpVector.points[2]); //y = w1y2 + y1w2 + z1x2 - x1z2
            output.points[2] = (points[3] * tmpVector.points[2] + points[2] * tmpVector.points[3] + points[0]
                    * tmpVector.points[1] - points[1] * tmpVector.points[0]); //z = w1z2 + z1w2 + x1y2 - y1x2
        }
    }

    public static Quaternion qMultiply(Quaternion input, Quaternion input_0) {
            Quaternion output = null;



            output.points[3] = (input_0.points[3] * input.points[3] - input_0.points[0] * input.points[0] - input_0.points[1]
                    * input.points[1] - input_0.points[2] * input.points[2]); //w = w1w2 - x1x2 - y1y2 - z1z2
            output.points[0] = (input_0.points[3] * input.points[0] + input_0.points[0] * input.points[3] + input_0.points[1]
                    * input.points[2] - input_0.points[2] * input.points[1]); //x = w1x2 + x1w2 + y1z2 - z1y2
            output.points[1] = (input_0.points[3] * input.points[1] + input_0.points[1] * input.points[3] + input_0.points[2]
                    * input.points[0] - input_0.points[0] * input.points[2]); //y = w1y2 + y1w2 + z1x2 - x1z2
            output.points[2] = (input_0.points[3] * input.points[2] + input_0.points[2] * input.points[3] + input_0.points[0]
                    * input.points[1] - input_0.points[1] * input.points[0]); //z = w1z2 + z1w2 + x1y2 - y1x2

        return output;
    }

    public void conjugate(){
        points[0]= -points[0];
        points[1]= -points[1];
        points[2]= -points[2];
    }

    /**
     * Multiply this quaternion by the input quaternion and store the result in the out quaternion
     *
     * @param input
     * @param output
     */
    public void multiplyByQuat(Quaternion input) {
        this.dirty = true;
        if(tmpQuaternion == null) tmpQuaternion = new Quaternion();
        tmpQuaternion.copyVec4(this);
        multiplyByQuat(input, tmpQuaternion);
        this.copyVec4(tmpQuaternion);
    }

    /**
     * Multiplies this Quaternion with a scalar
     *
     * @param scalar the value that the vector should be multiplied with
     */
    public void multiplyByScalar(float scalar) {
        this.dirty = true;
        multiplyByScalar(scalar);
    }

    /**
     * Add a quaternion to this quaternion
     *
     * @param input The quaternion that you want to add to this one
     */
    public void addQuat(Quaternion input) {
        this.dirty = true;
        addQuat(input, this);
    }

    /**
     * Add this quaternion and another quaternion together and store the result in the output quaternion
     *
     * @param input The quaternion you want added to this quaternion
     * @param output The quaternion you want to store the output in.
     */
    public void addQuat(Quaternion input, Quaternion output) {
        output.setX(getX() + input.getX());
        output.setY(getY() + input.getY());
        output.setZ(getZ() + input.getZ());
        output.setW(getW() + input.getW());
    }

    /**
     * Subtract a quaternion to this quaternion
     *
     * @param input The quaternion that you want to subtracted from this one
     */
    public void subQuat(Quaternion input) {
        this.dirty = true;
        subQuat(input, this);
    }

    /**
     * Subtract another quaternion from this quaternion and store the result in the output quaternion
     *
     * @param input The quaternion you want subtracted from this quaternion
     * @param output The quaternion you want to store the output in.
     */
    public void subQuat(Quaternion input, Quaternion output) {
        output.setX(getX() - input.getX());
        output.setY(getY() - input.getY());
        output.setZ(getZ() - input.getZ());
        output.setW(getW() - input.getW());
    }


    /**
     * Get an axis angle representation of this quaternion.
     *
     * @param output Vector4f axis angle.
     */
    public void toAxisAngle(Vector4f output) {
        if (getW() > 1) {
            normalise(); // if w>1 acos and sqrt will produce errors, this cant happen if quaternion is normalised
        }
        float angle = 2 * (float) Math.toDegrees(Math.acos(getW()));
        float x;
        float y;
        float z;

        float s = (float) Math.sqrt(1 - getW() * getW()); // assuming quaternion normalised then w is less than 1, so term always positive.
        if (s < 0.001) { // test to avoid divide by zero, s is always positive due to sqrt
            // if s close to zero then direction of axis not important
            x = points[0]; // if it is important that axis is normalised then replace with x=1; y=z=0;
            y = points[1];
            z = points[2];
        } else {
            x = points[0] / s; // normalise axis
            y = points[1] / s;
            z = points[2] / s;
        }

        output.points[0] = x;
        output.points[1] = y;
        output.points[2] = z;
        output.points[3] = angle;
    }

    /**
     * Returns the heading, attitude and bank of this quaternion as euler angles in the double array respectively
     *
     * @return An array of size 3 containing the euler angles for this quaternion
     */
    public double[] toEulerAngles() {
        double[] ret = new double[3];

        ret[0] = Math.atan2(2 * points[1] * getW() - 2 * points[0] * points[2], 1 - 2 * (points[1] * points[1]) - 2
                * (points[2] * points[2])); // atan2(2*qy*qw-2*qx*qz , 1 - 2*qy2 - 2*qz2)
        ret[1] = Math.asin(2 * points[0] * points[1] + 2 * points[2] * getW()); // asin(2*qx*qy + 2*qz*qw)
        ret[2] = Math.atan2(2 * points[0] * getW() - 2 * points[1] * points[2], 1 - 2 * (points[0] * points[0]) - 2
                * (points[2] * points[2])); // atan2(2*qx*qw-2*qy*qz , 1 - 2*qx2 - 2*qz2)

        return ret;
    }

    /**
     * Sets the quaternion to an identity quaternion of 0,0,0,1.
     */
    public void loadIdentityQuat() {
        this.dirty = true;
        setX(0);
        setY(0);
        setZ(0);
        setW(1);
    }

    @Override
    public String toString() {
        return "{X: " + getX() + ", Y:" + getY() + ", Z:" + getZ() + ", W:" + getW() + "}";
    }





    /**
     * Set this quaternion from axis angle values. All rotations are in degrees.
     *
     * @param azimuth The rotation around the z axis
     * @param pitch The rotation around the y axis
     * @param roll The rotation around the x axis
     */
    public void setEulerAngle(float azimuth, float pitch, float roll) {

        double heading = Math.toRadians(roll);
        double attitude = Math.toRadians(pitch);
        double bank = Math.toRadians(azimuth);

        double c1 = Math.cos(heading / 2);
        double s1 = Math.sin(heading / 2);
        double c2 = Math.cos(attitude / 2);
        double s2 = Math.sin(attitude / 2);
        double c3 = Math.cos(bank / 2);
        double s3 = Math.sin(bank / 2);
        double c1c2 = c1 * c2;
        double s1s2 = s1 * s2;
        setW((float) (c1c2 * c3 - s1s2 * s3));
        setX((float) (c1c2 * s3 + s1s2 * c3));
        setY((float) (s1 * c2 * c3 + c1 * s2 * s3));
        setZ((float) (c1 * s2 * c3 - s1 * c2 * s3));

        dirty = true;
    }

    /**
     * Rotation is in degrees. Set this quaternion from the supplied axis angle.
     *
     * @param vec The vector of rotation
     * @param rot The angle of rotation around that vector in degrees.
     */
    public void setAxisAngle(Vector3f vec, float rot) {
        double s = Math.sin(Math.toRadians(rot / 2));
        setX(vec.getX() * (float) s);
        setY(vec.getY() * (float) s);
        setZ(vec.getZ() * (float) s);
        setW((float) Math.cos(Math.toRadians(rot / 2)));

        dirty = true;
    }

    public void setAxisAngleRad(Vector3f vec, double rot) {
        double s = rot / 2;
        setX(vec.getX() * (float) s);
        setY(vec.getY() * (float) s);
        setZ(vec.getZ() * (float) s);
        setW((float) rot / 2);

        dirty = true;
    }



    public void copyFromVec3(Vector3f vec, float w) {
        copyFromV3f(vec, w);
    }

    /**
     * Get a linear interpolation between this quaternion and the input quaternion, storing the result in the output
     * quaternion.
     *
     * @param input The quaternion to be slerped with this quaternion.
     * @param output The quaternion to store the result in.
     * @param t The ratio between the two quaternions where 0 <= t <= 1.0 . Increase value of t will bring rotation
     *            closer to the input quaternion.
     */
    public void slerp(Quaternion input, Quaternion output, float t) {
        // Calculate angle between them.
        //double cosHalftheta = this.dotProduct(input);
        Quaternion bufferQuat;
        float cosHalftheta = this.dotProduct(input);

        if (cosHalftheta < 0) {
            if(tmpQuaternion == null) tmpQuaternion = new Quaternion();
            bufferQuat = tmpQuaternion;
            cosHalftheta = -cosHalftheta;
            bufferQuat.points[0] = (-input.points[0]);
            bufferQuat.points[1] = (-input.points[1]);
            bufferQuat.points[2] = (-input.points[2]);
            bufferQuat.points[3] = (-input.points[3]);
        } else {
            bufferQuat = input;
        }
        /**
         * if(dot < 0.95f){
         * double angle = Math.acos(dot);
         * double ratioA = Math.sin((1 - t) * angle);
         * double ratioB = Math.sin(t * angle);
         * double divisor = Math.sin(angle);
         *
         * //Calculate Quaternion
         * output.setW((float)((this.getW() * ratioA + input.getW() * ratioB)/divisor));
         * output.setX((float)((this.getX() * ratioA + input.getX() * ratioB)/divisor));
         * output.setY((float)((this.getY() * ratioA + input.getY() * ratioB)/divisor));
         * output.setZ((float)((this.getZ() * ratioA + input.getZ() * ratioB)/divisor));
         * }
         * else{
         * lerp(input, output, t);
         * }
         */
        // if qa=qb or qa=-qb then theta = 0 and we can return qa
        if (Math.abs(cosHalftheta) >= 1.0) {
            output.points[0] = (this.points[0]);
            output.points[1] = (this.points[1]);
            output.points[2] = (this.points[2]);
            output.points[3] = (this.points[3]);
        } else {
            double sinHalfTheta = Math.sqrt(1.0 - cosHalftheta * cosHalftheta);
            // if theta = 180 degrees then result is not fully defined
            // we could rotate around any axis normal to qa or qb
            //if(Math.abs(sinHalfTheta) < 0.001){
            //output.setW(this.getW() * 0.5f + input.getW() * 0.5f);
            //output.setX(this.getX() * 0.5f + input.getX() * 0.5f);
            //output.setY(this.getY() * 0.5f + input.getY() * 0.5f);
            //output.setZ(this.getZ() * 0.5f + input.getZ() * 0.5f);
            //  lerp(bufferQuat, output, t);
            //}
            //else{
            double halfTheta = Math.acos(cosHalftheta);

            double ratioA = Math.sin((1 - t) * halfTheta) / sinHalfTheta;
            double ratioB = Math.sin(t * halfTheta) / sinHalfTheta;

            //Calculate Quaternion
            output.points[3] = ((float) (points[3] * ratioA + bufferQuat.points[3] * ratioB));
            output.points[0] = ((float) (this.points[0] * ratioA + bufferQuat.points[0] * ratioB));
            output.points[1] = ((float) (this.points[1] * ratioA + bufferQuat.points[1] * ratioB));
            output.points[2] = ((float) (this.points[2] * ratioA + bufferQuat.points[2] * ratioB));

            //}
        }
    }
}
