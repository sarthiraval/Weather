package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    ImageView searchIcon, TempImage;
    TextView citynametf, Temperaturetf, Conditiontf, ForecastTitle;
    TextInputLayout city_name_input;
    ConstraintLayout cl;
    RecyclerView Rv;
    ArrayList<RvModel> rv_list;
    int PERMISSION_CODE = 1;
    String cityName;
    LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        citynametf = findViewById(R.id.cityname);
        Temperaturetf = findViewById(R.id.Temperature);
        Conditiontf = findViewById(R.id.Condition);
        cl = findViewById(R.id.cl);
        cl.setVisibility(View.INVISIBLE);
        Rv = findViewById(R.id.Rv);
        searchIcon = findViewById(R.id.searchIcon);
        TempImage = findViewById(R.id.TempImage);
        city_name_input = findViewById(R.id.city_name_input);
        rv_list = new ArrayList<>();

        checkPermissionsandGPS();
    }

    public boolean enabledGPS() {

       LocationManager locationManager = (LocationManager) getSystemService
                (Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }
        return true;
    }


    public void buildAlertMessageNoGps() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        dialog.dismiss();
                        System.exit(0);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        dialog.dismiss();
                        System.exit(0);
                    }
                });
        builder.create();
        builder.show();

    }

    @SuppressLint("MissingPermission")
    public void getLocation() {

        try{
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 5, MainActivity.this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 5, MainActivity.this);
        }catch (Exception e){
            System.out.println("EXCEPTION "+e);
        }

        cl.setVisibility(View.VISIBLE);

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city_Name = city_name_input.getEditText().getText().toString();
                if (city_Name == null || city_Name.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please add a city name", Toast.LENGTH_SHORT).show();
                } else {
                    GetWeatherInfo(city_Name);
                }
            }
        });
    }

    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (hasAllPermissionsGranted(grantResults)) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                getLocation();
            } else {
                Toast.makeText(this, "Permissions denied!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void GetWeatherInfo(String cityname) {
        String url = "https://api.weatherapi.com/v1/forecast.json?key=6a4ea0bd6d724c1aa22215713210408&q=" + cityname + "&days=1&aqi=no&alerts=no";
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                rv_list.clear();
                try {
                    String cityname = response.getJSONObject("location").getString("name") + "," +
                            response.getJSONObject("location").getString("country");
                    citynametf.setText(cityname);

                    String temp = response.getJSONObject("current").getString("temp_c");
                    Temperaturetf.setText(temp + "°c");

                    String condition = response.getJSONObject("current").getJSONObject("condition")
                            .getString("text");
                    Conditiontf.setText(condition);

                    String icon = response.getJSONObject("current").getJSONObject("condition")
                            .getString("icon");

                    Picasso.get().load("https:" + icon).into(TempImage);

                    //Check isDay or not
                    int isDay = response.getJSONObject("current").getInt("is_day");

                    if (isDay == 0) {
                        cl.setBackgroundResource(R.drawable.pexel_night);

                    } else {
                        cl.setBackgroundResource(R.drawable.pexel_morning);
                    }


                    JSONArray hourArray = response.getJSONObject("forecast").getJSONArray("forecastday")
                            .getJSONObject(0).getJSONArray("hour");


                    for (int i = 0; i <= hourArray.length(); i++) {

                        JSONObject hourObj = hourArray.getJSONObject(i);

                        String temp_by_time = hourObj.getString("temp_c");
                        String windSpeed_by_time = hourObj.getString("wind_kph");
                        String Icon_by_time = hourObj.getJSONObject("condition").
                                getString("icon");
                        String time_by_time = hourObj.getString("time");

                        RvModel rvModel = new RvModel(temp_by_time, time_by_time, Icon_by_time,
                                windSpeed_by_time);

                        rv_list.add(rvModel);

                        RvAdapter rvAdapter = new RvAdapter(rv_list, MainActivity.this);

                        Rv.setAdapter(rvAdapter);


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                citynametf.setText("Not Found");
                rv_list.clear();

                RvModel rvModel = new RvModel("", "", "", "");

                rv_list.add(rvModel);

                RvAdapter rvAdapter = new RvAdapter(rv_list, MainActivity.this);

                Rv.setAdapter(rvAdapter);
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPermissionsandGPS();
    }

    public void checkPermissionsandGPS(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }else if(!enabledGPS()){
            buildAlertMessageNoGps();
        }
        else {
            getLocation();
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {

        if(citynametf.getText().toString() == ""){

            try {
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                List<Address> address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                cityName = address.get(0).getLocality();
                if (cityName == "" || cityName == null) {
                    GetWeatherInfo("Not Found");
                } else {
                    GetWeatherInfo(cityName);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        buildAlertMessageNoGps();
    }
}
