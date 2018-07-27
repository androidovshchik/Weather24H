package io.androidovshchik.weather24h.current;

import com.google.gson.annotations.SerializedName;

public class Weather {

    @SerializedName("id")
    public Integer id;
    @SerializedName("main")
    public String main;
    @SerializedName("description")
    public String description;
    @SerializedName("icon")
    public String icon;

}
