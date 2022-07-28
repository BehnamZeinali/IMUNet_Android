package edu.usf.imunet;

public class SensorSample {
    private float ori_w ;
    private float ori_x ;
    private float ori_y ;
    private float ori_z ;
    private float acc_x ;
    private float acc_y ;
    private float acc_z ;
    private float gyro_x ;
    private float gyro_y ;
    private float gyro_z ;

    private float pos_x ;
    private float pos_y ;


    private double time ;

    public float getOri_w() {
        return ori_w;
    }

    public float getOri_x() {
        return ori_x;
    }

    public float getOri_y() {
        return ori_y;
    }

    public float getOri_z() {
        return ori_z;
    }



    public float getAcc_x() {
        return acc_x;
    }

    public float getAcc_y() {
        return acc_y;
    }

    public float getAcc_z() {
        return acc_z;
    }



    public float getGyro_x() {
        return gyro_x;
    }

    public float getGyro_y() {
        return gyro_y;
    }

    public float getGyro_z() {
        return gyro_z;
    }

    public float getPos_x() {
        return pos_x;
    }

    public float getPos_y() {
        return pos_y;
    }



    public double getTime() {
        return time;
    }

    public void setOri_w(float ori_w) {
        this.ori_w = ori_w;
    }

    public void setOri_x(float ori_x) {
        this.ori_x = ori_x;
    }

    public void setOri_y(float ori_y) {
        this.ori_y = ori_y;
    }

    public void setOri_z(float ori_z) {
        this.ori_z = ori_z;
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



    public void setGyro_x(float gyro_x) {
        this.gyro_x = gyro_x;
    }

    public void setGyro_y(float gyro_y) {
        this.gyro_y = gyro_y;
    }

    public void setGyro_z(float gyro_z) {
        this.gyro_z = gyro_z;
    }

    public void setPos_x(float pos_x) {
        this.pos_x = pos_x;
    }

    public void setPos_y(float pos_y) {
        this.pos_y = pos_y;
    }

    public void setTime(double time) {
        this.time = time;
    }
}
