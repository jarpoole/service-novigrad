package com.jpoole.service_novigrad;

import android.content.Intent;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static androidx.core.content.ContextCompat.startActivity;

public class Customer extends User {
    private List<String> requests;

    public Customer(){
        roleName = "Customer";
        requests = new ArrayList<>();
    }

    public List<String> getRequests(){
        return requests;
    }
    public void setRequests(List<String> requests){ this.requests = requests; }
}
