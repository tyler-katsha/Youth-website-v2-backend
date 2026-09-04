package com.tyler.YouthEngedi.constants;

import org.springframework.beans.factory.annotation.Value;

public class UrlConstants {

    @Value("${app.production:true}")
    public static boolean production;
    public final static String FRONTEND_URL_DEV = "http://localhost:5173/";

    public final static String FRONTEND_OAUTH_DEV = FRONTEND_URL_DEV + "oauth2/redirect";

    public final static String FRONTEND_LOGIN_DEV =  FRONTEND_URL_DEV + "login";
    public final static String FRONTEND_VERIFICATION_DEV = FRONTEND_URL_DEV + "verify?token=%s&email=%s";
    public final static String FRONTEND_RESET_PASSWORD_DEV = FRONTEND_URL_DEV + "reset-password?token=%s&email=%s";
    public final static String FRONTEND_CALENDER_DEV = FRONTEND_URL_DEV + "calendar";


    public final static String FRONTEND_URL_PROD = "https://engedi.netlify.app/";
    public final static String FRONTEND_LOGIN_PROD =  FRONTEND_URL_PROD + "login";
    public final static String FRONTEND_OAUTH_PROD = FRONTEND_URL_PROD + "oauth2/redirect";
    public final static String FRONTEND_VERIFICATION_PROD = FRONTEND_URL_PROD + "verify?token=%s&email=%s";
    public final static String FRONTEND_RESET_PASSWORD_PROD = FRONTEND_URL_PROD + "reset-password?token=%s&email=%s";
    public final static String FRONTEND_CALENDER_PROD = FRONTEND_URL_PROD + "calendar";

    public final static String PYTHON_URL_LOCAL = "http://localhost:8001/";
    public final static String PYTHON_GREETING_DEV = PYTHON_URL_LOCAL + "greeting";
    public final static String PYTHON_PREDICTION_DEV = PYTHON_URL_LOCAL + "predict";

    @Value("${internal.api}")
    public static String PYTHON_URL_PROD;
    public final static String PYTHON_GREETING_PROD  = PYTHON_URL_PROD + "greeting";
    public final static String PYTHON_PREDICTION_PROD  = PYTHON_URL_PROD + "predict";
}
