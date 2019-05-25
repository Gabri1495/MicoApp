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
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;
import com.gsorrentino.micoapp.util.Costanti;

import java.util.Objects;

/**
 * Un {@link Fragment} che gestisce fra le altre cose
 * la visualizzazione della {@link GoogleMap}
 */
public class MapCustomFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener {

    /*Recupero della visualizzazione precedente*/
    private double lat;
    private double lng;
    private float zoom;
    private SharedPreferences sharedPrefs;

    /*Gestione dell'aggiornamento posizione*/
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private MicoAppDatabase db;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker marker;
    /*Mantengo la posizione corrente di modo da allegarla all'Intent*/
    private LatLng currentPosition;
    /*Per permettere utilizzo della mappa anche senza localizzazione attiva*/
    private boolean alreadyShowed;


    public MapCustomFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (db == null)
            this.db = MicoAppDatabase.getInstance(getActivity(), false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        /*Inizializzo la variabile*/
        alreadyShowed = false;

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
        return inflater.inflate(R.layout.fragment_map_custom, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        sharedPrefs = Objects.requireNonNull(getActivity()).getSharedPreferences(Costanti.SHARED_PREFERENCES, 0);
        lat = Double.longBitsToDouble(sharedPrefs.getLong(Costanti.LAT, Double.doubleToLongBits(Costanti.LAT_DEFAULT)));
        lng = Double.longBitsToDouble(sharedPrefs.getLong(Costanti.LNG, Double.doubleToLongBits(Costanti.LNG_DEFAULT)));
        zoom = sharedPrefs.getFloat(Costanti.ZOOM, Costanti.ZOOM_DEFAULT);

        FloatingActionButton fab = Objects.requireNonNull(getActivity()).findViewById(R.id.fab);
        fab.setOnClickListener(view1 -> {
            retrieveCurrentLocation();
            Intent intent = new Intent(getActivity(), EditFindActivity.class);
            /*Se è stato impostato un marker esso verrà usato per le coordinate,
             * altrimenti verrà recuperata la posizione attuale*/
            intent.putExtra(Costanti.INTENT_LATLNG,
                    marker != null ? marker.getPosition() : currentPosition == null ? new LatLng(0f, 0f) : currentPosition);
            startActivity(intent);
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
        /*Se la mappa è già stata creata mi salvo la posizione mostrata*/
        if(mMap != null) {
            CameraPosition tmpPos = mMap.getCameraPosition();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putLong(Costanti.LAT, Double.doubleToRawLongBits(tmpPos.target.latitude));
            editor.putLong(Costanti.LNG, Double.doubleToRawLongBits(tmpPos.target.longitude));
            editor.putFloat(Costanti.ZOOM, tmpPos.zoom);
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
        /*Cerco di avere già pronte in memoria le coordinate appena la mappa è pronta*/
        retrieveCurrentLocation();
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


    /**
     * Dopo aver invocato {@link MapCustomFragment#checkManageLocationPermissions()}
     * recupera la posizione attuale e la salva nel campo interno privato.
     * In caso di insuccesso, se anche il campo privato non è inizializzato,
     * imposta le coordinate di 0.0, 0.0; in caso di solo errore verranno
     * lasciate le coordinate precedentemente ottenute
     */
    private void retrieveCurrentLocation() {
        if(checkManageLocationPermissions()) {
            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(Objects.requireNonNull(getActivity()), location -> {
                            if (location == null) {
                                if (currentPosition == null) {
                                    currentPosition = new LatLng(0f, 0f);
                                }
                            } else {
                                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                            }
                        });
            } catch (SecurityException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.error_location, Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * Controlla se il permesso di Localizzazione sia concesso o meno.
     * Nel caso non lo sia provvede a richiederlo ed eventualmente mostra notifica
     * informativa sull'utilizzo che viene fatto del permesso.
     * Nel caso sia invece concesso abilita il pulsante nella {@link GoogleMap} per
     * ottenere la posizione corrente
     *
     * @return true solo se il permesso è già stato concesso
     */
    private boolean checkManageLocationPermissions() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

             /*Permission is not granted. Should we show an explanation?*/
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), Costanti.PERMISSION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_menu_map)
                        .setContentTitle(getString(R.string.permission_loc_title))
                        .setContentText(getString(R.string.permission_loc_explanation))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.permission_loc_explanation)))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat.from(getActivity()).notify(Costanti.PERMISSION_LOCALIZATION_NOTIFICATION_ID, builder.build());
            }
            /*No explanation needed; request the permission*/
            /*La risposta sarà inviata in callback a questo fragment*/
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Costanti.REQUEST_ACCESS_FINE_LOCATION_PERMISSIONS);
        } else {
             /*Permission has already been granted*/
            if(mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
            return true;
        }
        return false;
    }


    /*Warning soppresso perché eseguo l'operazione solo se nella callback
    * ricevo un successo nella richiesta dell'autorizzazione; in tal caso
    * sono quindi certo che avrò l'autorizzazione*/
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == Costanti.REQUEST_ACCESS_FINE_LOCATION_PERMISSIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(mMap != null) {
                    mMap.setMyLocationEnabled(true);
                }
            }
        }
    }


    /**
     * Inizializza una {@link LocationRequest} e contestualmente controlla
     * le impostazioni del sistema operativo per verificare che sia attiva
     * la localizzazione. In caso contrario provvede a richiederne
     * l'attivazione all'utente
     */
    private void manageLocationUpdate() {
        /*Evito di settare nuovamente la LocationRequest nel caso lo sia già*/
        if(locationRequest == null) {
            locationRequest = LocationRequest.create();
            locationRequest.setInterval(15000);
            locationRequest.setFastestInterval(10000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        final Activity activity = getActivity();
        if(activity != null) {
            SettingsClient client = LocationServices.getSettingsClient(activity);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

            task.addOnSuccessListener(activity, locationSettingsResponse -> {});

            task.addOnFailureListener(activity, e -> {
                if (e instanceof ResolvableApiException && !alreadyShowed) {
                     /*Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                     * In ogni caso mostro max una volta il dialog all'utente ogni onCreate*/
                    try {
                         /*Show the dialog by calling startIntentSenderForResult(),
                          and check the result in onActivityResult()*/
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        startIntentSenderForResult(resolvable.getResolution().getIntentSender(),
                                Costanti.REQUEST_CHECK_LOCALIZATION_SETTINGS,
                                null, 0, 0, 0, null);
                        alreadyShowed = true;
                    } catch (IntentSender.SendIntentException sendEx) {
                        Toast.makeText(activity, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    /*Callback di ritorno con la risposta dell'utente alla richiesta di
    * abilitare la localizzazione del dispositivo*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        manageLocationUpdate();
    }
}
