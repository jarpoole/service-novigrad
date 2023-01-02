package com.jpoole.service_novigrad;

public class Administrator extends User {
    public Administrator(){
        roleName = "Administrator";
    }

    @Override
    public boolean isDeletable() {
        return false;
    }
}
