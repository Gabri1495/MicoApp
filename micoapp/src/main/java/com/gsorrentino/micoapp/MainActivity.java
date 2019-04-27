package com.gsorrentino.micoapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HomeFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener,
        EncyclopediaFragment.OnFragmentInteractionListener,
        ArchiveFragment.OnFragmentInteractionListener,
        MemoriesFragment.OnFragmentInteractionListener,
        StatisticsFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        SendFragment.OnFragmentInteractionListener,
        HistoryFragment.OnFragmentInteractionListener {

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Fragment newFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Aggiungo dinamicamente il primo fragment
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        newFragment = new HomeFragment();
        fragmentTransaction.add(R.id.fragment_container, newFragment);
        fragmentTransaction.commit();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            // Apre una chat su Telegram per contattare lo sviluppatore
            case R.id.action_telegram:
                Intent telegramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Gabri1495"));
                if(telegramIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(telegramIntent);
                return true;

            // Prepara una mail in parte precompilata per contattare lo sviluppatore
            case R.id.action_email:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"gabry1495@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject) + getString(R.string.app_name));
                emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_text));

                if(emailIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(emailIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        fragmentTransaction = fragmentManager.beginTransaction();

        switch (id) {
            case R.id.menu_home:
                newFragment = new HomeFragment();
                break;
            case R.id.menu_map:
                newFragment = new MapFragment();
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
            case R.id.menu_send:
                newFragment = new SendFragment();
                break;
            case R.id.menu_history:
                newFragment = new HistoryFragment();
        }
        fragmentTransaction.replace(R.id.fragment_container, newFragment);
        fragmentTransaction.commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //TODO gestire comunicazione con fragment
    }
}
