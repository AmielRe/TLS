package com.amiel.tls;

public class Constants {

    // Google Sheets Consts
    static final Integer SHEET_START_ROW_INDEX = 1;
    static final String PREF_ACCOUNT_NAME = "accountName";
    static final String GOOGLE_SHEET_APP_NAME = "TLSApp";

    static final String REQUESTS_SPREAD_SHEET_ID = "1SBopcGXJF2DBxTwMwyJVOnal0M-bWpyReONjcMgjJsE";
    static final String REQUESTS_SHEET_NAME = "'תגובות לטופס 1'";
    static final String REQUEST_SHEET_FETCH_RANGE = "!A:I";

    static final String backSlash = "/";

    // Whatsapp Consts
    static final String WHATSAPP_PACKAGE = "com.whatsapp";
    static final String REQUEST_FORM = "https://forms.gle/mp49Y7KWsn8Ma8Kk6";
    static final String ISRAEL_LOCALE_PHONE_PREFIX = "972";
    static final String SEND_API_PREFIX = "https://api.whatsapp.com/send";
    static final String SEND_API_PHONE_PARAM = "?phone=";
    static final String SEND_API_MESSAGE_PARAM = "&text=";

    // Phone call consts
    static final String CALL_PHONE_NUMBER_PREFIX = "tel:";

    // Unique ID's in DB
    public static final int ARMY_PERIOD_MUST_INT = 0;
    public static final int ARMY_PERIOD_SIGN_INT = 1;
    public static final int ROOM_TYPE_TOPAZ_INT = 0;
    public static final int ROOM_TYPE_LOTEM_INT = 1;
    public static final int GENDER_BOYS_INT = 0;
    public static final int GENDER_GIRLS_INT = 1;

    // Indexes inside the sheet (for each row)
    static final Integer GENDER_INDEX = 1;
    static final Integer ARMY_PERIOD_INDEX = 2;
    static final Integer FULL_NAME_INDEX = 3;
    static final Integer MID_INDEX = 4;
    static final Integer PHONE_NUMBER_INDEX = 5;
    static final Integer HOME_TOWN_INDEX = 6;
    static final Integer RELEASE_DATE_INDEX = 7;
    static final Integer BRANCH_INDEX = 8;

    // Request ID's for actions
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
}
