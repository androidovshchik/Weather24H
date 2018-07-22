package io.androidovshchik.weather24h.parser;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SiteResponse {

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
