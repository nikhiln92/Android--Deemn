package com.auxidos.offers.customers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class CustomGridAdapter extends BaseAdapter
{
    private Context context;
    private final String[] titles;
    private final int[] images;
    private final Integer[] category;
    private double lat, lon;

    public CustomGridAdapter(Context context, String[] titles, int[] images, Integer[] category, double lat, double lon)
    {
        this.context = context;
        this.titles = titles;
        this.images = images;
        this.category = category;
        this.lat = lat;
        this.lon = lon;
    }
    @Override
    public int getCount() {
        return category.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup)
    {
        View gridView = null;
        try
        {
            LayoutInflater  inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(view == null)
            {
                gridView = inflater.inflate(R.layout.each_grid_item, null);

                TextView title = gridView.findViewById(R.id.title);
                title.setText(titles[category[i]-1]);

                ImageView image = gridView.findViewById(R.id.image);
                Glide.with(context).load(images[category[i]-1])
                        .apply(RequestOptions.circleCropTransform())
                        .into(image);

                LinearLayout holder = gridView.findViewById(R.id.holder);
                holder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, OffersActivity.class);
                        intent.putExtra("title", titles[category[i]-1]);
                        intent.putExtra("category", category[i]);
                        intent.putExtra("lat", lat);
                        intent.putExtra("lon", lon);
                        context.startActivity(intent);
                    }
                });
            }
            else
                gridView = view;
        }
        catch(Exception e)
        {
            Toast.makeText(context, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
        }
        return gridView;
    }
}