package com.example.mqttgasalert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListAdapterSubscribers extends RecyclerView.Adapter<ListAdapterSubscribers.ViewHolder> {
    private List<Subscriber> subscriberList;
    private Context context;
    private OnDeleteClickListener onDeleteClickListener;


    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public ListAdapterSubscribers(Context context, List<Subscriber> subscriberList, OnDeleteClickListener onDeleteClickListener) {
        this.context = context;
        this.subscriberList = subscriberList;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Subscriber subscriber = subscriberList.get(position);

        // Populate the views in your list item layout
        holder.connectionNameTextView.setText(subscriber.getNameConnection());
        holder.seuilTextView.setText(String.valueOf(subscriber.getSeuil()));
        holder.topicTextView.setText("Topic: " + subscriber.getTopic());

        // Set click listener for the delete icon
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(position);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return subscriberList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView connectionNameTextView;
        TextView seuilTextView;
        TextView topicTextView;
        ImageView deleteIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            connectionNameTextView = itemView.findViewById(R.id.connectionName);
            seuilTextView = itemView.findViewById(R.id.tvSeuil);
            topicTextView = itemView.findViewById(R.id.tvTopicName);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
        }
    }
}
