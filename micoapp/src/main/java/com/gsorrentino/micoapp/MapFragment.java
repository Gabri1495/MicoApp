package com.gsorrentino.micoapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;

import java.util.Objects;


public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener {

    /*MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
    app-defined int constant. The callback method gets the result of the request.*/
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    static String PERMISSION_CHANNEL_ID = "Permissions";
    static int PERMISSION_NOTIFICATION_ID = 1;

    private static final double LAT_DEFAULT = 44.498955;
    private static final double LNG_DEFAULT = 11.327591;
    private static final float ZOOM_DEFAULT = 6f;

    private double lat;
    private double lng;
    private float zoom;
    private SharedPreferences sharedPrefs;

    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private MicoAppDatabase db;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker marker;
    private LatLng currentPosition;

    public MapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (db == null)
            this.db = MicoAppDatabase.getInstance(getActivity(), false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                }
            }
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        sharedPrefs = Objects.requireNonNull(getActivity()).getSharedPreferences("pref", 0);
        lat = Double.longBitsToDouble(sharedPrefs.getLong("lat", Double.doubleToLongBits(LAT_DEFAULT)));
        lng = Double.longBitsToDouble(sharedPrefs.getLong("lng", Double.doubleToLongBits(LNG_DEFAULT)));
        zoom = sharedPrefs.getFloat("zoom", ZOOM_DEFAULT);

        FloatingActionButton fab = Objects.requireNonNull(getActivity()).findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrieveCurrentLocation();
                Intent intent = new Intent(getActivity(), EditFindActivity.class);
                /*Se è stato impostato un marker esso verrà usato per le coordinate,
                 * altrimenti verrà recuperata la posizione attuale*/
                intent.putExtra(getString(R.string.intent_latlng),
                        marker != null ? marker.getPosition() : currentPosition);
                startActivity(intent);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveCurrentLocation();
        manageLocationUpdate();
        if (locationRequest != null)
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }

    }

    @Override
    public void onPause(){
        super.onPause();
        if(mMap != null) {
            CameraPosition tmpPos = mMap.getCameraPosition();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putLong("lat", Double.doubleToRawLongBits(tmpPos.target.latitude));
            editor.putLong("lng", Double.doubleToRawLongBits(tmpPos.target.longitude));
            editor.putFloat("zoom", tmpPos.zoom);
            editor.apply();
        }
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        /*Cerco di avere già pronte in memoria le coordinate*/
        retrieveCurrentLocation();
    }

    /*Return true only if permission is already granted*/
    private boolean checkManageLocationPermissions() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

             /*Permission is not granted
             Should we show an explanation?*/
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), PERMISSION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_menu_map)
                        .setContentTitle(getString(R.string.permission_title))
                        .setContentText(getString(R.string.permission_explanation))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.permission_explanation)))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat.from(getActivity()).notify(PERMISSION_NOTIFICATION_ID, builder.build());
            }
            /*No explanation needed; request the permission*/
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
             /*Permission has already been granted*/
            if(mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
            return true;
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(mMap != null) {
                    mMap.setMyLocationEnabled(true);
                }
            }
        }
    }

    private void retrieveCurrentLocation() {
        if(checkManageLocationPermissions()) {
            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(Objects.requireNonNull(getActivity()), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location == null) {
                                     currentPosition = new LatLng(0f, 0f);
                                } else {
                                    currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                                }
                            }
                        });
            } catch (SecurityException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.error_location, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void manageLocationUpdate() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(15000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Activity activity = getActivity();
        if(activity != null) {
            SettingsClient client = LocationServices.getSettingsClient(activity);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

            task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    // All location settings are satisfied. The client can initialize
                    // location requests here.

                }
            });

            task.addOnFailureListener(getActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
//                            resolvable.startResolutionForResult(getActivity(),
//                                    REQUEST_CHECK_SETTINGS);
                            startIntentSenderForResult(resolvable.getResolution().getIntentSender(), REQUEST_CHECK_SETTINGS,
                                    null, 0, 0, 0, null);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        manageLocationUpdate();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(marker != null)
            marker.remove();
        marker = mMap.addMarker(new MarkerOptions()
                .draggable(true)
                .position(latLng)
                .title("" + latLng.latitude + ", " + latLng.longitude));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(marker != null) {
            marker.remove();
            marker = null;
        }
    }
}
