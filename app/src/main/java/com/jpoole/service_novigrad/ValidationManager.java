package com.jpoole.service_novigrad;

import javax.annotation.RegEx;

public class ValidationManager {


    @RegEx
    private static final String emailRegex = "^\\w+@\\w+\\.\\w+$";
    private static final String emailRegexError = "Not a valid email";

    @RegEx
    private static final String nameRegex = "^[A-Za-z\\-\\']+$";
    private static final String nameRegexError = "Name can only contain alphabetic characters and - or '";

    @RegEx
    private static final String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d$&+,:;=?@#|'<>^*()%!.]{8,}$";
    private static final String passwordRegexError = "Minimum 8 chars, 1 lowercase, 1 uppercase, 1 number";

    @RegEx
    private static final String postalCodeRegex = "([A-Za-z]\\d[A-Za-z])(( )|-)?(\\d[A-Za-z]\\d)";
    private static final String postalCodeRegexError = "Not a valid postal code";


    //Accepts
    //      613 712 1234
    //      6134564342
    //      (613)-856.4342
    //      +1(613)-856.4342
    //      +12 6134564342
    @RegEx
    private static final String phoneNumberRegex = "^(\\+\\d{1,2}\\s?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$";
    private static final String phoneNumberRegexError = "Not a valid phone number";

    @RegEx
    private static final String ageRegex = "^\\d+$";
    private static final String ageRegexError = "Invalid characters in age";

    @RegEx
    private static final String addressRegex = "^[a-zA-Z\\d.,\\-#&;:'\"() ]+$";
    private static final String addressRegexError = "Invalid characters in address";




    public static String getEmailRegex() {
        return emailRegex;
    }
    public static String getEmailRegexError() {
        return emailRegexError;
    }
    public static String getNameRegex() {
        return nameRegex;
    }
    public static String getNameRegexError() {
        return nameRegexError;
    }
    public static String getPasswordRegex() {
        return passwordRegex;
    }
    public static String getPasswordRegexError() {
        return passwordRegexError;
    }
    public static String getPostalCodeRegex() {
        return postalCodeRegex;
    }
    public static String getPostalCodeRegexError() {
        return postalCodeRegexError;
    }
    public static String getPhoneNumberRegex() {
        return phoneNumberRegex;
    }
    public static String getPhoneNumberRegexError() {
        return phoneNumberRegexError;
    }
    public static String getAgeRegex() {
        return ageRegex;
    }
    public static String getAgeRegexError() {
        return ageRegexError;
    }
    public static String getAddressRegex() {
        return addressRegex;
    }
    public static String getAddressRegexError() {
        return addressRegexError;
    }
}
