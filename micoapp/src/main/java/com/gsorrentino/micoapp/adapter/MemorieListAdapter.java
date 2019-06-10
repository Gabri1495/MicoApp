package com.gsorrentino.micoapp.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.gsorrentino.micoapp.EditFindActivity;
import com.gsorrentino.micoapp.MainActivity;
import com.gsorrentino.micoapp.MemoriesFragment;
import com.gsorrentino.micoapp.R;
import com.gsorrentino.micoapp.model.Ritrovamento;
import com.gsorrentino.micoapp.util.Costanti;
import com.gsorrentino.micoapp.util.Metodi;
import com.gsorrentino.micoapp.util.OnClickCustomListeners;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class MemorieListAdapter extends RecyclerView.Adapter<MemorieListAdapter.MemorieViewHolder> {



    class MemorieViewHolder extends RecyclerView.ViewHolder {
        private final TextView mushroomNumberTextView;
        private final TextView mushroomTextView;
        private final TextView userTextView;
        private final TextView dateTextView;
        private final ConstraintLayout subItem;
        private final TextView addressTextView;
        private final TextView noteTextView;
        private final Button showMapButton;
        private final Button editButton;
        private final Button deleteButton;
        private final ImageView mushroomImageView;

        private MemorieViewHolder(View itemView) {
            super(itemView);
            mushroomNumberTextView = itemView.findViewById(R.id.memories_mushroomNumber_textView);
            mushroomTextView = itemView.findViewById(R.id.memories_mushroom_textView);
            userTextView = itemView.findViewById(R.id.memories_user_textView);
            dateTextView = itemView.findViewById(R.id.memories_date_textView);
            subItem = itemView.findViewById(R.id.memories_constraintSubLayout);
            addressTextView = itemView.findViewById(R.id.memories_address_textView);
            noteTextView = itemView.findViewById(R.id.memories_note_textView);
            showMapButton = itemView.findViewById(R.id.memories_showMap_button);
            editButton = itemView.findViewById(R.id.memories_edit_button);
            deleteButton = itemView.findViewById(R.id.memories_delete_button);
            mushroomImageView = itemView.findViewById(R.id.memories_mushroom_image);
        }
    }



    private List<Ritrovamento> ritrovamenti = Collections.emptyList();
    private final Activity activity;
    private final MemoriesFragment memoriesFragment;
    private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public MemorieListAdapter(MemoriesFragment memoriesFragment){
        this.activity = memoriesFragment.getActivity();
        this.memoriesFragment = memoriesFragment;
    }

    @NonNull
    @Override
    public MemorieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(activity)
                .inflate(R.layout.memories_recyclerview_item, parent, false);
        return new MemorieViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MemorieViewHolder holder, int position) {
        if (ritrovamenti != null) {
            Ritrovamento current = ritrovamenti.get(position);

            /*Gestisco la visibilità della parte da ampliare dell'item*/
            holder.subItem.setVisibility(current.expanded ? View.VISIBLE : View.GONE);
            holder.itemView.setOnClickListener(v -> {
                current.expanded = !current.expanded;
                notifyItemChanged(position);
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
                    OnClickMapButtonListener((MainActivity) activity, current));

            holder.editButton.setOnClickListener(v -> {
                memoriesFragment.findsMustBeUpdated();
                Intent intent = new Intent(activity, EditFindActivity.class);
                intent.putExtra(Costanti.INTENT_FIND, (Parcelable) current);
                activity.startActivity(intent);
            });

            holder.deleteButton.setOnClickListener(v -> memoriesFragment.removeMemory(current));

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
}
