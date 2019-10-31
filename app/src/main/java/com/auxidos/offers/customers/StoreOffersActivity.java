package com.auxidos.offers.customers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StoreOffersActivity extends AppCompatActivity
{
    Toolbar toolbar;
    double lat, lon, latitude, longitude;
    String id, name;
    SessionManager session;
    ProgressDialog progressDialog;
    TextView storeName, nothingText;
    ImageView direction;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    OffersAdapter mAdapter;

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.activity_store_offers);

        session = new SessionManager(this);
        recyclerView = findViewById(R.id.recycler_view);
        toolbar = findViewById(R.id.toolbar);
        progressDialog = new ProgressDialog(this, R.style.ProgressDialog);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        storeName = findViewById(R.id.name);
        nothingText = findViewById(R.id.nothing_text);
        direction = findViewById(R.id.direction);

        getStoreOffers(getIntent());
    }
    private void getStoreOffers(Intent intent)
    {
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
        lat = intent.getDoubleExtra("lat", 0);
        lon = intent.getDoubleExtra("lon", 0);
        id = intent.getStringExtra("id");
        name = intent.getStringExtra("name");

        direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://maps.google.com/maps?saddr="+latitude+","+longitude+"&daddr="+lat+","+lon);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        storeName.setText(name);

        ArrayMap<String, Object> data = new ArrayMap<>();
        data.put("email", session.getEmail());
        data.put("lat", lat);
        data.put("lon", lon);
        data.put("id", id);

        progressDialog.show();
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.LOCATION)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        StoreOffersApi storeOffersApi = retrofit.create(StoreOffersApi.class);
        Call<StoreOffers> call;
        call = storeOffersApi.getData(data);
        call.enqueue(new Callback<StoreOffers>() {
            @Override
            public void onResponse(Call<StoreOffers> call, Response<StoreOffers> response) {
                try
                {
                    StoreOffers offers = response.body();
                    String value = offers.getValue();
                    if(value.equalsIgnoreCase("done"))
                    {
                        recyclerView.setVisibility(View.VISIBLE);
                        nothingText.setVisibility(View.GONE);
                        JSONArray data = new JSONArray(offers.getData());
                        List<JSONObject> jsonList = new ArrayList<>();
                        for(int i = 0;i < data.length();i++)
                        {
                            jsonList.add(data.getJSONObject(i));
                        }
                        Collections.sort(jsonList, new Comparator<JSONObject>(){
                            @Override
                            public int compare(JSONObject a, JSONObject b) {
                                String valA = "", valB = "";

                                try{
                                    valA = (String)a.get("lat");
                                    valB = String.valueOf(latitude);
                                }
                                catch(Exception e)
                                {
                                    Toast.makeText(StoreOffersActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
                                }
                                return valB.compareTo(valA);
                            }
                        });
                        JSONArray sortedArray = new JSONArray();
                        for(int i = data.length() - 1;i >= 0 ;i--)
                        {
                            sortedArray.put(data.get(i));
                        }
                        mAdapter = new OffersAdapter(sortedArray, StoreOffersActivity.this, latitude, longitude);
                        mLayoutManager = new LinearLayoutManager(StoreOffersActivity.this);
                        recyclerView.setLayoutManager(mLayoutManager);
                        recyclerView.setAdapter(mAdapter);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
                        {
                            @Override
                            public void onGlobalLayout()
                            {
                                if(progressDialog != null && progressDialog.isShowing())
                                    progressDialog.dismiss();
                            }
                        });
                    }
                    else
                    {
                        if(progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                        recyclerView.setVisibility(View.GONE);
                        nothingText.setVisibility(View.VISIBLE);
                    }
                }
                catch(Exception e){
                    if(progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    recyclerView.setVisibility(View.GONE);
                    Toast.makeText(StoreOffersActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<StoreOffers> call, Throwable t) {
                if(progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                recyclerView.setVisibility(View.GONE);
                Toast.makeText(StoreOffersActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}