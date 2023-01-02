package com.jpoole.service_novigrad;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ServiceElementsAdapter extends RecyclerView.Adapter<ServiceElementsAdapter.ServiceElementViewHolder>{

    private List<ServiceElement> serviceElements;

    public ServiceElementsAdapter(List<ServiceElement> serviceElements){
        this.serviceElements = serviceElements;
    }

    public void setServiceElements(List<ServiceElement> serviceElements){
        this.serviceElements = serviceElements;
    }
    public List<ServiceElement> getServiceElements(){
        return serviceElements;
    }


    @NonNull
    @Override
    public ServiceElementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_service_elements_list_row, parent, false);
        ServiceElementViewHolder viewHolder = new ServiceElementsAdapter.ServiceElementViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceElementViewHolder holder, int position) {

        //Initialize the spinner
        ArrayAdapter<CharSequence> validationTypesAdapter = ArrayAdapter.createFromResource(holder.serviceElementValidation.getContext(), serviceElements.get(position).getValidationTypes(), android.R.layout.simple_spinner_item);
        validationTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.serviceElementValidation.setAdapter(validationTypesAdapter);

        //Update all form element components from the DB
        holder.serviceElementLogo.setImageResource(serviceElements.get(position).getDrawable());
        holder.serviceElementName.setHint(serviceElements.get(position).nameHint());
        holder.serviceElementName.setText(serviceElements.get(position).getName());
        holder.serviceElementMandatory.setChecked(serviceElements.get(position).isMandatory());
        //Update spinner from element from DB
        String currentlySelectedValidationType = serviceElements.get(position).getValidationType();
        for (int i = 0; i < holder.serviceElementValidation.getCount(); i++){
            if (holder.serviceElementValidation.getItemAtPosition(i).toString().equalsIgnoreCase(currentlySelectedValidationType)){
                holder.serviceElementValidation.setSelection(i);
            }
        }

        holder.serviceElementValidation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int spinnerPosition, long id) {
                String selectedValidationType = parent.getItemAtPosition(spinnerPosition).toString();
                serviceElements.get(holder.getAdapterPosition()).setValidationType(selectedValidationType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Add a listener to keep the name of the form element up to date
        holder.serviceElementName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // When focus is lost check that the text field has valid values.
                if (!hasFocus) {
                    serviceElements.get(holder.getAdapterPosition()).setName(holder.serviceElementName.getText().toString());
                }
            }
        });
        //Add a listener to keep the mandatory property of the form element up to date
        holder.serviceElementMandatory.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                serviceElements.get(holder.getAdapterPosition()).setMandatory(isChecked);
            }
        });


    }

    @Override
    public int getItemCount() {
        return serviceElements.size();
    }

    public class ServiceElementViewHolder extends RecyclerView.ViewHolder{

        ImageView serviceElementLogo;
        EditText serviceElementName;
        Switch serviceElementMandatory;
        Spinner serviceElementValidation;

        public ServiceElementViewHolder(@NonNull View itemView) {
            super(itemView);

            serviceElementLogo = itemView.findViewById(R.id.serviceElementLogoImageView);
            serviceElementName = itemView.findViewById(R.id.serviceElementNameEditText);
            serviceElementMandatory = itemView.findViewById(R.id.serviceElementMandatory);
            serviceElementValidation = itemView.findViewById(R.id.serviceElementValidation);
        }
    }

}

