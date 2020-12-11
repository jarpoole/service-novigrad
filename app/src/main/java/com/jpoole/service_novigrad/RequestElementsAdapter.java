package com.jpoole.service_novigrad;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.Date;
import java.util.List;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class RequestElementsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private int lastImageSelectionPosition;
    private Uri lastImageUri;
    private Activity activity;

    private List<RequestElement> requestElements;

    private final int TEXT = 0;
    private final int IMAGE = 1;
    private final int NUMBER = 2;
    private final int DATE = 3;

    public RequestElementsAdapter(List<RequestElement> requestElements, Activity activity){
        this.activity = activity;
        this.requestElements = requestElements;

        //Disable Strict mode checking to allow for file URI sharing within Intents
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }
    public void setRequestElements(List<RequestElement> requestElements){
        this.requestElements = requestElements;
    }

    public int getLastImageSelectionPosition(){
        return lastImageSelectionPosition;
    }
    public Uri getLastImageUri(){
        return lastImageUri;
    }

    public List<RequestElement> getRequestElements(){
        return requestElements;
    }


    //Returns the view type of the item at position for the purposes of view recycling.
    @Override
    public int getItemViewType(int position) {
        RequestElement requestElement = requestElements.get(position);
        if (requestElement.getType().equals("text") ) {
            return TEXT;
        } else if (requestElement.getType().equals("image") ) {
            return IMAGE;
        } else if (requestElement.getType().equals("number") ) {
            return NUMBER;
        } else if (requestElement.getType().equals("date") ) {
            return DATE;
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
                View textView = inflater.inflate(R.layout.layout_request_text_row, parent, false);
                viewHolder = new RequestTextViewHolder(textView);
                break;
            case IMAGE:
                View imageView = inflater.inflate(R.layout.layout_request_image_row, parent, false);
                viewHolder = new RequestImageViewHolder(imageView);
                break;
            case NUMBER:
                View numberView = inflater.inflate(R.layout.layout_request_number_row, parent, false);
                viewHolder = new RequestNumberViewHolder(numberView);
                break;
            case DATE:
                View dateView = inflater.inflate(R.layout.layout_request_date_row, parent, false);
                viewHolder = new RequestDateViewHolder(dateView);
                break;
            default:
                View v = inflater.inflate(R.layout.layout_request_unknown_row, parent, false);
                viewHolder = new RequestUnknownViewHolder(v);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TEXT:
                RequestTextViewHolder requestTextView = (RequestTextViewHolder) holder;
                configureTextViewHolder(requestTextView, position);
                break;
            case IMAGE:
                RequestImageViewHolder requestImageView = (RequestImageViewHolder) holder;
                configureImageViewHolder(requestImageView, position);
                break;
            case NUMBER:
                RequestNumberViewHolder requestNumberView = (RequestNumberViewHolder) holder;
                configureNumberViewHolder(requestNumberView, position);
                break;
            case DATE:
                RequestDateViewHolder requestDateView = (RequestDateViewHolder) holder;
                configureDateViewHolder(requestDateView, position);
                break;
            default:
                RequestUnknownViewHolder requestUnknownView = (RequestUnknownViewHolder) holder;
                configureUnknownViewHolder(requestUnknownView, position);
                break;
        }
    }


    private void configureUnknownViewHolder(RequestUnknownViewHolder requestUnknownViewHolder, int position) {

    }
    private void configureTextViewHolder(RequestTextViewHolder requestTextViewHolder, int position) {
        RequestElement requestElement = requestElements.get(position);
        if (requestElement != null) {
            requestTextViewHolder.mandatory.setVisibility(requestElement.isMandatory() ? View.VISIBLE : View.INVISIBLE);
            requestTextViewHolder.name.setText(requestElement.getName());

            //Load error if there is one
            if( requestElement.getError() != null && !requestElement.getError().equals("") ){
                requestTextViewHolder.text.setError(requestElement.getError());
            }else{
                requestTextViewHolder.text.setError(null);
            }

            //Add a listener to keep the data entered into the text element up to date
            requestTextViewHolder.text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // When focus is lost check that the text field has valid values.
                    if (!hasFocus) {
                        requestElements.get(position).setData(requestTextViewHolder.text.getText().toString());
                    }

                }
            });
        }
    }
    private void configureImageViewHolder(RequestImageViewHolder requestImageViewHolder, int position) {
        RequestElement requestElement = requestElements.get(position);
        if (requestElement != null) {
            requestImageViewHolder.mandatory.setVisibility(requestElement.isMandatory() ? View.VISIBLE : View.INVISIBLE);
            requestImageViewHolder.name.setText(requestElement.getName());

            if(requestElement.getData() == null || requestElement.getData().equals("") ){
                requestImageViewHolder.imageTakeButton.setText("Camera");
                requestImageViewHolder.imageGalleryButton.setVisibility(View.VISIBLE);
            }else{
                requestImageViewHolder.imageTakeButton.setText("Remove Image");
                requestImageViewHolder.imageGalleryButton.setVisibility(View.GONE);
            }

            requestImageViewHolder.imageTakeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //grab focus
                    requestImageViewHolder.imageTakeButton.setFocusableInTouchMode(true);
                    requestImageViewHolder.imageTakeButton.requestFocus();
                    requestImageViewHolder.imageTakeButton.setFocusableInTouchMode(false);

                    if( requestElement.getData() == null || requestElement.getData().equals("") ){

                        //https://android.stackexchange.com/questions/47924/where-android-apps-store-data
                        String imagePath;
                        //File file = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DCIM),"image" + new Date().getTime() + ".png");
                        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/","image" + new Date().getTime() + ".png");
                        Uri imgUri = Uri.fromFile(file);
                        //imagePath = file.getAbsolutePath();
                        lastImageSelectionPosition = position;
                        lastImageUri = imgUri;
                        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivityForResult(activity, intent, CreateRequest.TAKE_PICTURE_REQUEST_CODE,null);

                    }else{
                        requestElement.setData("");
                        notifyItemChanged(position);
                    }
                }
            });

            requestImageViewHolder.imageGalleryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //grab focus
                    requestImageViewHolder.imageGalleryButton.setFocusableInTouchMode(true);
                    requestImageViewHolder.imageGalleryButton.requestFocus();
                    requestImageViewHolder.imageGalleryButton.setFocusableInTouchMode(false);

                    lastImageSelectionPosition = position;
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    startActivityForResult(activity, intent, CreateRequest.PICK_PICTURE_REQUEST_CODE, null);
                }
            });

        }
    }
    private void configureNumberViewHolder(RequestNumberViewHolder requestNumberViewHolder, int position) {
        RequestElement requestElement = requestElements.get(position);
        if (requestElement != null) {
            requestNumberViewHolder.mandatory.setVisibility(requestElement.isMandatory() ? View.VISIBLE : View.INVISIBLE);
            requestNumberViewHolder.name.setText(requestElement.getName());

            //Load error if there is one
            if( requestElement.getError() != null && !requestElement.getError().equals("") ){
                requestNumberViewHolder.number.setError(requestElement.getError());
            }

            //Add a listener to keep the data entered into the text element up to date
            requestNumberViewHolder.number.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // When focus is lost check that the text field has valid values.
                    if (!hasFocus) {
                        requestElements.get(position).setData(requestNumberViewHolder.number.getText().toString());
                    }
                }
            });
        }
    }
    private void configureDateViewHolder(RequestDateViewHolder requestDateViewHolder, int position) {
        RequestElement requestElement = requestElements.get(position);
        if (requestElement != null) {
            requestDateViewHolder.mandatory.setVisibility(requestElement.isMandatory() ? View.VISIBLE : View.INVISIBLE);
            requestDateViewHolder.name.setText(requestElement.getName());

            //Load error if there is one
            if( requestElement.getError() != null && !requestElement.getError().equals("") ){
                requestDateViewHolder.date.setError(requestElement.getError());
                requestDateViewHolder.date.requestFocus();
            }else{
                requestDateViewHolder.date.setError(null);
            }

            requestDateViewHolder.select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //grab focus
                    requestDateViewHolder.select.setFocusableInTouchMode(true);
                    requestDateViewHolder.select.requestFocus();
                    requestDateViewHolder.select.setFocusableInTouchMode(false);

                    Calendar cal = Calendar.getInstance();
                    int year, month, day;
                    String date = requestDateViewHolder.date.getText().toString();
                    if(date.equals(null) || date.equals("")){
                        year = cal.get(Calendar.YEAR);
                        month = cal.get(Calendar.MONTH);
                        day = cal.get(Calendar.DAY_OF_MONTH);
                    }else{
                        String[] numbers = date.split("/");
                        month = Integer.parseInt(numbers[0]) - 1; //month ranges from 0-11 for some reason
                        day = Integer.parseInt(numbers[1]);
                        year = Integer.parseInt(numbers[2]);
                    }

                    DatePickerDialog dateDialog = new DatePickerDialog(activity, android.R.style.Theme_Holo_Light_Dialog_MinWidth, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            //month ranges from 0-11 for some reason
                            month = month + 1;
                            String date = month + "/" + dayOfMonth + "/" + year;
                            requestDateViewHolder.date.setText(date);
                            requestElements.get(position).setData(requestDateViewHolder.date.getText().toString());
                        }
                    }, year, month, day);
                    dateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    //dateDialog.updateDate(year, month, day);
                    dateDialog.show();

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return requestElements.size();
    }




    //ViewHolders
    public class RequestTextViewHolder extends RecyclerView.ViewHolder{
        TextView mandatory;
        TextView name;
        EditText text;
        public RequestTextViewHolder(@NonNull View itemView) {
            super(itemView);
            mandatory = itemView.findViewById(R.id.mandatoryTextTextView);
            name = itemView.findViewById(R.id.elementNameTextTextView);
            text = itemView.findViewById(R.id.elementTextEditText);
        }
    }
    public class RequestImageViewHolder extends RecyclerView.ViewHolder{
        TextView mandatory;
        TextView name;
        Button imageTakeButton;
        Button imageGalleryButton;
        public RequestImageViewHolder(@NonNull View itemView) {
            super(itemView);
            mandatory = itemView.findViewById(R.id.mandatoryImageTextView);
            name = itemView.findViewById(R.id.elementNameImageTextView);
            imageTakeButton = itemView.findViewById(R.id.imageTakeButton);
            imageGalleryButton = itemView.findViewById(R.id.imageGalleryButton);
        }
    }
    public class RequestNumberViewHolder extends RecyclerView.ViewHolder{
        TextView mandatory;
        TextView name;
        EditText number;
        public RequestNumberViewHolder(@NonNull View itemView) {
            super(itemView);
            mandatory = itemView.findViewById(R.id.mandatoryNumberTextView);
            name = itemView.findViewById(R.id.elementNameNumberTextView);
            number = itemView.findViewById(R.id.elementNumberEditText);
        }
    }
    public class RequestDateViewHolder extends RecyclerView.ViewHolder{
        TextView mandatory;
        TextView name;
        TextView date;
        Button select;
        public RequestDateViewHolder(@NonNull View itemView) {
            super(itemView);
            mandatory = itemView.findViewById(R.id.mandatoryDateTextView);
            name = itemView.findViewById(R.id.elementNameDateTextView);
            date = itemView.findViewById(R.id.elementDateTextView);
            select = itemView.findViewById(R.id.elementDateSelectButton);
        }

    }
    //Used to display an error if a request element is improperly formatted
    public class RequestUnknownViewHolder extends RecyclerView.ViewHolder{
        public RequestUnknownViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


}

