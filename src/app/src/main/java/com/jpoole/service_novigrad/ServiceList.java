package com.jpoole.service_novigrad;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class ServiceList extends ArrayAdapter<Service> {
    private Activity context;
    private boolean selectionModeEnabled = false;
    private List<Service> services;
    private List<String> selected;

    public ServiceList(android.app.Activity context, List<Service> services) {
        super(context, R.layout.layout_service_list_row, services);
        this.context = context;
        this.services = services;
    }

    //Used for list that requires the ability to select items
    public void setSelectionModeEnabled(boolean selectionModeEnabled){
        this.selectionModeEnabled = selectionModeEnabled;
    }
    public void setSelectedList(List<String> selected){
        this.selected = selected;
    }
    public List<String> getSelectedList(){
        return selected;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.layout_service_list_row, null, true);

        TextView textViewName = (TextView) listViewItem.findViewById(R.id.nameTextView);
        TextView textViewPrice = (TextView) listViewItem.findViewById(R.id.roleTextView);

        Service service = services.get(position);
        textViewName.setText(service.getName());
        textViewPrice.setText("Price: " + String.valueOf(service.getPrice()));

        if(selectionModeEnabled){
            for( String serviceId : selected){
                if(serviceId.equals(service.getId())) {
                    textViewName.setTypeface(Typeface.DEFAULT_BOLD);
                    break;
                }
                textViewName.setTypeface(Typeface.DEFAULT);
            }
        }
        return listViewItem;
    }
}
