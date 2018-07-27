package io.androidovshchik.weather24h.current;

import com.google.gson.annotations.SerializedName;

public class Sys {

    @SerializedName("type")
    public Integer type;
    @SerializedName("id")
    public Integer id;
    @SerializedName("message")
    public Double message;
    @SerializedName("country")
    public String country;
    @SerializedName("sunrise")
    public Integer sunrise;
    @SerializedName("sunset")
    public Integer sunset;

}
