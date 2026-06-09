package com.example.shealert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;

public class SafeRouteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private AutoCompleteTextView searchLocation;
    private View routePanel;
    private MaterialCardView riskCard;
    private android.widget.TextView txtRiskLevel, txtRiskMessage;
    private android.widget.EditText fromLocation, toLocation;
    private android.widget.Button btnFindRoute;

    private DatabaseReference riskRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_route);

        // Initialize Views
        searchLocation = findViewById(R.id.searchLocation);
        routePanel = findViewById(R.id.routePanel);
        riskCard = findViewById(R.id.riskCard);
        txtRiskLevel = findViewById(R.id.txtRiskLevel);
        txtRiskMessage = findViewById(R.id.txtRiskMessage);
        fromLocation = findViewById(R.id.fromLocation);
        toLocation = findViewById(R.id.toLocation);
        btnFindRoute = findViewById(R.id.btnFindRoute);

        riskRef = FirebaseDatabase.getInstance().getReference("risk_data");

        // Toggle route panel
        FloatingActionButton btnRoute = findViewById(R.id.btnRoute);
        btnRoute.setOnClickListener(v -> {
            if (routePanel.getVisibility() == View.VISIBLE) {
                routePanel.setVisibility(View.GONE);
            } else {
                routePanel.setVisibility(View.VISIBLE);
            }
        });

        // Open Google Maps for route
        btnFindRoute.setOnClickListener(v -> {

            String from = fromLocation.getText().toString().trim();
            String to = toLocation.getText().toString().trim();

            if (from.isEmpty() || to.isEmpty()) {
                Toast.makeText(this, "Enter both locations", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1"
                    + "&origin=" + Uri.encode(from)
                    + "&destination=" + Uri.encode(to));

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No map application found", Toast.LENGTH_SHORT).show();
            }
        });

        // Hide risk when search cleared
        searchLocation.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    riskCard.setVisibility(View.GONE);
                }
            }
        });

        // Search action
        searchLocation.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE) {

                String locationName = searchLocation.getText().toString().trim();

                Geocoder geocoder = new Geocoder(SafeRouteActivity.this);

                try {
                    List<Address> addressList =
                            geocoder.getFromLocationName(locationName, 1);

                    if (addressList != null && !addressList.isEmpty()) {

                        Address address = addressList.get(0);

                        LatLng latLng = new LatLng(
                                address.getLatitude(),
                                address.getLongitude()
                        );

                        mMap.clear();
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(locationName));

                        mMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(latLng, 15));

                        String district = address.getSubAdminArea();

                        if (district != null) {
                            district = district.toLowerCase().trim();
                            district = district.replace(" district", "");
                            district = district.replace(" dist", "");
                            district = district.replace(".", "");
                            checkRisk(district);
                        } else {
                            checkRisk(locationName.toLowerCase().trim());
                        }

                        searchLocation.clearFocus();

                    } else {
                        Toast.makeText(this,
                                "Location not found",
                                Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return true;
            }

            return false;
        });

        // Load Map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        enableUserLocation();

        // Disable unwanted Google UI
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);

        // Hide risk when map tapped
        mMap.setOnMapClickListener(latLng -> {
            riskCard.setVisibility(View.GONE);
            routePanel.setVisibility(View.GONE);
        });
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            enableUserLocation();
        }
    }

    private void checkRisk(String place) {

        place = place.trim().toLowerCase();

        riskRef.child(place).get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) {
                showRisk("UNKNOWN AREA",
                        "No district-level safety data available.",
                        "#757575");
                return;
            }

            String level = snapshot.getValue(String.class);

            if (level == null) {
                showRisk("UNKNOWN AREA",
                        "No district-level safety data available.",
                        "#757575");
                return;
            }

            switch (level.toUpperCase()) {

                case "LOW":
                    showRisk("LOW RISK AREA",
                            "This area is relatively safe.",
                            "#2E7D32");
                    break;

                case "MEDIUM":
                    showRisk("MEDIUM RISK AREA",
                            "Stay alert while travelling.",
                            "#F9A825");
                    break;

                case "HIGH":
                    showRisk("HIGH RISK AREA",
                            "Avoid if possible or travel carefully.",
                            "#C62828");
                    break;

                default:
                    showRisk("UNKNOWN AREA",
                            "No district-level safety data available.",
                            "#757575");
            }
        });
    }

    private void showRisk(String title, String message, String colorHex) {

        riskCard.setAlpha(0f);
        riskCard.setVisibility(View.VISIBLE);
        riskCard.animate().alpha(1f).setDuration(300);

        txtRiskLevel.setText(title);
        txtRiskMessage.setText(message);

        int color = Color.parseColor(colorHex);
        riskCard.setCardBackgroundColor(color);
    }
}