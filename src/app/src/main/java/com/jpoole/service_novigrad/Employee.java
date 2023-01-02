package com.jpoole.service_novigrad;

public class Employee extends User {

    String branchId;

    public Employee(){
        roleName = "Employee";
    }

    public void setBranch(String branch){
        this.branchId = branch;
    }

    public String getBranch(){
        return this.branchId;
    }
}
