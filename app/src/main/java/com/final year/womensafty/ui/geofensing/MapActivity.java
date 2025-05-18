package com.pemchip.womensafty.ui.geofensing;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pemchip.womensafty.R;
import com.pemchip.womensafty.common.Constants;
import com.pemchip.womensafty.common.SharedPrefManager;

import java.io.IOException;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private TextView addressTxt;
    private Button confirmAddress;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ProgressDialog pDialog;
    private double mOriginLattitude = 0.0;
    private double mOriginLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_screen);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading");
        pDialog.setCanceledOnTouchOutside(false);

        addressTxt = findViewById(R.id.address);
        confirmAddress = findViewById(R.id.confirmAddress);

        // Get the SupportMapFragment and request notification when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        confirmAddress.setOnClickListener(v -> {
            String address = addressTxt.getText().toString();
            if (!address.equals("Select your location")) {
                SharedPrefManager.getInstance(this).saveString(Constants.ORIGIN_LATITUDE, String.valueOf(mOriginLattitude));
                SharedPrefManager.getInstance(this).saveString(Constants.ORIGIN_LONGITUDE, String.valueOf(mOriginLongitude));
                showBoundaryDialogue();
            } else {
                Toast.makeText(MapActivity.this, "Select your address", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBoundaryDialogue() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter a boundary limit in KM.");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String userInput = input.getText().toString();
            if(userInput.isEmpty()){
                Toast.makeText(MapActivity.this, "Please enter a boundary", Toast.LENGTH_SHORT).show();
            }else{
                SharedPrefManager.getInstance(this).saveString(Constants.BOUNDARY_LIMIT, userInput);
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear(); // Remove previous markers
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
            getAddressFromLatLng(latLng);
        });
    }

    private void getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            mOriginLattitude = latLng.latitude;
            mOriginLongitude = latLng.longitude;
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);
                addressTxt.setText(addressText);
            } else {
                Toast.makeText(this, "No address found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error retrieving address", Toast.LENGTH_SHORT).show();
        }
    }
}

