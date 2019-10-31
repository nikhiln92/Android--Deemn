package com.auxidos.offers.customers;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Nikhil on 14-04-2018.
 */

public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private ConnectionListener connectionListener;
    private Bundle connectionBundle;

    GoogleApiHelper(Context context) {
        this.context = context;
        buildGoogleApiClient();
        connect();
    }

    GoogleApiClient getGoogleApiClient() {
        return this.mGoogleApiClient;
    }

    void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
        if (this.connectionListener != null && isConnected())
            connectionListener.onConnected(connectionBundle);
    }

    private void connect() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    public void disconnect() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    private boolean isConnected() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        connectionBundle = bundle;
        if (connectionListener != null)
            connectionListener.onConnected(bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        if (connectionListener != null)
            connectionListener.onConnectionSuspended(i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionListener != null)
            connectionListener.onConnectionFailed(connectionResult);
    }

    public interface ConnectionListener {
        void onConnectionFailed(@NonNull ConnectionResult connectionResult);
        void onConnectionSuspended(int i);
        void onConnected(Bundle bundle);
    }
}