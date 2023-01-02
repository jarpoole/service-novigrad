package com.jpoole.service_novigrad;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class BranchList extends ArrayAdapter<Branch>{
    private Activity context;
    List<Branch> branches;

    public BranchList(android.app.Activity context, List<Branch> branches) {
        super(context, R.layout.layout_branch_list, branches);
        this.context = context;
        this.branches = branches;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.layout_branch_list, null, true);

        TextView textViewAddress = listViewItem.findViewById(R.id.addressTextView);
        TextView textViewPhone = listViewItem.findViewById(R.id.phoneTextView);
        TextView textViewWeekHours = listViewItem.findViewById(R.id.hoursTextView);
        TextView textViewWkndHours = listViewItem.findViewById(R.id.hoursTextView2);


        Branch branch = branches.get(position);
        textViewAddress.setText(branch.getAddress());
        textViewPhone.setText(branch.getPhone());
        textViewWeekHours.setText("Mon-Fri: " + branch.getSpecificHours(0)+"-"+branch.getSpecificHours(1));
        textViewWkndHours.setText("Sat-Sun: " + branch.getSpecificHours(2)+"-"+branch.getSpecificHours(3));

        return listViewItem;
    }

}
