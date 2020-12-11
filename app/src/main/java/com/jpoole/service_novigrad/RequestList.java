package com.jpoole.service_novigrad;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class RequestList extends ArrayAdapter<Request>{
    private Activity context;
    List<Request> requests;

    public RequestList(Activity context, List<Request> requests) {
        super(context, R.layout.layout_request_list, requests);
        this.context = context;
        this.requests = requests;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.layout_request_list, null, true);

        TextView textViewRequestName = listViewItem.findViewById(R.id.requestElementNameTextView);
        TextView textViewRequestRating = listViewItem.findViewById(R.id.requestElementStatusTextView);
        TextView textViewRequestId = listViewItem.findViewById(R.id.requestElementIdTextView);

        Request request = requests.get(position);
        textViewRequestName.setText(request.getName());
        textViewRequestRating.setText("Rating: " + request.getRating());
        textViewRequestId.setText("Request Id: " + request.getId());


        return listViewItem;
    }

}
