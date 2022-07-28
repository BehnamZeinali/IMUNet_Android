package edu.usf.imunet.sensors;

public class AccelerometerSample {

    private float acc_x ;
    private float acc_y ;
    private float acc_z ;



    private double time ;





    public float getAcc_x() {
        return acc_x;
    }

    public float getAcc_y() {
        return acc_y;
    }

    public float getAcc_z() {
        return acc_z;
    }







    public double getTime() {
        return time;
    }




    public void setAcc_x(float acc_x) {
        this.acc_x = acc_x;
    }

    public void setAcc_y(float acc_y) {
        this.acc_y = acc_y;
    }

    public void setAcc_z(float acc_z) {
        this.acc_z = acc_z;
    }




    public void setTime(double time) {
        this.time = time;
    }
}
