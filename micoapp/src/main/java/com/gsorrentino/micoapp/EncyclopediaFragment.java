package com.gsorrentino.micoapp;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.gsorrentino.micoapp.util.Costanti;

import java.util.Objects;


public class EncyclopediaFragment extends Fragment {

    private WebView myWebView;
    private Switch saveLink;
    private SharedPreferences sharedPrefs;


    public EncyclopediaFragment() {}


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_encyclopedia, container, false);
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPrefs = Objects.requireNonNull(getActivity()).getSharedPreferences(Costanti.SHARED_PREFERENCES, 0);
        myWebView = getActivity().findViewById(R.id.encyclopedia_webview);

        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }});
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.encyclopedia, menu);

        MenuItem saveLinkMenuItem = menu.findItem(R.id.action_save_link);
        saveLink = saveLinkMenuItem.getActionView().findViewById(R.id.action_switch);
        String link = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getActivity()))
                .getString(getActivity().getString(R.string.preference_link_encyclopedia), "");
        boolean restoredSaveLink = sharedPrefs.getBoolean(Costanti.ENCYCLOPEDIA_SAVE_LINK, saveLink.isChecked());
        link = sharedPrefs.getString(Costanti.ENCYCLOPEDIA_SAVED_LINK, link);

        /*Al click dello switch simulo la selezione dello specifico MenuItem*/
        saveLink.setOnClickListener(v -> onOptionsItemSelected(saveLinkMenuItem));

        saveLink.setChecked(restoredSaveLink);
        myWebView.loadUrl(link);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.action_save_link):
                if(saveLink.isChecked())
                    Toast.makeText(getActivity(), R.string.link_will_be_saved, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), R.string.link_will_not_be_saved, Toast.LENGTH_SHORT).show();
                return true;

            case (R.id.action_copy_url):
                ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(getActivity())
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(myWebView.getUrl(), myWebView.getUrl());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity(), R.string.copied_on_clipboard, Toast.LENGTH_SHORT).show();
                return true;

            case (R.id.action_refresh):
                myWebView.reload();
                return true;
        }
        return false;
    }


    @Override
    public void onPause(){
        super.onPause();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(Costanti.ENCYCLOPEDIA_SAVE_LINK, saveLink.isChecked());
        if(saveLink.isChecked()){
            editor.putString(Costanti.ENCYCLOPEDIA_SAVED_LINK, myWebView.getUrl());
        }
        else{
            editor.remove(Costanti.ENCYCLOPEDIA_SAVED_LINK);
        }
        editor.apply();
    }
}
