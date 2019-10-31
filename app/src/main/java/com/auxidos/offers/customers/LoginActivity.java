package com.auxidos.offers.customers;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener
{
    AppCompatEditText email, password;
    TextView register, forgot;
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
        setContentView(R.layout.activity_login);

        session = new SessionManager(this);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        forgot = findViewById(R.id.forgot);
        submit = findViewById(R.id.submit);
        register = findViewById(R.id.register);
        google = findViewById(R.id.google);
        facebook = findViewById(R.id.facebook);

        progressDialog = new ProgressDialog(this, R.style.ProgressDialog);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEmailDialog();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailText = email.getText().toString().trim();
                String passwordText = password.getText().toString().trim();

                if(emailText.isEmpty() || passwordText.isEmpty())
                    Toast.makeText(LoginActivity.this, "Enter all details", Toast.LENGTH_LONG).show();
                else
                {
                    ArrayMap<String, String> data = new ArrayMap<>();
                    data.put("password", passwordText);
                    data.put("email", emailText);
                    String appToken = session.getUserToken();
                    if(appToken != null)
                    {
                        data.put("appToken", appToken);
                        loginUser(data);
                    }
                    else
                        Toast.makeText(LoginActivity.this, "Kindly clear cache and try again", Toast.LENGTH_LONG).show();
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
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, permissionNeeds);
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
                                            loginUser(details);
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
                        Toast.makeText(LoginActivity.this, "Request was cancelled", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(LoginActivity.this, "Please try another method to login for now", Toast.LENGTH_LONG).show();                    }
                });
    }

    private void showEmailDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
        alertDialog.setTitle("Email");
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Enter your email address");

        final EditText input = new EditText(LoginActivity.this);
        input.setHint("Email");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(60, 0, 60, 0);
        input.setLayoutParams(lp);
        LinearLayout layout = new LinearLayout(LoginActivity.this);
        layout.addView(input);
        alertDialog.setView(layout);
        alertDialog.setIcon(R.drawable.logo);

        alertDialog.setPositiveButton("CONTINUE",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String emailText = input.getText().toString().trim();
                        if(!emailText.isEmpty())
                        {
                            ArrayMap<String, String> data = new ArrayMap<>();
                            data.put("condition", "1");
                            data.put("email", emailText);
                            resetPassword(data, dialog);
                        }
                        else
                        {
                            showEmailDialog();
                            Toast.makeText(LoginActivity.this, "Enter email id", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        alertDialog.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    void resetPassword(ArrayMap<String, String> data, final DialogInterface dialog)
    {
        progressDialog.show();
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.LOCATION)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ResetPasswordApi resetPasswordApi = retrofit.create(ResetPasswordApi.class);
        Call<ResetPassword> call;
        call = resetPasswordApi.getData(data);
        call.enqueue(new Callback<ResetPassword>() {
            @Override
            public void onResponse(Call<ResetPassword> call, Response<ResetPassword> response) {
                try
                {
                    if(progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    final ResetPassword data = response.body();
                    String value = data.getValue();
                    if(value.equalsIgnoreCase("done"))
                    {
                        dialog.dismiss();
                        showSecurityCodeDialog(data);
                    }
                    else
                    {
                        showEmailDialog();
                        Toast.makeText(LoginActivity.this, value, Toast.LENGTH_LONG).show();
                    }
                }
                catch(Exception e){
                    if(progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ResetPassword> call, Throwable t) {
                if(progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSecurityCodeDialog(final ResetPassword data)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
        alertDialog.setTitle("Security Code");
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Enter security code sent to your email address");

        final EditText input = new EditText(LoginActivity.this);
        input.setHint("Security Code");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(60, 0, 60, 0);
        input.setLayoutParams(lp);
        LinearLayout layout = new LinearLayout(LoginActivity.this);
        layout.addView(input);
        alertDialog.setView(layout);
        alertDialog.setIcon(R.drawable.logo);

        alertDialog.setPositiveButton("CONTINUE",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String codeText = input.getText().toString().trim();
                        if(!codeText.isEmpty())
                        {
                            dialog.dismiss();
                            ArrayMap<String, String> codeData = new ArrayMap<>();
                            codeData.put("condition", "2");
                            codeData.put("code", codeText);
                            codeData.put("email", data.getData());
                            sendPasswordCode(codeData, dialog, data);
                        }
                        else
                        {
                            showSecurityCodeDialog(data);
                            Toast.makeText(LoginActivity.this, "Enter security code", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        alertDialog.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    void sendPasswordCode(ArrayMap<String, String> data, final DialogInterface dialog, final ResetPassword resetPasswordData)
    {
        progressDialog.show();
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.LOCATION)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ResetPasswordApi resetPasswordApi = retrofit.create(ResetPasswordApi.class);

        Call<ResetPassword> call;
        call = resetPasswordApi.getData(data);
        call.enqueue(new Callback<ResetPassword>() {
            @Override
            public void onResponse(Call<ResetPassword> call, Response<ResetPassword> response) {
                try
                {
                    if(progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    final ResetPassword data = response.body();
                    String value = data.getValue();
                    if(value.equalsIgnoreCase("done"))
                    {
                        dialog.dismiss();
                        showNewPasswordDialog(data);
                    }
                    else
                    {
                        showSecurityCodeDialog(resetPasswordData);
                        Toast.makeText(LoginActivity.this, value, Toast.LENGTH_LONG).show();
                    }
                }
                catch(Exception e){
                    Toast.makeText(LoginActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResetPassword> call, Throwable t) {
                if(progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showNewPasswordDialog(final ResetPassword data) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
        alertDialog.setTitle("New Password");
        alertDialog.setMessage("Enter new password");

        final EditText input = new EditText(LoginActivity.this);
        input.setHint("Password");
        input.setMaxLines(1);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(60, 0, 60, 0);
        input.setLayoutParams(lp);
        LinearLayout layout = new LinearLayout(LoginActivity.this);
        layout.addView(input);
        alertDialog.setView(layout);
        alertDialog.setIcon(R.drawable.logo);

        alertDialog.setPositiveButton("CONTINUE",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String codeText = input.getText().toString().trim();
                        if(!codeText.isEmpty())
                        {
                            dialog.dismiss();
                            ArrayMap<String, String> codeData = new ArrayMap<>();
                            codeData.put("condition", "3");
                            codeData.put("p", codeText);
                            codeData.put("email", data.getData());
                            sendNewPassword(codeData, dialog, data);
                        }
                        else
                        {
                            showNewPasswordDialog(data);
                            Toast.makeText(LoginActivity.this, "Enter security code", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        alertDialog.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    private void sendNewPassword(ArrayMap<String, String> data, final DialogInterface dialog, final ResetPassword resetPasswordData)
    {
        progressDialog.show();
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.LOCATION)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ResetPasswordApi resetPasswordApi = retrofit.create(ResetPasswordApi.class);

        Call<ResetPassword> call;
        call = resetPasswordApi.getData(data);
        call.enqueue(new Callback<ResetPassword>() {
            @Override
            public void onResponse(Call<ResetPassword> call, Response<ResetPassword> response) {
                try
                {
                    if(progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    final ResetPassword data = response.body();
                    String value = data.getValue();
                    if(value.equalsIgnoreCase("done"))
                    {
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Password Updated Successfully. Enter new password to continue", Toast.LENGTH_SHORT).show();
                        email.setText(data.getData());
                    }
                    else
                    {
                        showNewPasswordDialog(resetPasswordData);
                        Toast.makeText(LoginActivity.this, value, Toast.LENGTH_LONG).show();
                    }
                }
                catch(Exception e){
                    Toast.makeText(LoginActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResetPassword> call, Throwable t) {
                if(progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
            }
        });
    }
    void loginUser(ArrayMap<String, String> data)
    {
        data.put("condition", "1");
        progressDialog.show();
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
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                    else
                        Toast.makeText(LoginActivity.this, value, Toast.LENGTH_LONG).show();
                }
                catch(Exception e){
                    Toast.makeText(LoginActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UserCredentials> call, Throwable t) {
                if(progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, AppConfig.UNKNOWN_ERROR, Toast.LENGTH_LONG).show();
            }
        });
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
                loginUser(details);
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