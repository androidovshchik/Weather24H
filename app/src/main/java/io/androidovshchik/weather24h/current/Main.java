package io.androidovshchik.weather24h.current;

import com.google.gson.annotations.SerializedName;

public class Main {

    @SerializedName("temp")
    public float temp;
    @SerializedName("pressure")
    public float pressure;
    @SerializedName("humidity")
    public float humidity;
    @SerializedName("temp_min")
    public float tempMin;
    @SerializedName("temp_max")
    public float tempMax;

}
