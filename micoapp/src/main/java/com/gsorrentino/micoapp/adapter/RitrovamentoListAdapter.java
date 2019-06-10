package com.gsorrentino.micoapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.TooltipCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.gsorrentino.micoapp.EditFindActivity;
import com.gsorrentino.micoapp.MainActivity;
import com.gsorrentino.micoapp.R;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.util.AsyncTasks;
import com.gsorrentino.micoapp.util.Costanti;
import com.gsorrentino.micoapp.util.ImportExportRitrovamentoManager;
import com.gsorrentino.micoapp.util.Metodi;
import com.gsorrentino.micoapp.util.OnClickCustomListeners;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class RitrovamentoListAdapter extends RecyclerView.Adapter<RitrovamentoListAdapter.RitrovamentoViewHolder> {


    class RitrovamentoViewHolder extends RecyclerView.ViewHolder {
        private final TextView mushroomNumberTextView;
        private final TextView mushroomTextView;
        private final TextView userTextView;
        private final TextView dateTextView;
        private final ConstraintLayout subItem;
        private final TextView addressTextView;
        private final TextView noteTextView;
        private final Button showMapButton;
        private final ImageView mushroomImageView;

        private RitrovamentoViewHolder(View itemView) {
            super(itemView);
            mushroomNumberTextView = itemView.findViewById(R.id.archive_mushroomNumber_textView);
            mushroomTextView = itemView.findViewById(R.id.archive_mushroom_textView);
            userTextView = itemView.findViewById(R.id.archive_user_textView);
            dateTextView = itemView.findViewById(R.id.archive_date_textView);
            subItem = itemView.findViewById(R.id.archive_constraintSubLayout);
            addressTextView = itemView.findViewById(R.id.archive_address_textView);
            noteTextView = itemView.findViewById(R.id.archive_note_textView);
            showMapButton = itemView.findViewById(R.id.archive_showMap_button);
            mushroomImageView = itemView.findViewById(R.id.archive_mushroom_image);
        }
    }


    private Activity activity;
    /*Cached copy of Ritrovamenti*/
    private List<Ritrovamento> ritrovamenti = Collections.emptyList();
    private final LayoutInflater mInflater;
    private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    /*Gestione selezione elementi*/
    private ActionMode actionMode;  /*Utilizzato per evitare una doppia creazione in caso di doppio LongClick*/
    private boolean multiSelect = false;
    private ArrayList<Integer> selectedItemsIndex = new ArrayList<>();

    public RitrovamentoListAdapter(Activity activity) {
        mInflater = LayoutInflater.from(activity);
        this.activity = activity;
    }

    @Override
    @NonNull
    public RitrovamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.archive_recyclerview_item, parent, false);
        return new RitrovamentoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RitrovamentoViewHolder holder, int position) {
        if (ritrovamenti != null) {
            Ritrovamento current = ritrovamenti.get(position);

            /*Resetto lo sfondo dei vari elementi*/
            if (!selectedItemsIndex.contains(position)) {
                holder.itemView.setBackgroundColor(Color.WHITE);
            } else {
                holder.itemView.setBackgroundColor(Color.LTGRAY);
            }

            holder.itemView.setOnLongClickListener(view -> {
                if(actionMode != null)
                    return false;
                /*Chiudo la tastiera se aperta*/
                View currentFocus = activity.getCurrentFocus();
                if(currentFocus != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) activity
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                }
                ((AppCompatActivity)view.getContext()).startSupportActionMode(new ActionModeCallbacks());
                selectItem(holder.itemView, position);
                return true;
            });
            /*Gestisco la visibilità della parte da ampliare dell'item*/
            holder.subItem.setVisibility(current.expanded ? View.VISIBLE : View.GONE);
            holder.itemView.setOnClickListener(v -> {
                if(multiSelect){
                    selectItem(holder.itemView, position);
                }
                else {
                    current.expanded = !current.expanded;
                    notifyItemChanged(position);
                }
            });

            holder.mushroomNumberTextView.setText(String.valueOf(current.quantita));

            holder.mushroomTextView.setText(current.fungo);

            holder.userTextView.setText(current.autore.nickname);
            /*Controllo la versione di Android, essendo i Tooltips disponibili solo dall'SDk 26*/
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                holder.userTextView.setTooltipText(current.autore.getNomeCompleto());
            }
            else {
                TooltipCompat.setTooltipText(holder.userTextView, current.autore.getNomeCompleto());
            }

            String ind = current.indirizzo;
            String showAddress;
            if (ind != null) {
                showAddress = ind;
            } else {
                showAddress = current.latitudine + " " + current.longitudine;
            }
            holder.addressTextView.setText(showAddress);

            Calendar calendar = current.data;
            String showDate = dateFormat.format(calendar.getTime());
            holder.dateTextView.setText(showDate);

            if(current.note.isEmpty()) {
                holder.noteTextView.setVisibility(View.GONE);
            }
            else {
                holder.noteTextView.setVisibility(View.VISIBLE);
                holder.noteTextView.setText(current.note);
            }

            holder.showMapButton.setOnClickListener(new OnClickCustomListeners.
                    OnClickMapButtonListener((MainActivity) activity, this, current));

            List<String> paths = current.getPathsImmagine();
            if(paths.size() > 0){
                String path = paths.get(0);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int width = displayMetrics.widthPixels;
                Bitmap bitmap = Metodi.loadBitMapResized(path, width / 4, false);
                if(bitmap != null)
                    holder.mushroomImageView.setImageBitmap(bitmap);
            }

        }
        /*Se i dati non sono ancora pronti evitiamo di lavorare
          su dei null, ma non prendiamo ulteriori provvedimenti */
    }

    @Override
    public int getItemCount() {
        if(ritrovamenti != null) {
            return ritrovamenti.size();
        }
        else return 0;
    }

    public void setRitrovamenti(List<Ritrovamento> ritrovamenti) {
        this.ritrovamenti = ritrovamenti;
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
        if(selected == 0) {
            actionMode.finish();
            return;
        }
        /*Disabilito la possibilità di modificare nel caso la selezione sia multipla*/
        MenuItem editItem = actionMode.getMenu().findItem(R.id.menu_context_edit);
        editItem.setVisible(selected == 1);
        editItem.setEnabled(selected == 1);
    }

    /**
     * Termina {@link ActionMode}
     */
    public void terminaActionMode(){
        if(actionMode != null)
            actionMode.finish();
    }



    /**
     * Custom {@link ActionMode.Callback} to manage a multiple selectable
     * {@link RecyclerView} of {@link Ritrovamento}
     */
    private class ActionModeCallbacks implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            multiSelect = true;
            mode.getMenuInflater().inflate(R.menu.selection, menu);
            mode.setTitle(String.valueOf(selectedItemsIndex.size()));
            mode.setSubtitle(R.string.action_mode_selected);
            /*Inibisco la possibilità di aprire il drawer con lo swipe*/
            ((DrawerLayout)activity.findViewById(R.id.drawer_layout))
                    .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_context_edit:
                    Intent intent = new Intent(activity, EditFindActivity.class);
                    intent.putExtra(Costanti.INTENT_FIND, (Parcelable) ritrovamenti.get(selectedItemsIndex.get(0)));
                    activity.startActivity(intent);
                    break;
                case R.id.menu_context_send:
                    ImportExportRitrovamentoManager saver = new ImportExportRitrovamentoManager();
                    for (Integer intItem : selectedItemsIndex) {
                        saver.exportRitrovamento(activity, ritrovamenti.get(intItem));
                    }
                    break;
                case R.id.menu_context_delete:
                    for (Integer intItem : selectedItemsIndex) {
                        new AsyncTasks.ManageFindAsync(activity, ritrovamenti.get(intItem)).execute(Costanti.DELETE);
                    }
                    break;
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            multiSelect = false;
            selectedItemsIndex.clear();
            actionMode = null;
            ((DrawerLayout)activity.findViewById(R.id.drawer_layout))
                    .setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            notifyDataSetChanged();
        }
    }
}
