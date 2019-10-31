package com.auxidos.offers.customers;

/**
 * Created by Nikhil on 08-01-2017.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

class SessionManager
{
    private SharedPreferences pref;
    private Editor editor;
    private String PREF_NAME = "NikhilApp";
    private static final String KEY_IS_LOGGEDIN = "isLoggedIn", SET_NAME = "AppUserName",
            SET_EMAIL = "AppEmail", SET_USER = "AppUSER", USER_TOKEN = "FireToken";

    SessionManager(Context context)
    {
        int PRIVATE_MODE = 0;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    boolean setName(String key)
    {
        editor.putString(SET_NAME, key);
        return editor.commit();
    }
    boolean setEmail(String key)
    {
        editor.putString(SET_EMAIL, key);
        return editor.commit();
    }
    boolean setUser(String key)
    {
        editor.putString(SET_USER, key);
        return editor.commit();
    }
    boolean setLoggedIn(boolean key)
    {
        editor.putBoolean(KEY_IS_LOGGEDIN, key);
        return editor.commit();
    }
    public boolean setToken(String token)
    {
        editor.putString(USER_TOKEN, token);
        return editor.commit();
    }

    public String getUserToken(){return pref.getString(USER_TOKEN, null);}
    String getName(){
        return pref.getString(SET_NAME, null);
    }
    String getUser(){
        return pref.getString(SET_USER, null);
    }
    String getEmail(){
        return pref.getString(SET_EMAIL, null);
    }
    boolean getLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
}