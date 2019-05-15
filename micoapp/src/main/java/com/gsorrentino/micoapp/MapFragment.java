package com.gsorrentino.micoapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;

import java.util.Objects;


public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener {

     /*MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
     app-defined int constant. The callback method gets the result of the request.*/
    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    static String PERMISSION_CHANNEL_ID = "Permissions";
    static int PERMISSION_NOTIFICATION_ID = 1;

    private static final double LAT_DEFAULT = 44.498955;
    private static final double LNG_DEFAULT = 11.327591;
    private static final float ZOOM_DEFAULT = 6f;

    private double lat;
    private double lng;
    private float zoom;
    private SharedPreferences sharedPrefs;

    private MicoAppDatabase db;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker marker;
    private LatLng currentPosition;

    public MapFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (db == null)
            this.db = MicoAppDatabase.getInstance(getActivity(), false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        sharedPrefs = Objects.requireNonNull(getActivity()).getSharedPreferences("pref", 0);
        lat = Double.longBitsToDouble(sharedPrefs.getLong("lat", Double.doubleToLongBits(LAT_DEFAULT)));
        lng = Double.longBitsToDouble(sharedPrefs.getLong("lng", Double.doubleToLongBits(LNG_DEFAULT)));
        zoom = sharedPrefs.getFloat("zoom", ZOOM_DEFAULT);
    }

    @Override
    public void onPause(){
        super.onPause();
        CameraPosition tmpPos = mMap.getCameraPosition();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong("lat", Double.doubleToRawLongBits(tmpPos.target.latitude));
        editor.putLong("lng", Double.doubleToRawLongBits(tmpPos.target.longitude));
        editor.putFloat("zoom", tmpPos.zoom);
        editor.apply();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
        checkManageLocationPermissions();
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
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
            mMap.setMyLocationEnabled(true);
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
                mMap.setMyLocationEnabled(true);
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

    @Override
    public void onMapClick(LatLng latLng) {
        if(marker != null)
            marker.remove();
        marker = mMap.addMarker(new MarkerOptions().position(latLng));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(marker != null) {
            marker.remove();
            marker = null;
        }
    }
}
