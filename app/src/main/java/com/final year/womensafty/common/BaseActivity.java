package com.pemchip.womensafty.common;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pemchip.womensafty.R;
import com.pemchip.womensafty.SheGuard;
import com.pemchip.womensafty.config.Prefs;
import com.pemchip.womensafty.model.ContactModel;
import com.pemchip.womensafty.service.NotificationHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {

    private Context mContext;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private final SmsManager smsManager = SmsManager.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // 5 seconds
        locationRequest.setFastestInterval(3000); // 3 seconds
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    Log.d("LOCATION", "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());

                    String customerLat = SharedPrefManager.getInstance(mContext).getString(Constants.ORIGIN_LATITUDE,"");
                    String customerLong = SharedPrefManager.getInstance(mContext).getString(Constants.ORIGIN_LONGITUDE,"");
                    String BOUNDARY_LIMIT = SharedPrefManager.getInstance(mContext).getString(Constants.BOUNDARY_LIMIT, "0");
                    if(!BOUNDARY_LIMIT.equals("0")){
                        float km = calculateDistanceInKm(location.getLatitude(),location.getLongitude(),Double.parseDouble(customerLat),Double.parseDouble(customerLong));
                        float BL = Float.parseFloat(BOUNDARY_LIMIT);
                        Log.d("DISTANCE", "Distance: " + km + " km");
                        if(km>BL){
                            Log.d("Boundary","Crossed");
                            SharedPrefManager.getInstance(mContext).saveBoolean(Constants.BOUNDARY_CROSSED, true);
                            NotificationHelper.showLocalNotification(mContext, "Hello!", "You are crossed your boundary");
                            sendBoundarySMS("1",location.getLatitude() ,location.getLongitude());
                        }else{
                            boolean isBoundaryCrossed = SharedPrefManager.getInstance(mContext).getBoolean(Constants.BOUNDARY_CROSSED,false);
                            if(isBoundaryCrossed){
                                SharedPrefManager.getInstance(mContext).saveBoolean(Constants.BOUNDARY_CROSSED, false);
                                NotificationHelper.showLocalNotification(mContext, "Hello!", "You are entered your boundary");
                                sendBoundarySMS("2",location.getLatitude() ,location.getLongitude());
                                Log.d("Boundary", "Entered");
                            }
                        }
                    }
                }
            }
        };

        // Start location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void sendBoundarySMS(String mType, double latitude, double longitude) {

        ArrayList<ContactModel> contacts = new ArrayList<>();
        Gson gson = SheGuard.GSON;
        String jsonContacts = Prefs.getString(Constants.CONTACTS_LIST, "");

        if (!jsonContacts.isEmpty()) {
            Type type = new TypeToken<List<ContactModel>>() {
            }.getType();
            contacts.addAll(gson.fromJson(jsonContacts, type));
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String mLocation = "https://maps.google.com/maps?q=loc:" + latitude + "," + longitude;

        for (ContactModel contact : contacts) {
            if(mType.equals("1")){
                smsManager.sendTextMessage(contact.getPhone(), null, getString(R.string.exit_message, contact.getName(), mLocation), null, null);
            }else{
                smsManager.sendTextMessage(contact.getPhone(), null, getString(R.string.enter_message, contact.getName(), mLocation), null, null);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public static float calculateDistanceInKm(double lat1, double lon1, double lat2, double lon2) {
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lon1);

        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lon2);

        float distanceInMeters = loc1.distanceTo(loc2);
        return distanceInMeters / 1000f; // convert to kilometers
    }
}
