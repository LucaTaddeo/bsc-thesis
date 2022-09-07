package com.lucataddeo.datacollectionapp.model;


import com.google.gson.annotations.SerializedName;

public class EnergyGetModel {

    @SerializedName("Content")
    public int content;

    public EnergyGetModel(int content) {
        this.content = content;
    }

}
