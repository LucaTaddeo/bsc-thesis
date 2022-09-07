package com.lucataddeo.datacollectionapp.app_sections.multi_connection.writer.measurements;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TemperatureMeasurement implements MeasurementValue{
    @JsonProperty("RelativeTime")
    public final long relativeTime;

    @JsonProperty("Temperature")
    public final double measurement;

    public TemperatureMeasurement(long relativeTime, double measurement) {
        this.relativeTime = relativeTime;
        this.measurement = measurement;
    }
}
