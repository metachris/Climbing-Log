package com.tapdom.climbinglog;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LogListAdapter extends ArrayAdapter<LogEntry> {
    int resource;
    
    // Initialize adapter
    public LogListAdapter(Context context, int resource, List<LogEntry> items) {
        super(context, resource, items);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout itemView;

        // Get the current item
        LogEntry item = getItem(position);
 
        // Get the view for this position
        if(convertView == null) {
            // Create it once
            itemView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            vi = (LayoutInflater)getContext().getSystemService(inflater);
            vi.inflate(resource, itemView, true);
            
        } else {
            // And reuse previously created ones
            itemView = (LinearLayout) convertView;
        }

        //Get the views from the more_games_item.xml file
        TextView tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
        TextView tvDate = (TextView) itemView.findViewById(R.id.tv_date);
        
        // Set the item contents
        tvTitle.setText(item.title);
        tvDate.setText(new java.sql.Date(item.date_start).toGMTString());
        
        // Return the compiled MoreGamesItemView
        return itemView;
    }    
}