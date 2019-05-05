package com.gsorrentino.micoapp.persistence;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gsorrentino.micoapp.R;
import com.gsorrentino.micoapp.model.Ritrovamento;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class RitrovamentoListAdapter extends RecyclerView.Adapter<RitrovamentoListAdapter.RitrovamentoViewHolder> {


    class RitrovamentoViewHolder extends RecyclerView.ViewHolder {
        private final TextView mushroomTextView;
        private final TextView userTextView;
        private final TextView addressTextView;
        private final TextView dateTextView;
        private final ImageView mushroomImageView;

        private RitrovamentoViewHolder(View itemView) {
            super(itemView);
            mushroomTextView = itemView.findViewById(R.id.mushroom_textView);
            userTextView = itemView.findViewById(R.id.user_textView);
            addressTextView = itemView.findViewById(R.id.address_textView);
            dateTextView = itemView.findViewById(R.id.date_textView);
            mushroomImageView = itemView.findViewById(R.id.mushroom_image);
        }
    }



    /*Cached copy of words*/
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
            Address ind = current.indirizzo;
            String showAddress;
            if (ind != null) {
                showAddress = Objects.toString(ind.getThoroughfare(), "") + ", "
                        + Objects.toString(ind.getLocality(), "") + ", "
                        + Objects.toString(ind.getCountryName(), "");
            } else {
                showAddress = current.latitudine + " " + current.longitudine;
            }
            holder.addressTextView.setText(showAddress);
            Calendar calendar = current.data;
            String showDate = dateFormat.format(calendar.getTime());
            holder.dateTextView.setText(showDate);
            Bitmap immagine = current.immagine;
            if (immagine != null) {
                holder.mushroomImageView.setImageBitmap(current.immagine);
            }
        }
        /*Se i dati non sono ancora pronti evitiamo di lavorare
          su dei null, ma non prendiamo ulteriori provvedimenti */
    }

    public void setRitrovamenti(List<Ritrovamento> ritrovamenti) {
        this.ritrovamenti = ritrovamenti;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(ritrovamenti != null) {
            return ritrovamenti.size();
        }
        else return 0;
    }
}
