package com.auxidos.offers.customers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements LocationListener
{
    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates", KEY_LOCATION = "location";
    Toolbar toolbar;
    String cats;
    private NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout mDrawerLayout;
    TextView txtName, txtEmail, seeAll, refresh, noOffer, turnOn;
    EditText search;
    ProgressBar searchProgressbar;
    SessionManager session;
    View overlay;
    CardView noLocation;
    ProgressDialog progressDialog;
    NestedScrollView scrollView;
    Calendar calendar;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    final int PERMISSION_ACCESS_FINE_LOCATION = 65, REQUEST_CHECK_SETTINGS = 0x1, APP_SETTINGS = 98,
            AUTOCOMPLETE_REQUEST_CODE = 123;
    LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    boolean mRequestingLocationUpdates;
    double latitude, longitude, previousLat, previousLon;
    LinearLayout categories, clothing, mobiles, food;
    RelativeLayout top, bottom;
    RecyclerView recyclerView, searchRecyclerView;
    RecyclerView.LayoutManager mLayoutManager, mLayoutManager1;
    OffersAdapter mAdapter;
    SearchStoreAdapter searchStoreAdapter;
    ImageView clothingImage, mobilesImage, foodImage;
    InputMethodManager imm;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);
        if (!session.getLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        mRequestingLocationUpdates = false;
        updateValuesFromBundle(bundle);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        noLocation = findViewById(R.id.no_location);
        turnOn = findViewById(R.id.turn);
        search = findViewById(R.id.search);
        recyclerView = findViewById(R.id.recycler_view);
        searchRecyclerView = findViewById(R.id.search_recycler_view);
        categories = findViewById(R.id.categories);
        noOffer = findViewById(R.id.no_offer);
        top = findViewById(R.id.top);
        bottom = findViewById(R.id.bottom);
        overlay = findViewById(R.id.overlay);
        refresh = findViewById(R.id.refresh);
        scrollView = findViewById(R.id.scroll_view);
        searchProgressbar = findViewById(R.id.search_progress);
        progressDialog = new ProgressDialog(this, R.style.ProgressDialog);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);

        clothing = findViewById(R.id.clothing);
        mobiles = findViewById(R.id.mobiles);
        food = findViewById(R.id.food);
        clothingImage = findViewById(R.id.clothing_image);
        mobilesImage = findViewById(R.id.mobile_image);
        foodImage = findViewById(R.id.food_image);

        Glide.with(this).load(R.drawable.clothing)
                .apply(RequestOptions.circleCropTransform())
                .into(clothingImage);
        Glide.with(this).load(R.drawable.shoes)
                .apply(RequestOptions.circleCropTransform())
                .into(mobilesImage);
        Glide.with(this).load(R.drawable.salon)
                .apply(RequestOptions.circleCropTransform())
                .into(foodImage);

        clothing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOffer("Clothing", 1);
            }
        });
        mobiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOffer("Footwear", 7);
            }
        });
        food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOffer("Salon", 15);
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });
        top.post(new Runnable() {
            @Override
            public void run() {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) scrollView.getLayoutParams();
                params.setMargins(0, top.getHeight() + 30, 0, 0);
                scrollView.setLayoutParams(params);
            }
        });
        calendar = Calendar.getInstance();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        View navHeader = navigationView.getHeaderView(0);
        txtName = navHeader.findViewById(R.id.name);
        txtEmail = navHeader.findViewById(R.id.email);
        seeAll = findViewById(R.id.see_all);

        txtName.setText(session.getName());
        txtEmail.setText(session.getEmail());

        if (session.getName() != null)
            txtName.setText(session.getName());

        setUpNavigationView();
        askLocationPermission();

        turnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askLocationPermission();
            }
        });
        seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SeeAllActivity.class);
                intent.putExtra("lat", latitude);
                intent.putExtra("lon", longitude);
                if(cats != null)
                    intent.putExtra("cats", cats);
                startActivity(intent);
            }
        });
        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b)
                    overlay.setVisibility(View.VISIBLE);
                else
                    overlay.setVisibility(View.GONE);
            }
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (latitude != 0.0 && longitude != 0.0)
                {
                    bottom.setClickable(false);
                    overlay.setVisibility(View.VISIBLE);
                    String text = editable.toString();
                    searchRecyclerView.setVisibility(View.GONE);
                    if (text.length() > 0)
                        getStores(editable.toString());
                    else
                    {
                        bottom.setClickable(true);
                        overlay.setVisibility(View.GONE);
                    }
                } else
                    Toast.makeText(MainActivity.this, "Could not get your location. Check your location settings", Toast.LENGTH_LONG).show();
            }
        });
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overlay.setVisibility(View.GONE);
                searchRecyclerView.setVisibility(View.GONE);
                hideKeyboard();
                search.clearFocus();
            }
        });
    }
    private void hideKeyboard()
    {
        View view = getCurrentFocus();
        if(view == null)
            view = new View(this);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void openOffer(String title, int i)
    {
        if(longitude != 0.0 && latitude != 0.0)
        {
            Intent intent = new Intent(MainActivity.this, OffersActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("category", i);
            intent.putExtra("lat", latitude);
            intent.putExtra("lon", longitude);
            startActivity(intent);
        }
        else
            Toast.makeText(this, "Could not get your location", Toast.LENGTH_LONG).show();
    }

    private void setupGoogleApiClient()
    {
        MyApplication myApplication = new MyApplication();
        mGoogleApiClient = myApplication.getGoogleApiHelper().getGoogleApiClient();
        myApplication.getGoogleApiHelper().setConnectionListener(new GoogleApiHelper.ConnectionListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}
            @Override
            public void onConnectionSuspended(int i) {}
            @SuppressLint("MissingPermission")
            @Override
            public void onConnected(Bundle bundle)
            {
                if (mCurrentLocation != null)
                {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                    }
                    else
                    {
                        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        setLatLng();
                    }
                }
            }
        });
    }
    private void buildLocationSettingsRequest()
    {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    private void createLocationRequest()
    {
        setupGoogleApiClient();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(6000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void checkLocationSettings()
    {
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest);
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try
                        {
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        }
                        catch (IntentSender.SendIntentException e)
                        {
                            Toast.makeText(MainActivity.this, "Unable to execute result", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(MainActivity.this, "Could not get location settings", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }
    protected void stopLocationUpdate()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }
    @SuppressLint("MissingPermission")
    protected void startLocationUpdates()
    {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }
        else
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            mRequestingLocationUpdates = true;
                        }
                    });
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            setLatLng();
        }
    }
    private void updateValuesFromBundle(Bundle savedInstanceState)
    {
        if (savedInstanceState != null)
        {
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES))
                mRequestingLocationUpdates = savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES);

            if (savedInstanceState.keySet().contains(KEY_LOCATION))
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        noLocation.setVisibility(View.VISIBLE);
                        break;
                }
                break;
            case AUTOCOMPLETE_REQUEST_CODE:
                switch(resultCode){
                    case RESULT_OK:
                        Place place = Autocomplete.getPlaceFromIntent(data);
                        LatLng latlng = place.getLatLng();
                        Intent intent = new Intent(MainActivity.this, StoreOffersActivity.class);
                        intent.putExtra("lat", latlng.latitude);
                        intent.putExtra("lon", latlng.longitude);
                        intent.putExtra("latitude", latitude);
                        intent.putExtra("longitude", longitude);
                        intent.putExtra("name", place.getName());
                        startActivity(intent);
                        break;
                    case AutocompleteActivity.RESULT_ERROR:
                        Status status = Autocomplete.getStatusFromIntent(data);
                        Toast.makeText(MainActivity.this, status.getStatusMessage(), Toast.LENGTH_LONG).show();
                        break;
                }
        }
    }
    void setLatLng()
    {
        if(mCurrentLocation != null)
        {
            noOffer.setVisibility(View.GONE);
            seeAll.setVisibility(View.VISIBLE);
            refresh.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            searchRecyclerView.setVisibility(View.GONE);
            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();

            if(previousLat != latitude)
                getData();

            previousLat = latitude;
            previousLon = longitude;
        }
        else
        {
            recyclerView.setVisibility(View.GONE);
            searchRecyclerView.setVisibility(View.GONE);
            seeAll.setVisibility(View.GONE);
            refresh.setVisibility(View.GONE);
            noOffer.setVisibility(View.VISIBLE);
        }
    }
    void getData()
    {
        try
        {
            ArrayMap<String, Object> data = new ArrayMap<>();
            data.put("email", session.getEmail());
            data.put("lat", latitude);
            data.put("lon", longitude);

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String location = addresses.get(0).getLocality();
            data.put("location", location);

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
                        noLocation.setVisibility(View.GONE);
                        Offers offers = response.body();
                        String value = offers.getValue();
                        if(value.equalsIgnoreCase("done"))
                        {
                            recyclerView.setVisibility(View.VISIBLE);
                            searchRecyclerView.setVisibility(View.GONE);
                            noOffer.setVisibility(View.GONE);
                            JSONObject jsonObject = new JSONObject(offers.getCount());
                            cats = jsonObject.toString();
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
                                        Toast.makeText(MainActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
                                    }
                                    return valB.compareTo(valA);
                                }
                            });
                            JSONArray sortedArray = new JSONArray();
                            for(int i = data.length() - 1;i >= 0 ;i--)
                            {
                                sortedArray.put(data.get(i));
                            }
                            mAdapter = new OffersAdapter(sortedArray, MainActivity.this, latitude, longitude);
                            mLayoutManager = new LinearLayoutManager(MainActivity.this);
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
                            noOffer.setVisibility(View.VISIBLE);
                            seeAll.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);
                            searchRecyclerView.setVisibility(View.GONE);
                        }
                    }
                    catch(Exception e){
                        noLocation.setVisibility(View.GONE);
                        if(progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                        recyclerView.setVisibility(View.GONE);
                        searchRecyclerView.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<Offers> call, Throwable t) {
                    noLocation.setVisibility(View.GONE);
                    if(progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    recyclerView.setVisibility(View.GONE);
                    searchRecyclerView.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
                }
            });
        }
        catch (Exception e)
        {
            noLocation.setVisibility(View.GONE);
            seeAll.setVisibility(View.GONE);

            if(progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            recyclerView.setVisibility(View.GONE);
            searchRecyclerView.setVisibility(View.GONE);
            noOffer.setVisibility(View.VISIBLE);
        }
    }
    void getStores(String text)
    {
        searchProgressbar.setVisibility(View.VISIBLE);
        ArrayMap<String, Object> data = new ArrayMap<>();
        data.put("email", session.getEmail());
        data.put("lat", latitude);
        data.put("lon", longitude);
        data.put("text", text);

        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.LOCATION)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        SearchStoresApi searchStoresApi = retrofit.create(SearchStoresApi.class);
        Call<SearchStores> call;
        call = searchStoresApi.getData(data);
        call.enqueue(new Callback<SearchStores>() {
            @Override
            public void onResponse(Call<SearchStores> call, Response<SearchStores> response) {
                try
                {
                    noLocation.setVisibility(View.GONE);
                    searchProgressbar.setVisibility(View.GONE);
                    SearchStores offers = response.body();
                    String value = offers.getValue();
                    if(search.getText().toString().length() > 0)
                    {
                        if(value.equalsIgnoreCase("done"))
                        {
                            searchRecyclerView.setVisibility(View.VISIBLE);
                            searchRecyclerView.bringToFront();
                            JSONArray stores = new JSONArray(offers.getData());
                            searchStoreAdapter = new SearchStoreAdapter(stores, MainActivity.this, latitude, longitude);
                            mLayoutManager1 = new LinearLayoutManager(MainActivity.this);
                            searchRecyclerView.setLayoutManager(mLayoutManager1);
                            searchRecyclerView.setAdapter(searchStoreAdapter);
                            searchRecyclerView.setItemAnimator(new DefaultItemAnimator());
                        }
                        else
                            Toast.makeText(MainActivity.this, value, Toast.LENGTH_LONG).show();
                    }
                }
                catch(Exception e){
                    noLocation.setVisibility(View.GONE);
                    searchProgressbar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<SearchStores> call, Throwable t) {
                noLocation.setVisibility(View.GONE);
                searchProgressbar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setUpNavigationView()
    {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId())
                {
                    case R.id.nav_home:
                        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                            mDrawerLayout.closeDrawers();
                        break;
                    case R.id.nav_invite:
                        openInviteOptions();
                        break;
                    case R.id.nav_rate_us:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getApplicationContext().getPackageName())));
                        break;
                    case R.id.nav_privacy:
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://auxidos.com/deemn_privacy.html"));
                        startActivity(browserIntent);
                        break;
                    case R.id.nav_log_out:
                        session.setLoggedIn(false);
                        session.setEmail(null);
                        session.setName(null);
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                        break;

                    default:
                        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                            mDrawerLayout.closeDrawers();
                        break;
                }
                return true;
            }
        });
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setItemIconTintList(null);
    }
    private void openInviteOptions()
    {
        Spanned text = Html.fromHtml(getResources().getString(R.string.invite_email)
                + Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())+"<br>");

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hey! Check out Deemn");
        emailIntent.setType("message/rfc822");

        PackageManager pm = getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");

        Intent openInChooser = Intent.createChooser(emailIntent, "Email");
        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<>();
        ResolveInfo ri;
        String packageName;
        for (int i = 0; i < resInfo.size(); i++)
        {
            ri = resInfo.get(i);
            packageName = ri.activityInfo.packageName;

            if(packageName.contains("android.email"))
                emailIntent.setPackage(packageName);
            else if(packageName.contains("whatsapp") ||  packageName.contains("android.gm"))
            {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                if(packageName.contains("whatsapp"))
                    intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.invite_whatsapp)
                            + Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                else if(packageName.contains("android.gm"))
                {
                    intent.putExtra(Intent.EXTRA_TEXT, text);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Invitation for Deemn");
                    intent.setType("message/rfc822");
                }
                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }
        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new LabeledIntent[intentList.size()]));
        startActivity(openInChooser);
    }
    private void askLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }
        else if(ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            createLocationRequest();
            buildLocationSettingsRequest();
            checkLocationSettings();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    noLocation.setVisibility(View.GONE);
                    createLocationRequest();
                    buildLocationSettingsRequest();
                    checkLocationSettings();
                    return;
                }
                else
                {
                    seeAll.setVisibility(View.GONE);
                    refresh.setVisibility(View.GONE);
                    noLocation.setVisibility(View.VISIBLE);
                }
                break;
            case APP_SETTINGS:
                askLocationPermission();
                break;
        }
    }
    @Override
    public void onBackPressed()
    {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawers();
        else
            super.onBackPressed();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        setLatLng();
    }
    @Override
    protected  void onResume()
    {
        super.onResume();
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
        {
            navigationView.getMenu().getItem(0).setChecked(true);
            mDrawerLayout.closeDrawers();
        }
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            startLocationUpdates();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            stopLocationUpdate();
    }
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
    }
}