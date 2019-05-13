package com.gsorrentino.micoapp;

import android.Manifest;
import android.content.Intent;
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
    static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    static String PERMISSION_CHANNEL_ID = "Permissions";
    static int PERMISSION_NOTIFICATION_ID = 1;

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
        // Inflate the layout for this fragment
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkManageLocationPermissions();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.498955, 11.327591), 6f));
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
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
             /*Permission has already been granted*/
            mMap.setMyLocationEnabled(true);
            return true;
        }
        return false;
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
