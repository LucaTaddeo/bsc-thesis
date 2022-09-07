package com.lucataddeo.datacollectionapp.app_sections.multi_connection.writer.measurements;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.lucataddeo.datacollectionapp.model.*;

@JsonPropertyOrder({"Timestamp", "Sensor", "Value"})
public class Measurement {
    @JsonProperty("Sensing")
    public final Sensing sensing;

    @JsonProperty("Timestamp")
    public long timestamp;
    @JsonProperty("Measurement")
    public final MeasurementValue measurementValue;

    public Measurement(Sensing sensing, long timestamp, MeasurementValue measurementValue) {
        this.sensing = sensing;
        this.timestamp = timestamp;
        this.measurementValue = measurementValue;
    }

    public Measurement(LinearAcceleration linearAcceleration){
        this.sensing = Sensing.LinearAcceleration;
        this.timestamp = linearAcceleration.body.timestamp;
        LinearAcceleration.Array values = linearAcceleration.body.array[0];
        this.measurementValue = new XYZmeasurement(values.x, values.y, values.z);
    }

    public Measurement(AngularVelocity angularVelocity){
        this.sensing = Sensing.AngularVelocity;
        this.timestamp = angularVelocity.body.timestamp;
        AngularVelocity.Array values = angularVelocity.body.array[0];
        this.measurementValue = new XYZmeasurement(values.x, values.y, values.z);
    }

    public Measurement(MagneticField magneticField){
        this.sensing = Sensing.MagneticField;
        this.timestamp = magneticField.body.timestamp;
        MagneticField.Array values = magneticField.body.array[0];
        this.measurementValue = new XYZmeasurement(values.x, values.y, values.z);
    }

    public Measurement(HeartRate heartRate){
        this.sensing = Sensing.HeartRate;
        HeartRate.Body body = heartRate.body;
        this.measurementValue = new HeartRateMeasurement(body.average, body.rrData);
    }

    public Measurement(Temperature temperature){
        this.sensing = Sensing.Temperature;
        Temperature.Content content = temperature.content;
        this.measurementValue = new TemperatureMeasurement(content.relativeTime, content.measurement);
    }
}

enum Sensing {
    LinearAcceleration,
    AngularVelocity,
    MagneticField,
    HeartRate,
    Temperature
}
