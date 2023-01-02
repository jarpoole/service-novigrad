package com.jpoole.service_novigrad;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ServiceElement {

    // Important: Each custom class must have a public constructor that takes no arguments.
    // In addition, the class must include a public getter for each property.
    // https://firebase.google.com/docs/firestore/query-data/get-data

    protected int drawableIcon;     //Stores an integer id of a drawable for the UI icon
    protected String name;          //Name the of the information the user will eventually be asked to provide
    protected String nameHint;      //Hint for the administrator on what to put in the name field
    protected String type;          //Identifies the type of form entry: text, number, date, image
    protected int validationTypes;  //Stores an integer id of an array of validation types
    protected String validationType;//Stores the specific input validation step which will eventually be done when the user fills out a request based on this service
    protected boolean mandatory;    //Defines if the form element is mandatory or not

    public ServiceElement(){
        mandatory = true; //Default to elements being required to fill out form
    }
    public boolean isMandatory(){
        return mandatory;
    }
    public void setMandatory(boolean mandatory){ this.mandatory = mandatory; }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public int getDrawable(){
        return drawableIcon;
    }
    public void setDrawable(int drawableIcon){
        this.drawableIcon = drawableIcon;
    }
    public String nameHint(){
        return nameHint;
    }
    public void setFormElementTypeName(String nameHint){
        this.nameHint = nameHint;
    }
    public String getType(){
        return type;
    }
    public void setType(String type){
        this.type = type;
    }
    public int getValidationTypes(){
        return validationTypes;
    }
    public void setValidationTypes(int validationTypes){
        this.validationTypes = validationTypes;
    }
    public String getValidationType(){
        return validationType;
    }
    public void setValidationType(String validationType){
        this.validationType = validationType;
    }
}


//R.drawable.ic_baseline_image_24;
//"Image/Document Name";
