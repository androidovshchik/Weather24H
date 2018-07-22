package io.androidovshchik.weather24h.parser;

import com.google.gson.annotations.SerializedName;

public class Coord {

    @SerializedName("lat")
    public float lat;
    @SerializedName("lon")
    public float lon;

}
