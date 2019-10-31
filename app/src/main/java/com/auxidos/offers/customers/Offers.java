package com.auxidos.offers.customers;

import com.google.gson.annotations.SerializedName;

public class Offers
{
    @SerializedName("value")
    private String value;
    @SerializedName("data")
    private String data;
    @SerializedName("count")
    private String count;

    public Offers() {}

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }

    public String getCount() {
        return count;
    }
    public void setCount(String count) {
        this.count = count;
    }
}