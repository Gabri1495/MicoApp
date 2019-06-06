package com.gsorrentino.micoapp.util;

import android.view.View;
import android.view.ViewGroup;

import com.gsorrentino.micoapp.MainActivity;
import com.gsorrentino.micoapp.MapCustomFragment;
import com.gsorrentino.micoapp.R;
import com.gsorrentino.micoapp.model.Ritrovamento;

public class OnClickCustomListeners {



    public static class OnClickMapButtonListener implements View.OnClickListener {

        private MainActivity activity;
        private Ritrovamento ritrovamento;


        /**
         * Listener personalizzato che dovrà rimpiazzare un fragment con
         * un {@link MapCustomFragment} passandogli uno specifico Ritrovamento
         *
         * @param activity Necessaria per sostituire il fragment
         * @param ritrovamento Verrà visualizzato sulla mappa
         */
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



    public static class OnClickRemoveImageListener implements View.OnClickListener {

        private View rootView;


        /**
         * Listener personalizzato che dovrà rimuovere
         * dinamicamente una view
         *
         * @param rootView View che dovrà essere rimossa
         */
        public OnClickRemoveImageListener(View rootView){
            this.rootView = rootView;
        }

        @Override
        public void onClick(View v) {
            ((ViewGroup)rootView.getParent()).removeView(rootView);
        }
    }
}
