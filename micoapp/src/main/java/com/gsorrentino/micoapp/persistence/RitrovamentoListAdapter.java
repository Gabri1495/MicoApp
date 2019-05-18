package com.gsorrentino.micoapp.persistence;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gsorrentino.micoapp.R;
import com.gsorrentino.micoapp.model.Ritrovamento;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class RitrovamentoListAdapter extends RecyclerView.Adapter<RitrovamentoListAdapter.RitrovamentoViewHolder> {


    class RitrovamentoViewHolder extends RecyclerView.ViewHolder {
        private final TextView mushroomTextView;
        private final TextView userTextView;
        private final TextView addressTextView;
        private final TextView dateTextView;
        private final ImageView mushroomImageView;

        private RitrovamentoViewHolder(View itemView) {
            super(itemView);
            mushroomTextView = itemView.findViewById(R.id.archive_mushroom_textView);
            userTextView = itemView.findViewById(R.id.archive_user_textView);
            addressTextView = itemView.findViewById(R.id.archive_address_textView);
            dateTextView = itemView.findViewById(R.id.archive_date_textView);
            mushroomImageView = itemView.findViewById(R.id.archive_mushroom_image);
        }
    }


    /*Cached copy of Ritrovamenti*/
    private List<Ritrovamento> ritrovamenti = Collections.emptyList();
    private final LayoutInflater mInflater;
    private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public RitrovamentoListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
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
            // TODO Caricare l'immagine a partire dal path
//            Bitmap immagine = current.immagine;
//            if (immagine != null) {
//                holder.mushroomImageView.setImageBitmap(current.immagine);
//            }
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
