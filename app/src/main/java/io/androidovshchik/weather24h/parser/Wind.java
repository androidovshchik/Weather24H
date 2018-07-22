package io.androidovshchik.weather24h.parser;

import com.google.gson.annotations.SerializedName;

public class Wind {

    @SerializedName("speed")
    public float speed;
    @SerializedName("deg")
    public float deg;

}
