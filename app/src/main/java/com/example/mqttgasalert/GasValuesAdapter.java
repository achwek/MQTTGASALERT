package com.example.mqttgasalert;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class GasValuesAdapter extends ArrayAdapter<Gas> {
    private Context context;
    private List<Gas> gasValues;


    public GasValuesAdapter(Context context, List<Gas> gasValues) {
        super(context, R.layout.list_item_gas_value, gasValues);
        this.context = context;
        this.gasValues = gasValues;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_gas_value, parent, false);
        }

        Gas gasValueItem = gasValues.get(position);

        TextView gasValueTextView = convertView.findViewById(R.id.textViewGasValue);
        TextView dateTextView = convertView.findViewById(R.id.textViewDate);

        gasValueTextView.setText(String.valueOf(gasValueItem.getValue()));
        dateTextView.setText(gasValueItem.getDateTime());

        return convertView;
    }
}
