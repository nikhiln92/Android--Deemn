package com.auxidos.offers.customers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener
{
    AppCompatEditText name, email, password;
    TextView login;
    Button submit;
    ImageView google, facebook;
    SessionManager session;
    ProgressDialog progressDialog;
    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    CallbackManager callBackManager;

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.activity_register);

        session = new SessionManager(this);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        submit = findViewById(R.id.submit);
        login = findViewById(R.id.login);
        google = findViewById(R.id.google);
        facebook = findViewById(R.id.facebook);

        progressDialog = new ProgressDialog(this, R.style.ProgressDialog);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameText = name.getText().toString().trim();
                String emailText = email.getText().toString().trim();
                String passwordText = password.getText().toString().trim();

                if(nameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty())
                    Toast.makeText(RegisterActivity.this, "Enter all details", Toast.LENGTH_LONG).show();
                else
                {
                    ArrayMap<String, String> data = new ArrayMap<>();
                    data.put("password", passwordText);
                    data.put("name", nameText);
                    data.put("email", emailText);
                    registerUser(data);
                }
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> permissionNeeds = Collections.singletonList("email");
                LoginManager.getInstance().logInWithReadPermissions(RegisterActivity.this, permissionNeeds);
            }
        });
        callBackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callBackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        final ArrayMap<String, String> details = new ArrayMap<>();
                        if(loginResult.getAccessToken().getToken() != null)
                            details.put("accessToken", loginResult.getAccessToken().getToken());
                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        try
                                        {
                                            String id = object.getString("id");
                                            details.put("password", id);
                                            details.put("name", object.getString("name"));
                                            details.put("email", object.getString("email"));
                                            details.put("social", "facebook");
                                            registerUser(details);
                                        }
                                        catch (JSONException e)
                                        { }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }
                    @Override
                    public void onCancel() {
                        Toast.makeText(RegisterActivity.this, "Request was cancelled", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(RegisterActivity.this, "Please try another method to register for now", Toast.LENGTH_LONG).show();                    }
                });
    }
    void registerUser(ArrayMap<String, String> data)
    {
        data.put("condition", "0");
        progressDialog.show();
        String appToken = session.getUserToken();
        if(appToken != null)
        {
            data.put("appToken", session.getUserToken());

            Gson gson = new GsonBuilder().setLenient().create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(AppConfig.LOCATION)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            UserCredentialsApi balancesRequestApi = retrofit.create(UserCredentialsApi.class);
            Call<UserCredentials> call;
            call = balancesRequestApi.getData(data);
            call.enqueue(new Callback<UserCredentials>() {
                @Override
                public void onResponse(Call<UserCredentials> call, Response<UserCredentials> response) {
                    try
                    {
                        if(progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                        UserCredentials data = response.body();
                        String value = data.getValue();
                        if(value.equalsIgnoreCase("done"))
                        {
                            session.setLoggedIn(true);
                            session.setName(data.getName());
                            session.setEmail(data.getEmail());
                            session.setUser(data.getUser());
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        }
                        else
                            Toast.makeText(RegisterActivity.this, value, Toast.LENGTH_LONG).show();
                    }
                    catch(Exception e){
                        if(progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<UserCredentials> call, Throwable t) {
                    if(progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
                }
            });
        }
        else
            Toast.makeText(this, "Could not verify device. Kindly clear cache and try again", Toast.LENGTH_LONG).show();
    }
    private void handleSignInResult(GoogleSignInResult result)
    {
        if (result.isSuccess())
        {
            try
            {
                GoogleSignInAccount acct = result.getSignInAccount();
                ArrayMap<String, String> details = new ArrayMap<>();
                details.put("password",acct.getId());
                details.put("name",acct.getDisplayName());
                details.put("email",acct.getEmail());
                details.put("social","google");
                registerUser(details);
            }
            catch(Exception e)
            {
                Toast.makeText(this, "Could not register. Try different method", Toast.LENGTH_LONG).show();
            }
        }
        else
            Toast.makeText(this, "Could not register. Try different method", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else
            callBackManager.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}