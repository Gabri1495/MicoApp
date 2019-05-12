package com.gsorrentino.micoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;

import java.util.Objects;


public class MapFragment extends Fragment implements OnMapReadyCallback {

     /*MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
     app-defined int constant. The callback method gets the result of the request.*/
    static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    static String PERMISSION_CHANNEL_ID = "Permissions";
    static int PERMISSION_NOTIFICATION_ID = 1;

    private MicoAppDatabase db;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

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
                Intent intent = new Intent(getActivity(), EditFindActivity.class);
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

        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        if(checkManageLocationPermissions()) {
            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(Objects.requireNonNull(getActivity()), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                double lat;
                                double lng;
                                String titleMarker;
                                if (location == null) {
                                    lat = 44.498955;
                                    lng = 11.327591;
                                    titleMarker = "Marker in Bologna";
                                } else {
                                    lat = location.getLatitude();
                                    lng = location.getLongitude();
                                    titleMarker = "Marker on your current position";
                                }
                                LatLng bologna = new LatLng(lat, lng);
                                mMap.addMarker(new MarkerOptions().position(bologna).title(titleMarker));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bologna, 16f));

                            }
                        });
            }catch (SecurityException e){e.printStackTrace();}
        }
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
            return true;
        }
        return false;
    }
}
