package com.auxidos.offers.customers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
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

public class OffersActivity extends AppCompatActivity
{
    ProgressDialog progressDialog;
    Toolbar toolbar;
    Intent intent;
    SessionManager session;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    OffersAdapter mAdapter;
    LinearLayout nothing;
    TextView nothingText;

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.activity_offers);

        session = new SessionManager(this);
        recyclerView = findViewById(R.id.recycler_view);
        progressDialog = new ProgressDialog(this, R.style.ProgressDialog);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);
        toolbar = findViewById(R.id.toolbar);
        nothing = findViewById(R.id.nothing);
        nothingText = findViewById(R.id.nothing_text);
        intent = getIntent();
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);

            if(intent.hasExtra("title"))
                getSupportActionBar().setTitle(intent.getStringExtra("title"));
        }
        if(intent.hasExtra("category") && intent.hasExtra("lat") && intent.hasExtra("lon"))
            getData(intent.getIntExtra("category", 0), intent.getDoubleExtra("lat", 0.0), intent.getDoubleExtra("lon", 0.0));
    }
    void getData(int category, final double latitude, final double longitude)
    {
        nothing.setVisibility(View.GONE);
        ArrayMap<String, Object> data = new ArrayMap<>();
        data.put("email", session.getEmail());
        data.put("category", category);
        data.put("lat", latitude);
        data.put("lon", longitude);

        progressDialog.show();
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.LOCATION)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        OffersApi offersApi = retrofit.create(OffersApi.class);
        Call<Offers> call;
        call = offersApi.getData(data);
        call.enqueue(new Callback<Offers>() {
            @Override
            public void onResponse(Call<Offers> call, Response<Offers> response) {
                try
                {
                    Offers offers = response.body();
                    String value = offers.getValue();
                    if(value.equalsIgnoreCase("done"))
                    {
                        JSONArray data = new JSONArray(offers.getData());
                        if(data.length() > 0)
                        {
                            recyclerView.setVisibility(View.VISIBLE);
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
                                        Toast.makeText(OffersActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
                                    }
                                    return valB.compareTo(valA);
                                }
                            });
                            JSONArray sortedArray = new JSONArray();
                            for(int i = data.length() - 1;i >= 0 ;i--)
                            {
                                sortedArray.put(data.get(i));
                            }
                            mAdapter = new OffersAdapter(sortedArray, OffersActivity.this, latitude, longitude);
                            mLayoutManager = new LinearLayoutManager(OffersActivity.this);
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
                            nothing.setVisibility(View.VISIBLE);
                        }
                    }
                    else
                    {
                        nothing.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        if(progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                        nothingText.setText(value);
                    }
                }
                catch(Exception e){
                    recyclerView.setVisibility(View.GONE);
                    if(progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    nothing.setVisibility(View.VISIBLE);
                    nothingText.setText(AppConfig.UNKNOWN_ERROR);
                }
            }
            @Override
            public void onFailure(Call<Offers> call, Throwable t) {
                progressDialog.dismiss();
                nothing.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                nothingText.setText(AppConfig.UNKNOWN_ERROR);
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