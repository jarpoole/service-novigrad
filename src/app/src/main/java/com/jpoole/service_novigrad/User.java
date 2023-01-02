package com.jpoole.service_novigrad;

public class User {

    protected String id;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String roleName;

    public User(){
        this.id = "";
    }

    public void setEmail(String email){
        this.email = email;
    }
    public void setName(String firstName, String lastName){
        this.firstName = firstName;
        this.lastName = lastName;

    }
    public void setId(String id){
        this.id = id;
    }
    public String getEmail(){
        return this.email;
    }
    public String getFirstName(){
        return this.firstName;
    }
    public String getLastName(){
        return this.lastName;
    }
    public String getRoleName(){ return this.roleName; }
    public String getId(){ return this.id; }


    public void delete(){
        //hook method will be updated later when User classes have been implemented
    }

    public boolean isDeletable(){
        return true;
    }
}
