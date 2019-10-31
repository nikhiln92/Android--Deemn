package com.auxidos.offers.customers;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SearchStoreAdapter extends RecyclerView.Adapter<SearchStoreAdapter.MyViewHolder>
{
    private JSONArray data;
    private Activity activity;
    SessionManager session;
    double latitude, longitude;
    SimpleDateFormat format;

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView store, distance;
        RelativeLayout holder;

        MyViewHolder(View view)
        {
            super(view);
            store = view.findViewById(R.id.store);
            distance = view.findViewById(R.id.distance);
            holder = view.findViewById(R.id.holder);
        }
    }
    SearchStoreAdapter(JSONArray data, Activity activity, double lat, double lon) {
        super();
        this.data = data;
        this.activity = activity;
        this.latitude = lat;
        this.longitude = lon;
        format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        session = new SessionManager(activity);
        setHasStableIds(true);
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_search, parent, false);
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position)
    {
        try
        {
            JSONObject objectInArray = data.getJSONObject(position);
            final String store = objectInArray.getString("store");
            final double lat = objectInArray.getDouble("lat");
            final double lon = objectInArray.getDouble("lon");
            final String id = objectInArray.getString("id");

            holder.store.setText(store.trim());
            holder.holder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, StoreOffersActivity.class);
                    intent.putExtra("name", store);
                    intent.putExtra("lat", lat);
                    intent.putExtra("lon", lon);
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("id", id);
                    activity.startActivity(intent);
                }
            });
            holder.distance.setText(distance(latitude, lat, longitude, lon));
        }
        catch (Exception e) {
            Toast.makeText(activity, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
        }
    }
    private static String distance(double lat1, double lat2, double lon1, double lon2)
    {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        distance = Math.pow(distance , 2);
        return String.format(Locale.ENGLISH, "%.02f", Math.sqrt(distance)) + " KM";
    }
    @Override
    public int getItemCount() {
        return data.length();
    }
}