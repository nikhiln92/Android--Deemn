package com.auxidos.offers.customers;

/**
 * Created by Nikhil on 20-03-2018.
 */

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends Application
{
    private static MyApplication mInstance;
    private GoogleApiHelper googleApiHelper;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;
        googleApiHelper = new GoogleApiHelper(mInstance);
    }
    protected synchronized MyApplication getInstance()
    {
        return mInstance;
    }

    public GoogleApiHelper getGoogleApiHelperInstance() {
        return this.googleApiHelper;
    }
    public GoogleApiHelper getGoogleApiHelper() {
        return getInstance().getGoogleApiHelperInstance();
    }
}