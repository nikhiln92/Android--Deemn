package com.auxidos.offers.customers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Nikhil on 02-12-2017.
 */

public class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.MyViewHolder>
{
    private JSONArray data;
    private Activity activity;
    private SessionManager session;
    private double latitude, longitude;
    private SimpleDateFormat format;
    DatabaseReference root, child, userInterest;

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView store, title, date, distance;
        ImageView image;
        CardView holder;

        MyViewHolder(View view)
        {
            super(view);
            store = view.findViewById(R.id.store);
            title = view.findViewById(R.id.title);
            date = view.findViewById(R.id.date);
            image = view.findViewById(R.id.image);
            distance = view.findViewById(R.id.distance);
            holder = view.findViewById(R.id.holder);
        }
    }
    OffersAdapter(JSONArray data, Activity activity, double lat, double lon) {
        super();
        this.data = data;
        this.activity = activity;
        this.latitude = lat;
        this.longitude = lon;
        format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        session = new SessionManager(activity);
        root = FirebaseDatabase.getInstance().getReference().getRoot();
        setHasStableIds(true);
    }
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_offer, parent, false);
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position)
    {
        try
        {
            JSONObject objectInArray = data.getJSONObject(position);
            final String name = objectInArray.getString("name");
            final String title = objectInArray.getString("title");
            final String category = objectInArray.getString("cat");
            final double lat = objectInArray.getDouble("lat");
            final double lon = objectInArray.getDouble("lon");
            final String end = objectInArray.getString("end");
            final String start = objectInArray.getString("start");
            final String store = objectInArray.getString("store");
            final String id = objectInArray.getString("id");

            Date endDate = format.parse(end);
            Date startDate = format.parse(start);
            Date today = new Date();
            Date t = format.parse(format.format(today));
            long diff1 = startDate.getTime() - t.getTime();
            if(diff1 < 0)
            {
                long diff = endDate.getTime() - t.getTime();
                long d = diff / (1000 * 60 * 60 * 24);
                if(d < 1)
                    holder.date.setText("Ends Today");
                else if(d == 1)
                    holder.date.setText("Ends Tomorrow");
                else
                    holder.date.setText("Ends in "+String.valueOf(d)+" days");
            }
            else
            {
                long d = diff1 / (1000 * 60 * 60 * 24);
                if(d < 1)
                    holder.date.setText("Starts Today");
                else if(d == 1)
                    holder.date.setText("Starts Tomorrow");
                else
                    holder.date.setText("Starts in "+String.valueOf(d)+" days");
            }

            String imageName = store.replaceAll("\\d","");
            imageName = imageName.trim();

            final Handler handler = new Handler();
            Glide.with(activity)
                    .load(AppConfig.WEBSITE+"Mobile/Deemn/Images/"+imageName+".jpg")
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showDefaultImage(holder, category);
                                }
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(holder.image);

            holder.title.setText(title.trim());
            holder.store.setText(name.trim());
            holder.holder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try
                    {
                        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
                        bottomSheetDialog.setContentView(R.layout.bottom_sheet_offer_details);
                        bottomSheetDialog.setCancelable(true);

                        TextView offer = bottomSheetDialog.findViewById(R.id.offer);
                        TextView storeName = bottomSheetDialog.findViewById(R.id.name);
                        TextView startText = bottomSheetDialog.findViewById(R.id.start);
                        TextView endText = bottomSheetDialog.findViewById(R.id.end);
                        ImageView share = bottomSheetDialog.findViewById(R.id.share);

                        offer.setText(title);
                        storeName.setText(name+" ");
                        startText.setText("Start Date: " + start);
                        endText.setText("End Date: " + end);

                        bottomSheetDialog.show();

                        share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                shareOffer(title, name, holder.date.getText().toString());
                            }
                        });
                        child = root.child("Shops").child(category).child(store).child(id);
                        child.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue() != null)
                                    child.setValue(String.valueOf(Integer.valueOf(dataSnapshot.getValue().toString()) + 1));
                                else
                                    child.setValue(1);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });

                        userInterest = root.child("Users").child(session.getUser()).child(category).child(store).child(id);
                        userInterest.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue() != null)
                                    userInterest.setValue(String.valueOf(Integer.valueOf(dataSnapshot.getValue().toString()) + 1));
                                else
                                    userInterest.setValue(1);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });
                    }
                    catch(Exception e)
                    {
                        Toast.makeText(activity, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
                    }
                }
            });
            holder.distance.setText(distance(latitude, lat, longitude, lon));
            holder.distance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openMap(lat, lon);
                }
            });
        }
        catch (Exception e) {
            Toast.makeText(activity, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
        }
    }
    private void shareOffer(String title, String store, String date)
    {
        Spanned text = Html.fromHtml(title+" at "+store+"<br><b>"+date+"</b>");
        String whatsappText = "Hey! Checkout this offer at deemn \n" + "*"+title+"* at " + store + "\n" + "*" + date + "*";

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hey! Check out this offer at deemn");
        emailIntent.setType("text/html");

        PackageManager pm = activity.getPackageManager();
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
                    intent.putExtra(Intent.EXTRA_TEXT, whatsappText);
                else if(packageName.contains("android.gm"))
                {
                    intent.putExtra(Intent.EXTRA_TEXT, text);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Checkout this offer at deemn");
                    intent.setType("message/rfc822");
                }
                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }
        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new LabeledIntent[intentList.size()]));
        activity.startActivity(openInChooser);
    }
    private void showDefaultImage(@NonNull MyViewHolder holder, String category) {
        switch(category)
        {
            case "1":
                Glide.with(activity).load(R.drawable.clothing).into(holder.image);
                break;
            case "2":
                Glide.with(activity).load(R.drawable.mobiles).into(holder.image);
                break;
            case "3":
                Glide.with(activity).load(R.drawable.electronics).into(holder.image);
                break;
            case "4":
                Glide.with(activity).load(R.drawable.watches).into(holder.image);
                break;
            case "5":
                Glide.with(activity).load(R.drawable.jewellery).into(holder.image);
                break;
            case "6":
                Glide.with(activity).load(R.drawable.gym).into(holder.image);
                break;
            case "7":
                Glide.with(activity).load(R.drawable.shoes).into(holder.image);
                break;
            case "8":
                Glide.with(activity).load(R.drawable.supermarket).into(holder.image);
                break;
            case "9":
                Glide.with(activity).load(R.drawable.gifts).into(holder.image);
                break;
            case "10":
                Glide.with(activity).load(R.drawable.food_joint).into(holder.image);
                break;
            case "11":
                Glide.with(activity).load(R.drawable.glasses).into(holder.image);
                break;
            case "12":
                Glide.with(activity).load(R.drawable.book).into(holder.image);
                break;
            case "13":
                Glide.with(activity).load(R.drawable.flower).into(holder.image);
                break;
            case "14":
                Glide.with(activity).load(R.drawable.spa).into(holder.image);
                break;
            case "15":
                Glide.with(activity).load(R.drawable.salon).into(holder.image);
                break;
            case "16":
                Glide.with(activity).load(R.drawable.luggage).into(holder.image);
                break;
            case "17":
                Glide.with(activity).load(R.drawable.cosmetics).into(holder.image);
                break;
            default:
                Glide.with(activity).load(R.drawable.clothing).into(holder.image);
                break;
        }
    }

    private void openMap(double lat, double lon) {
        Uri uri = Uri.parse("http://maps.google.com/maps?saddr="+latitude+","+longitude+"&daddr="+lat+","+lon);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(intent);
    }
    private static String distance(double lat1, double lat2, double lon1, double lon2)
    {
        final int R = 6380;
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
    @Override
    public long getItemId(int position){return position;}
    @Override
    public int getItemViewType(int position){return position;}
}