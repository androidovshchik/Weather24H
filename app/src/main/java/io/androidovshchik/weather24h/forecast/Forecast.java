package io.androidovshchik.weather24h.forecast;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Forecast {

    @SerializedName("cod")
    public String cod;
    @SerializedName("message")
    public float message;
    @SerializedName("cnt")
    public int cnt;
    @SerializedName("list")
    public List<MyList> list = null;
    @SerializedName("city")
    public City city;

}
