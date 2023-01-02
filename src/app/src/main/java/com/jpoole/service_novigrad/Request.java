package com.jpoole.service_novigrad;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

public class Request {
    private String id; //generated by db
    private String serviceId;
    private String customerId;
    private String branchId;
    private String name;
    private String status; // "open" when the request is open, "approved" when the request is waiting to be rated, "closed" after being rated
    private float rating;

    //@JsonProperty(access = JsonProperty.Access.READ_ONLY)
    //@JsonAdapter()
    //https://firebase.google.com/docs/reference/android/com/google/firebase/firestore/package-summary#annotations
    private List<RequestElement> requestElements;

    public static final String OPEN = "open";
    public static final String APPROVED = "approved";
    public static final String DENIED = "denied";
    public static final String CLOSED = "closed";

    public Request(){ }

    public String getService(){
        return serviceId;
    }
    public void setService(String serviceId){
        this.serviceId = serviceId;
    }

    public String getCustomer(){
        return customerId;
    }
    public void setCustomer(String customerId){
        this.customerId = customerId;
    }

    public String getBranch(){
        return branchId;
    }
    public void setBranch(String branchId){
        this.branchId = branchId;
    }

    public List<RequestElement> getRequestElements(){
        return requestElements;
    }
    public void setRequestElements(List<RequestElement> requestElements){
        this.requestElements = requestElements;
    }

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getStatus(){
        return status;
    }
    public void setStatus(String status){
        this.status = status;
    }

    public float getRating(){
        return rating;
    }
    public void setRating(float rating){
        this.rating = rating;
    }

}