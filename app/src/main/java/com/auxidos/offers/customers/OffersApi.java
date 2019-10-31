package com.auxidos.offers.customers;

import android.support.v4.util.ArrayMap;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface OffersApi
{
    @FormUrlEncoded
    @POST("getOffers.php")
    Call<Offers> getData(@FieldMap ArrayMap<String, Object> options);
}