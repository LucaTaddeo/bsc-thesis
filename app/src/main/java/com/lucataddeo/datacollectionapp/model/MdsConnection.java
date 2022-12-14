package com.lucataddeo.datacollectionapp.model;

import com.google.gson.annotations.SerializedName;

public class MdsConnection {

    @SerializedName("UUID")
    private String uuid;

    @SerializedName("Type")
    private String type;

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "MdsConnection{" +
                "uuid='" + uuid + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
