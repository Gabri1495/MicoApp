package com.gsorrentino.micoapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.persistence.MicoAppDatabase;
import com.gsorrentino.micoapp.util.Costanti;
import com.gsorrentino.micoapp.util.ManagePermissions;

import java.text.DateFormat;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

/**
 * Un {@link Fragment} che gestisce fra le altre cose
 * la visualizzazione della {@link GoogleMap}
 */
public class MapCustomFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnInfoWindowClickListener {

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
    private Marker markerToBeUpdated;
    /*Mantengo la posizione corrente di modo da allegarla all'Intent*/
    private LatLng currentPosition;
    /*Per permettere utilizzo della mappa anche senza localizzazione attiva*/
    private boolean alreadyShowed;

    private Ritrovamento ritrovamento;


    public MapCustomFragment() {
    }

    public MapCustomFragment(Ritrovamento ritrovamento) {
        this.ritrovamento = ritrovamento;
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

        FloatingActionButton fab = Objects.requireNonNull(getActivity()).findViewById(R.id.map_fab);
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
        /*Controllo se devo mostrare il marker di un Ritrovamento*/
        if(ritrovamento != null) {
            setMarker(ritrovamento, mMap);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ritrovamento.getCoordinate(), zoom));
        }
        else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
        }
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
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


    @Override
    public void onInfoWindowClick(Marker iMarker) {
        Object tmp = iMarker.getTag();
        if(tmp instanceof Ritrovamento) {
            Ritrovamento find = (Ritrovamento) tmp;
            Intent intent = new Intent(getActivity(), EditFindActivity.class);
            intent.putExtra(Costanti.INTENT_FIND, (Parcelable) find);
            markerToBeUpdated = iMarker;
            startActivityForResult(intent, Costanti.REQUEST_UPDATE_FIND);
        }
        else {
            /*Copio le coordinate negli appunti*/
            /*Gets a handle to the clipboard service*/
            ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(getActivity())
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            /* Creates a new text clip to put on the clipboard*/
            ClipData clip = ClipData.newPlainText(marker.getTitle(), marker.getTitle());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), R.string.copied_on_clipboard, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Dopo aver invocato
     * {@link com.gsorrentino.micoapp.util.ManagePermissions#checkManageLocationPermissions(Fragment, GoogleMap)}
     * recupera la posizione attuale e la salva nel campo interno privato.
     * In caso di insuccesso, se anche il campo privato non è inizializzato,
     * imposta le coordinate di 0.0, 0.0; in caso di solo errore verranno
     * lasciate le coordinate precedentemente ottenute
     */
    private void retrieveCurrentLocation() {
        if(ManagePermissions.checkManageLocationPermissions(this, mMap)) {
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


    /*Warning soppresso perché eseguo l'operazione solo se nella callback
    * ricevo un successo nella richiesta dell'autorizzazione; in tal caso
    * sono quindi certo che avrò l'autorizzazione*/
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
         /*If request is cancelled, the result arrays are empty.*/
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


    /**
     * Aggiunge un {@link Marker} ad una {@link GoogleMap}
     *
     * @param ritrovamento Ritrovamento associato al Marker
     * @param map mappa a cui aggiungere un Ritrovamento
     */
    private void setMarker(Ritrovamento ritrovamento, GoogleMap map) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        Marker mark = map.addMarker(new MarkerOptions()
                .position(ritrovamento.getCoordinate())
                .title("" + ritrovamento.fungo)
                .snippet(ritrovamento.autore.nickname + " - " + dateFormat.format(ritrovamento.data.getTime()))
                .icon(vectorToBitmap(R.drawable.ic_marker_mushroom, 2)));
        /*Al posto di caricare un immagine Bitmap converto un vector per via di una
        * maggior definizione dell'icona finale*/
        mark.setTag(ritrovamento);
        mark.showInfoWindow();
    }


    /**
     * Modifica posizione, titolo, snippet e tag di un marker sulla base di un Ritrovamento
     *
     * @param ritrovamento da cui ricavare i dati da modificare
     * @param marker da modificare
     */
    private void updateMarker(Ritrovamento ritrovamento, Marker marker){
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        marker.setPosition(ritrovamento.getCoordinate());
        marker.setTitle("" + ritrovamento.fungo);
        marker.setSnippet(ritrovamento.autore.nickname + " - " + dateFormat.format(ritrovamento.data.getTime()));
        marker.setTag(ritrovamento);
        marker.showInfoWindow();
    }


    /**
     * Demonstrates converting a {@link Drawable} to a {@link BitmapDescriptor},
     * for use as a marker icon.
     * See <a href= "https://stackoverflow.com/questions/33548447/vectordrawable-with-googlemap-bitmapdescriptor">
     *     Suggested workaround from Google Maps team</a>
     *
     * @param id Resource that should be converted
     * @param resize How much the original size should be scaled
     */
    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, int resize) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(Objects.requireNonNull(vectorDrawable).getIntrinsicWidth() * resize,
                vectorDrawable.getIntrinsicHeight() * resize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    /*Callback di ritorno con la risposta dell'utente alla richiesta di
    * abilitare la localizzazione del dispositivo e non solo*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case (Costanti.REQUEST_CHECK_LOCALIZATION_SETTINGS):
                manageLocationUpdate();
                break;
            case(Costanti.REQUEST_UPDATE_FIND):
                if (resultCode == RESULT_OK) {
                    updateMarker(data.getParcelableExtra(Costanti.INTENT_FIND), markerToBeUpdated);
                }
                break;
        }
    }
}
