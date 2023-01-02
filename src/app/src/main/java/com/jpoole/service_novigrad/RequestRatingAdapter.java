package com.jpoole.service_novigrad;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.Date;
import java.util.List;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class RequestRatingAdapter extends RecyclerView.Adapter<RequestRatingAdapter.RequestRatingViewHolder>{

    private Activity activity;
    private List<Request> requests;

    FirebaseFirestore fStore;


    public RequestRatingAdapter(List<Request> requests, Activity activity){
        this.activity = activity;
        this.requests = requests;

        //Initialize Firebase
        fStore = FirebaseFirestore.getInstance();
    }
    public void setRequests(List<Request> requests){
        this.requests = requests;
    }

    public List<Request> getRequests(){
        return requests;
    }


    @NonNull
    @Override
    public RequestRatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RequestRatingViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view= inflater.inflate(R.layout.layout_request_status_row, parent, false);
        viewHolder = new RequestRatingViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RequestRatingViewHolder holder, int position) {
        Request request = requests.get(position);
        holder.serviceName.setText(request.getName());
        holder.serviceStatus.setText("Status: " + request.getStatus());
        holder.requestRatingBar.setRating(request.getRating());

        //Request approved is the only state where the request is allows to be rated
        if(request.getStatus().equals(Request.APPROVED)){
            holder.requestRatingBar.setIsIndicator(false);
            holder.ratingButton.setVisibility(View.VISIBLE);
            holder.ratingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Update the user with the new request
                    DocumentReference requestReference = fStore.collection("requests").document(request.getId());
                    requestReference.update("status", Request.CLOSED);
                    requestReference.update("rating", holder.requestRatingBar.getRating());
                }
            });
        }else{
            holder.requestRatingBar.setIsIndicator(true);
            holder.ratingButton.setVisibility(View.INVISIBLE);
        }

        if (request.getStatus().equals(Request.DENIED)) {
            holder.serviceStatus.setTextColor(Color.RED);
        }else if (request.getStatus().equals(Request.APPROVED)) {
            holder.serviceStatus.setTextColor(Color.GREEN);
        }else{
            holder.serviceStatus.setTextColor(holder.serviceName.getCurrentTextColor());
        }


    }

    @Override
    public int getItemCount() {
        return requests.size();
    }


    public class RequestRatingViewHolder extends RecyclerView.ViewHolder{
        TextView serviceName;
        TextView serviceStatus;
        RatingBar requestRatingBar;
        Button ratingButton;
        public RequestRatingViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.requestNameTextView);
            serviceStatus = itemView.findViewById(R.id.requestStatusTextView);
            requestRatingBar = itemView.findViewById(R.id.requestRatingBar);
            ratingButton = itemView.findViewById(R.id.requestRatingButton);
        }
    }


}

