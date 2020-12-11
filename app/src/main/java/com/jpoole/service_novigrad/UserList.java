package com.jpoole.service_novigrad;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class UserList extends ArrayAdapter<User> {
    private Activity context;
    List<User> users;

    public UserList(Activity context, List<User> users) {
        super(context, R.layout.layout_user_list, users);
        this.context = context;
        this.users = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.layout_user_list, null, true);

        TextView textViewName = (TextView) listViewItem.findViewById(R.id.nameTextView);
        TextView textViewPrice = (TextView) listViewItem.findViewById(R.id.roleTextView);

        User user = users.get(position);
        textViewName.setText(user.getFirstName()+" "+user.getLastName());
        textViewPrice.setText(user.getRoleName());
        return listViewItem;
    }
}
