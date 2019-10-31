package com.auxidos.offers.customers;

import com.google.gson.annotations.SerializedName;

public class StoreOffers
{
    @SerializedName("value")
    private String value;
    @SerializedName("data")
    private String data;

    public StoreOffers() {}

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
}