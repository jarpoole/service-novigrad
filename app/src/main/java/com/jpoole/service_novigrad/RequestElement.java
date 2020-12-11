package com.jpoole.service_novigrad;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

//import android.icu.util.Calendar;
import java.util.Calendar;


import java.io.ByteArrayOutputStream;

public class RequestElement {
    private String name;
    private Boolean mandatory;
    private String type;
    private String validationType;
    private String error;
    private String data;

    public static final String TEXT = "text";
    public static final String IMAGE = "image";
    public static final String NUMBER = "number";
    public static final String DATE = "date";

    public RequestElement(){
        error = "";
    }

    public Boolean isMandatory(){
        return mandatory;
    }
    public void setMandatory(Boolean mandatory){
        this.mandatory = mandatory;
    }

    public void setValidationType(String validationType){
        this.validationType = validationType;
    }
    public String getValidationType(){
        return validationType;
    }

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

    public void setType(String type){
        this.type = type;
    }
    public String getType(){
        return type;
    }

    public void setError(String error){
        this.error = error;
    }
    public String getError(){
        return error;
    }

    public void setData(String data){
        this.data = data;
    }
    public String getData(){
        return data;
    }

    public boolean validate(){
        boolean result = true;
        //reset previous error if there was one
        error = "";

        //Validate field if form element is mandatory or if form element isn't mandatory but data was entered
        if( mandatory || (!mandatory && data != null && !data.equals("")  ) ){
            //General check for empty field
            if( data == null || data.equals("") ){
                error = "Field cannot be empty";
                result = false;
            }
            else if(validationType.equals("raw")) {
                //Do nothing, only requirement is that input is non empty
            }

            //Validation options for text input
            else if(type.equals(TEXT)){
                if(validationType.equals("email")) {
                    if(!data.matches(ValidationManager.getEmailRegex())){
                        error = ValidationManager.getEmailRegexError();
                        result = false;
                    }
                }else if(validationType.equals("name")) {
                    if(!data.matches(ValidationManager.getNameRegex())){
                        error = ValidationManager.getNameRegexError();
                        result = false;
                    }
                }else if(validationType.equals("password")) {
                    if(!data.matches(ValidationManager.getPasswordRegex())){
                        error = ValidationManager.getPasswordRegexError();
                        result = false;
                    }
                }else if(validationType.equals("postalcode")) {
                    if(!data.matches(ValidationManager.getPostalCodeRegex())){
                        error = ValidationManager.getPostalCodeRegexError();
                        result = false;
                    }
                }else if(validationType.equals("address")) {
                    //Address validation not specified as the service area of service-novigrad is not specified
                    //Only check for invalid special characters
                    if(!data.matches(ValidationManager.getAddressRegex())){
                        error = ValidationManager.getAddressRegexError();
                        result = false;
                    }
                }
            }

            //Validation options for date input
            else if(type.equals(DATE)){
                int selectedDay = 0;
                int selectedMonth = 0;
                int selectedYear = 0;
                boolean parseError = false;

                try{
                    String[] numbers = data.split("/");
                    selectedMonth = Integer.parseInt(numbers[0]);
                    selectedDay = Integer.parseInt(numbers[1]);
                    selectedYear = Integer.parseInt(numbers[2]);
                }catch(Exception e){
                    parseError = true;
                }
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DAY_OF_MONTH);

                if(parseError){
                    error = "Invalid date format";
                    result = false;
                }
                if(validationType.equals("future")) {
                    if( (selectedYear < year) || (selectedYear == year && selectedMonth < month) || (selectedYear == year && selectedMonth == month && selectedDay < day) ){
                        error = "Date must be upcoming";
                        result = false;
                    }
                }else if(validationType.equals("past")) {
                    if((selectedYear > year) || (selectedYear == year && selectedMonth > month) || (selectedYear == year && selectedMonth == month && selectedDay > day)){
                        error = "Date must be in the past";
                        result = false;
                    }
                }
            }

            //Validation options for number input
            else if(type.equals(NUMBER)) {
                if (validationType.equals("phone")) {
                    if (!data.matches(ValidationManager.getPhoneNumberRegex())) {
                        error = ValidationManager.getPhoneNumberRegexError();
                        result = false;
                    }
                } else if (validationType.equals("age")) {
                    if(!data.matches(ValidationManager.getAgeRegex())){
                        error = ValidationManager.getAgeRegexError();
                        result = false;
                    }
                    //int age = Integer.getInteger(data);
                    int age = Integer.parseInt(data);
                    if (age < 0) {
                        error = "Age cannot be less than zero";
                        result = false;
                    } else if (age > 125) {
                        error = "Invalid age";
                        result = false;
                    }
                }
            }

        }
        return result;


    }


    public void assignImage(Bitmap bitmap){
        //Load image from file
        //Bitmap bitmap = BitmapFactory.decodeFile(path);

        //Get current image properties
        int currentWidth = bitmap.getWidth();
        int currentHeight = bitmap.getHeight();

        //Define final maximum image size
        int maxHeight = 400;
        int maxWidth = 400;
        float scale = Math.min(((float) maxHeight / currentWidth), ((float)maxWidth / currentHeight));
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        //Compress the bitmap
        Bitmap bitmapCompressed = Bitmap.createBitmap(bitmap, 0, 0, currentWidth, currentHeight, matrix, true);
        //bitmap.recycle(); //trigger garbage collection now

        //Transcode to base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmapCompressed.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] b = outputStream.toByteArray();
        data = Base64.encodeToString(b, Base64.DEFAULT);
    }

    public Bitmap retrieveImage(){
        if(type != null && type.equals("image") && data != null){
            byte[] decodedString = Base64.decode(data, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            return decodedBitmap;
        }else{
            return null;
        }
    }

}
