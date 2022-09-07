package com.lucataddeo.datacollectionapp.app_sections.multi_connection.writer.measurements;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HeartRateMeasurement implements MeasurementValue{
    @JsonProperty("Average")
    public final float average;

    @JsonProperty("RrData")
    public final int[] rrData;

    public HeartRateMeasurement(float average, int[] rrData) {
        this.average = average;
        this.rrData = rrData;
    }
}
