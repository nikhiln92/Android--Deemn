package com.auxidos.offers.customers;

import com.google.gson.annotations.SerializedName;

public class UserCredentials
{
    @SerializedName("value")
    private String value;
    @SerializedName("email")
    private String email;
    @SerializedName("name")
    private String name;
    @SerializedName("user")
    private String user;

    public UserCredentials() {}

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}