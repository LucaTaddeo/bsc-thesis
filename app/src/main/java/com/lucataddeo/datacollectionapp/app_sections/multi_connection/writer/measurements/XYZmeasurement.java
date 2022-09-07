package com.lucataddeo.datacollectionapp.app_sections.multi_connection.writer.measurements;

public class XYZmeasurement implements MeasurementValue {
    public double x, y, z;

    public XYZmeasurement(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
