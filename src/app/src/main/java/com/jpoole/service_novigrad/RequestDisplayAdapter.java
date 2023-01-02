package com.jpoole.service_novigrad;


import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RequestDisplayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<RequestElement> requestDisplayElements;

    private final int TEXT = 0;
    private final int IMAGE = 1;

    public RequestDisplayAdapter(List<RequestElement> requestDisplayElements){
        this.requestDisplayElements = requestDisplayElements;
    }

    public void setRequestDisplayElements(List<RequestElement> requestDisplayElements){
        this.requestDisplayElements = requestDisplayElements;
    }
    public List<RequestElement> getRequestDisplayElements(){
        return requestDisplayElements;
    }


    //Returns the view type of the item at position for the purposes of view recycling.
    @Override
    public int getItemViewType(int position) {
        RequestElement requestElement = requestDisplayElements.get(position);
        String type = requestElement.getType();
        if (type.equals("text") || type.equals("number") || type.equals("date")) {
            return TEXT;
        } else if (type.equals("image") ) {
            return IMAGE;
        }
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case TEXT:
                View textView = inflater.inflate(R.layout.layout_request_display_text_row, parent, false);
                viewHolder = new RequestDisplayAdapter.RequestDisplayTextViewHolder(textView);
                break;
            case IMAGE:
                View imageView = inflater.inflate(R.layout.layout_request_display_image_row, parent, false);
                viewHolder = new RequestDisplayAdapter.RequestDisplayImageViewHolder(imageView);
                break;
            default:
                View v = inflater.inflate(R.layout.layout_request_unknown_row, parent, false);
                viewHolder = new RequestDisplayAdapter.RequestDisplayUnknownViewHolder(v);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TEXT:
                RequestDisplayAdapter.RequestDisplayTextViewHolder requestDisplayTextView = (RequestDisplayAdapter.RequestDisplayTextViewHolder) holder;
                configureTextViewHolder(requestDisplayTextView, position);
                break;
            case IMAGE:
                RequestDisplayAdapter.RequestDisplayImageViewHolder requestDisplayImageView = (RequestDisplayAdapter.RequestDisplayImageViewHolder) holder;
                configureImageViewHolder(requestDisplayImageView, position);
                break;
            default:
                RequestElementsAdapter.RequestUnknownViewHolder requestUnknownView = (RequestElementsAdapter.RequestUnknownViewHolder) holder;
                configureUnknownViewHolder(requestUnknownView, position);
                break;
        }
    }

    private void configureUnknownViewHolder(RequestElementsAdapter.RequestUnknownViewHolder requestDisplayUnknownViewHolder, int position) {
    }
    private void configureTextViewHolder(RequestDisplayAdapter.RequestDisplayTextViewHolder requestDisplayTextViewHolder, int position) {
        RequestElement requestElement = requestDisplayElements.get(position);
        requestDisplayTextViewHolder.name.setText(requestElement.getName());

        String data = requestElement.getData();
        if(data.equals(null) || data.equals("")){
            requestDisplayTextViewHolder.text.setText("(optional - not provided)");
        }else{
            requestDisplayTextViewHolder.text.setText(data);
        }

    }
    private void configureImageViewHolder(RequestDisplayAdapter.RequestDisplayImageViewHolder requestDisplayImageViewHolder, int position) {
        RequestElement requestElement = requestDisplayElements.get(position);
        requestDisplayImageViewHolder.name.setText(requestElement.getName());

        Bitmap image = requestElement.retrieveImage();
        if(image == null){
            requestDisplayImageViewHolder.imageNotPresent.setVisibility(View.VISIBLE);
            requestDisplayImageViewHolder.image.setVisibility(View.GONE);
        }else{
            requestDisplayImageViewHolder.imageNotPresent.setVisibility(View.GONE);
            requestDisplayImageViewHolder.image.setVisibility(View.VISIBLE);
            requestDisplayImageViewHolder.image.setImageBitmap(image);
        }

    }

    @Override
    public int getItemCount() {
        return requestDisplayElements.size();
    }




    //ViewHolders
    public class RequestDisplayTextViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView text;
        public RequestDisplayTextViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.requestDisplayTextNameTextView);
            text = itemView.findViewById(R.id.requestDisplayTextValueTextView);
        }
    }
    public class RequestDisplayImageViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView image;
        TextView imageNotPresent;
        public RequestDisplayImageViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.requestDisplayImageNameTextView);
            image = itemView.findViewById(R.id.requestDisplayImageImageView);
            imageNotPresent = itemView.findViewById(R.id.requestDisplayImageNotPresentImageView);
        }
    }
    //Used to display an error if a request element is improperly formatted
    public class RequestDisplayUnknownViewHolder extends RecyclerView.ViewHolder{
        public RequestDisplayUnknownViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

