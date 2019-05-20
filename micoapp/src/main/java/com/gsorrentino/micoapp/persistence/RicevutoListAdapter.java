package com.gsorrentino.micoapp.persistence;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gsorrentino.micoapp.EditFindActivity;
import com.gsorrentino.micoapp.R;
import com.gsorrentino.micoapp.model.Ricevuto;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.util.AsyncTasks;
import com.gsorrentino.micoapp.util.Costanti;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class RicevutoListAdapter extends RecyclerView.Adapter<RicevutoListAdapter.RicevutoViewHolder> {


    class RicevutoViewHolder extends RecyclerView.ViewHolder {
        private final TextView mushroomTextView;
        private final TextView userTextView;
        private final TextView dateTextView;
        private final TextView dateReceivedTextView;

        private RicevutoViewHolder(View itemView) {
            super(itemView);
            mushroomTextView = itemView.findViewById(R.id.history_mushroom_textView);
            userTextView = itemView.findViewById(R.id.history_user_textView);
            dateTextView = itemView.findViewById(R.id.history_date_textView);
            dateReceivedTextView = itemView.findViewById(R.id.history_received_textView);
        }
    }


    private Context context;
    private final LayoutInflater mInflater;
    private List<Ricevuto> ricevuti = Collections.emptyList();
    private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    /*Gestione selezione elementi*/
    private ActionMode actionMode;  /*Utilizzato per evitare una doppia creazione in caso di doppio LongClick*/
    private boolean multiSelect = false;
    private ArrayList<Integer> selectedItemsIndex = new ArrayList<>();

    public RicevutoListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    @NonNull
    public RicevutoListAdapter.RicevutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.history_recyclerview_item, parent, false);
        return new RicevutoListAdapter.RicevutoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RicevutoListAdapter.RicevutoViewHolder holder, int position) {
        if (ricevuti != null) {
            Ricevuto current = ricevuti.get(position);

            /*Resetto lo sfondo dei vari elementi*/
            if (!selectedItemsIndex.contains(position)) {
                holder.itemView.setBackgroundColor(Color.WHITE);
            } else {
                holder.itemView.setBackgroundColor(Color.LTGRAY);
            }

            holder.itemView.setOnLongClickListener(view -> {
                if(actionMode != null)
                    return false;
                ((AppCompatActivity)view.getContext()).startSupportActionMode(new RicevutoListAdapter.ActionModeCallbacks());
                selectItem(holder.itemView, position);
                return true;
            });
            holder.itemView.setOnClickListener(v -> {
                if(multiSelect)
                    selectItem(holder.itemView, position);
            });

            holder.mushroomTextView.setText(current.fungo);
            holder.userTextView.setText(current.autore.nickname);
            /*Controllo la versione di Android, essendo i Tooltips disponibili solo dall'SDk 26*/
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                holder.userTextView.setTooltipText(current.autore.getNomeCompleto());
            }
            else {
                TooltipCompat.setTooltipText(holder.userTextView, current.autore.getNomeCompleto());
            }
            Calendar calendar = current.data;
            String showDate = dateFormat.format(calendar.getTime());
            holder.dateTextView.setText(showDate);
            calendar = current.dataRicezione;
            showDate = dateFormat.format(calendar.getTime());
            holder.dateReceivedTextView.setText(showDate);
        }
        /*Se i dati non sono ancora pronti evitiamo di lavorare
          su dei null, ma non prendiamo ulteriori provvedimenti */
    }

    @Override
    public int getItemCount() {
        if(ricevuti != null) {
            return ricevuti.size();
        }
        else return 0;
    }

    public void setRicevuti(List<Ricevuto> ricevuti) {
        this.ricevuti = ricevuti;
        notifyDataSetChanged();
    }

    /**
     * Seleziona o deseleziona un elemento della RecyclerView,
     * terminando l'{@link ActionMode} nel caso non ne restino selezionati
     *
     * @param layout Il Layout contenente l'item della RecyclerView
     * @param position Posizione dell'item nella RecyclerView
     */
    private void selectItem(View layout, Integer position) {
        if (selectedItemsIndex.contains(position)) {
            selectedItemsIndex.remove(position);
            layout.setBackgroundColor(Color.WHITE);
        } else {
            selectedItemsIndex.add(position);
            layout.setBackgroundColor(Color.LTGRAY);
        }
        int selected = selectedItemsIndex.size();
        actionMode.setTitle(String.valueOf(selected));
        if(selected == 0)
            actionMode.finish();
    }



    /**
     * Custom {@link ActionMode.Callback} to manage a multiple selectable
     * {@link RecyclerView} of {@link Ricevuto}
     */
    private class ActionModeCallbacks implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            multiSelect = true;
            mode.getMenuInflater().inflate(R.menu.archive, menu);
            menu.removeItem(R.id.menu_context_edit);
            menu.removeItem(R.id.menu_context_send);
            mode.setTitle(String.valueOf(selectedItemsIndex.size()));
            mode.setSubtitle(R.string.action_mode_selected);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            for (Integer intItem : selectedItemsIndex) {
                    new AsyncTasks.ManageReceivedAsync(context, ricevuti.get(intItem)).execute(Costanti.DELETE);
                }
                mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            multiSelect = false;
            selectedItemsIndex.clear();
            actionMode = null;
            notifyDataSetChanged();
        }
    }
}
