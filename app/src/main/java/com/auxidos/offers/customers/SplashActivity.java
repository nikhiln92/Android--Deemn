package com.auxidos.offers.customers;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import yanzhikai.textpath.AsyncTextPathView;
import yanzhikai.textpath.PathAnimatorListener;

public class SplashActivity extends AppCompatActivity
{
    SessionManager session;
    AsyncTextPathView text;
    boolean active;

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.activity_splash);

        active = true;
        session = new SessionManager(this);
        text = findViewById(R.id.text);
        text.startAnimation(0,1);
        text.setAnimatorListener(new PathAnimatorListener(){
            @Override
            public void onAnimationRepeat(Animator animator){}
            @Override
            public void onAnimationStart(Animator animator){
                isCancel = false;
            }
            @Override
            public void onAnimationEnd(Animator animator){
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        active = true;
                        text.setEnabled(true);
                        if(active)
                        {
                            if(session.getLoggedIn())
                                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            else
                                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        }
                    }
                }, 800);
            }
            @Override
            public void onAnimationCancel(Animator animator){
                isCancel = true;
            }
        });
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        active = false;
        text.stopAnimation();
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        active = false;
        text.stopAnimation();
    }
    @Override
    protected void onPause()
    {
        super.onPause();
        active = false;
        text.stopAnimation();
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        if(!active)
        {
            text.startAnimation(0, 1);
            active = true;
        }
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        if(!active)
        {
            text.startAnimation(0, 1);
            active = true;
        }
    }
}