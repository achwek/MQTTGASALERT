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

    static class ViewHolder {
        TextView gasValueTextView;
        TextView dateTextView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_gas_value, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.gasValueTextView = convertView.findViewById(R.id.textViewGasValue);
            viewHolder.dateTextView = convertView.findViewById(R.id.textViewDate);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Gas gasValueItem = gasValues.get(position);

        viewHolder.gasValueTextView.setText(String.valueOf(gasValueItem.getValue()));
        viewHolder.dateTextView.setText(gasValueItem.getDateTime());

        return convertView;
    }
}
