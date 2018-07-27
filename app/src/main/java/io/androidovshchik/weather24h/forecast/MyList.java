package io.androidovshchik.weather24h.forecast;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.androidovshchik.weather24h.model.Data;

public class MyList {

    @SerializedName("dt")
    public int dt;
    @SerializedName("main")
    public Main main;
    @SerializedName("weather")
    public java.util.List<Weather> weather = null;
    @SerializedName("clouds")
    public Clouds clouds;
    @SerializedName("wind")
    public Wind wind;
    @SerializedName("rain")
    public Rain rain;
    @SerializedName("sys")
    public Sys sys;
    @SerializedName("dt_txt")
    public Date dtTxt;
    public transient Data data;

}
