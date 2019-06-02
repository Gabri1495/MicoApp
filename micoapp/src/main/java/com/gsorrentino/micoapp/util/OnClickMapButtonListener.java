package com.gsorrentino.micoapp.util;

import android.view.View;

import com.gsorrentino.micoapp.MainActivity;
import com.gsorrentino.micoapp.MapCustomFragment;
import com.gsorrentino.micoapp.R;
import com.gsorrentino.micoapp.model.Ritrovamento;

public class OnClickMapButtonListener implements View.OnClickListener {

    private MainActivity activity;
    private Ritrovamento ritrovamento;


    public OnClickMapButtonListener(MainActivity activity, Ritrovamento ritrovamento) {
        this.activity = activity;
        this.ritrovamento = ritrovamento;
    }

    @Override
    public void onClick(View v) {
        if(activity != null) {
            activity.replaceFragment(new MapCustomFragment(ritrovamento), false,
                    true, R.id.menu_map);
        }
    }
}
