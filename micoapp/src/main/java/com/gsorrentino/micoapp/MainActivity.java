package com.gsorrentino.micoapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.gsorrentino.micoapp.util.Costanti;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HomeFragment.OnFragmentInteractionListener,
        EncyclopediaFragment.OnFragmentInteractionListener,
        MemoriesFragment.OnFragmentInteractionListener,
        StatisticsFragment.OnFragmentInteractionListener {

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment newFragment;

    SharedPreferences sharedPreferences;

    /*Variabile utilizzata per gestire il drawer in base alle impostazioni dell'utente*/
    boolean backPressed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createNotificationChannel();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        backPressed = false;

        /* Aggiungo dinamicamente il primo fragment solo se non ve ne sono già */
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if (fragmentManager.getFragments().isEmpty()) {
            newFragment = new HomeFragment();
            fragmentTransaction.replace(R.id.fragment_container, newFragment);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView){
                super.onDrawerOpened(drawerView);
                View currentFocus = getCurrentFocus();
                if(currentFocus != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView){
                super.onDrawerClosed(drawerView);
                backPressed = false;
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /* Utilizzo i valori delle impostazioni per personalizzare l'header */
        View headerView = navigationView.getHeaderView(0);
        String tmp = sharedPreferences.getString(getString(R.string.preference_name), "Nemo")
                + " " + sharedPreferences.getString(getString(R.string.preference_surname), "Nessuno");
        ((TextView) headerView.findViewById(R.id.header_nickname))
                .setText(sharedPreferences.getString(getString(R.string.preference_nickname), "Nessuno"));
        ((TextView) headerView.findViewById(R.id.header_name_surname))
                .setText(tmp);

         /* In base ai valori delle impostazioni adatto l'interfaccia */
        if(sharedPreferences.getBoolean(getString(R.string.preference_drawer_open_onStart), false)){
            drawer.openDrawer(GravityCompat.START);
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        checkCredentials();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        boolean drawerOpen = drawer.isDrawerOpen(GravityCompat.START);
        boolean doubleBack = sharedPreferences.getBoolean(getString(R.string.preference_exit_double_back), false);
        boolean drawerOnBack = sharedPreferences.getBoolean(getString(R.string.preference_drawer_on_back), false);

        if (drawerOnBack) {
            if (doubleBack) {
                if (!drawerOpen) {
                    drawer.openDrawer(GravityCompat.START);
                    backPressed = true;
                } else if (!backPressed) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    finishAffinity();
                }
            } else {
                if (!drawerOpen) {
                    drawer.openDrawer(GravityCompat.START);
                } else {
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        } else if (drawerOpen) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    /*Handle action bar item clicks here. The action bar will
      automatically handle clicks on the Home/Up button, so long
      as you specify a parent activity in AndroidManifest.xml.*/
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
             /*Apre una chat su Telegram per contattare lo sviluppatore*/
            case R.id.action_telegram:
                Intent telegramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Gabri1495"));
                if(telegramIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(telegramIntent);
                return true;

             /*Prepara una mail in parte precompilata per contattare lo sviluppatore*/
            case R.id.action_email:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"gabry1495@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject) + getString(R.string.app_name));
                emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_text));

                if(emailIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(emailIntent);
                return true;

            /*Mostra la pagina su GitHub con il codice sorgente*/
            case R.id.action_github:
                Toast.makeText(this, R.string.tmp_github, Toast.LENGTH_LONG).show();
                return true;

            /*Mostra l'activity riassuntiva dell'applicazione*/
            case R.id.action_informations:
                Intent infoIntent = new Intent(this, InfoActivity.class);
                startActivity(infoIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    /*Handle navigation view item clicks here.*/
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_home:
                newFragment = new HomeFragment();
                break;
            case R.id.menu_map:
                newFragment = new MapCustomFragment();
                break;
            case R.id.menu_encyclopedia:
                newFragment = new EncyclopediaFragment();
                break;
            case R.id.menu_archive:
                newFragment = new ArchiveFragment();
                break;
            case R.id.menu_memories:
                newFragment = new MemoriesFragment();
                break;
            case R.id.menu_statistics:
                newFragment = new StatisticsFragment();
                break;
            case R.id.menu_settings:
                newFragment = new SettingsFragment();
                break;
            case R.id.menu_receive:
                newFragment = new ReceiveFragment();
                break;
            case R.id.menu_history:
                newFragment = new HistoryFragment();
        }
        replaceFragment(newFragment, false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * Sostituisce il fragment dell'activity
     *
     * @param newFragment Fragment da sostituire
     * @param backStack Aggiungere il fragment rimpiazzato al backstack
     */
    public void replaceFragment(Fragment newFragment, boolean backStack){
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, newFragment);
        if(backStack)
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
        //TODO gestire comunicazione con fragment
    }


    /**
     * Crea i vari {@link NotificationChannel} che l'applicazione usa,
     * ma solo nel caso le API utilizzate siano 26+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Costanti.PERMISSION_CHANNEL_ID,
                    getString(R.string.channel_permissions_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getString(R.string.channel_permissions_desc));
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    /**
     * Controllo se le preference legate alle credenziali siano mai state cambiate,
     * in caso contrario un {@link AlertDialog verrà mostrato}
     */
    private void checkCredentials() {
        if(!sharedPreferences.contains(getString(R.string.preference_nickname))
        || !sharedPreferences.contains(getString(R.string.preference_name))
        || !sharedPreferences.contains(getString(R.string.preference_surname))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_credentials).setNeutralButton(R.string.ok, null)
                    .create().show();
        }
    }
}
