package io.androidovshchik.weather24h.current;

import com.google.gson.annotations.SerializedName;

public class Main {

    @SerializedName("temp")
    public Double temp;
    @SerializedName("pressure")
    public Integer pressure;
    @SerializedName("humidity")
    public Integer humidity;
    @SerializedName("temp_min")
    public Double tempMin;
    @SerializedName("temp_max")
    public Double tempMax;

}
