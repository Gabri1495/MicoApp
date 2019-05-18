package com.gsorrentino.micoapp.persistence;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gsorrentino.micoapp.R;
import com.gsorrentino.micoapp.model.Ricevuto;

import java.text.DateFormat;
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


    private final LayoutInflater mInflater;
    private List<Ricevuto> ricevuti = Collections.emptyList();
    private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    public RicevutoListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
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
}
