package com.brain.ringconnector;

public class SensorData {
    private long id;
    private double timestamp;
    private double accel_x;
    private double accel_y;
    private double accel_z;
    private double gyro_x;
    private double gyro_y;
    private double gyro_z;

    // Add getters and setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public double getAccel_x() {
        return accel_x;
    }

    public void setAccel_x(double accel_x) {
        this.accel_x = accel_x;
    }

    public double getAccel_y() {
        return accel_y;
    }

    public void setAccel_y(double accel_y) {
        this.accel_y = accel_y;
    }

    public double getAccel_z() {
        return accel_z;
    }

    public void setAccel_z(double accel_z) {
        this.accel_z = accel_z;
    }

    public double getGyro_x() {
        return gyro_x;
    }

    public void setGyro_x(double gyro_x) {
        this.gyro_x = gyro_x;
    }

    public double getGyro_y() {
        return gyro_y;
    }

    public void setGyro_y(double gyro_y) {
        this.gyro_y = gyro_y;
    }

    public double getGyro_z() {
        return gyro_z;
    }

    public void setGyro_z(double gyro_z) {
        this.gyro_z = gyro_z;
    }
}