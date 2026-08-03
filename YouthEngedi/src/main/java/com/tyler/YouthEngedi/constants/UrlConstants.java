package com.tyler.YouthEngedi.constants;

public class UrlConstants {

    public final static String FRONTEND_URL_DEV = "http://localhost:5173/";

    public final static String FRONTEND_OAUTH_DEV = FRONTEND_URL_DEV + "oauth2/redirect";

    public final static String FRONTEND_LOGIN_DEV =  FRONTEND_URL_DEV + "login";
    public final static String FRONTEND_VERIFICATION_DEV = FRONTEND_URL_DEV + "verify?token=%s&email=%s";
    public final static String FRONTEND_RESET_PASSWORD_DEV = FRONTEND_URL_DEV + "reset-password?token=%s";



    public final static String FRONTEND_URL_PROD = "https://engedi.netlify.app/";
    public final static String FRONTEND_LOGIN_PROD =  FRONTEND_URL_PROD + "login";
    public final static String FRONTEND_OAUTH_PROD = FRONTEND_URL_PROD + "oauth2/redirect";
    public final static String FRONTEND_VERIFICATION_PROD = FRONTEND_URL_PROD + "verify?token=%s&email=%s";
    public final static String FRONTEND_RESET_PASSWORD_PROD = FRONTEND_URL_PROD + "reset-password?token=%s";


}
