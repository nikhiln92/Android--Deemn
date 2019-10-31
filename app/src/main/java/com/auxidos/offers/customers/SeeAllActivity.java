package com.auxidos.offers.customers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SeeAllActivity extends AppCompatActivity
{
    GridView gridView;
    Toolbar toolbar;
    String[] titles;
    int[] images;
    Integer[] category;
    Intent intent;

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.activity_see_all);

        titles = new String[]{"Clothing", "Mobiles", "Electronics", "Watches", "Jewellery",
                "Gym", "Footwear", "Supermarket", "Gifts", "Food Joints", "Glasses & Lens", "Book Store",
                "Flower Shop", "Spa & Massage", "Salon", "Luggage", "Cosmetics"};
        //category = new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
        images = new int[]{R.drawable.clothing, R.drawable.mobiles, R.drawable.electronics, R.drawable.watches, R.drawable.jewellery,
                R.drawable.gym, R.drawable.shoes, R.drawable.supermarket, R.drawable.gifts, R.drawable.food_joint,
                R.drawable.glasses, R.drawable.book, R.drawable.flower, R.drawable.spa, R.drawable.salon,
                R.drawable.luggage, R.drawable.cosmetics};
        toolbar = findViewById(R.id.toolbar);
        gridView = findViewById(R.id.grid_view);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        intent = getIntent();
        if(intent.hasExtra("cats"))
        {
            try
            {
                JSONObject jsonObject = new JSONObject(intent.getStringExtra("cats"));
                Iterator<String> keys = jsonObject.keys();
                List<Integer> catKeys = new ArrayList<>();
                while(keys.hasNext())
                {
                    String key = keys.next();
                    catKeys.add(Integer.valueOf(key));
                }
                category = catKeys.toArray(new Integer[catKeys.size()]);
            }
            catch (Exception e) {
                Toast.makeText(SeeAllActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
            }
        }
        gridView.setAdapter(new CustomGridAdapter(this, titles, images, category, intent.getDoubleExtra("lat", 0), intent.getDoubleExtra("lon", 0)));
    }
    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }
}