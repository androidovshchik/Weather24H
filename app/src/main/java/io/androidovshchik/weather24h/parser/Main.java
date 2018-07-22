package io.androidovshchik.weather24h.parser;

import com.google.gson.annotations.SerializedName;

public class Main {

    @SerializedName("temp")
    public float temp;
    @SerializedName("temp_min")
    public float tempMin;
    @SerializedName("temp_max")
    public float tempMax;
    @SerializedName("pressure")
    public float pressure;
    @SerializedName("sea_level")
    public float seaLevel;
    @SerializedName("grnd_level")
    public float grndLevel;
    @SerializedName("humidity")
    public int humidity;
    @SerializedName("temp_kf")
    public float tempKf;

}
