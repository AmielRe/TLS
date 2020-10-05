package com.amiel.tls;

public class Constants {

    public static final Integer SHEET_START_ROW_INDEX = 1;
    public static final String PREF_ACCOUNT_NAME = "accountName";

    // Indexes inside the sheet (for each row)
    public static final Integer GENDER_INDEX = 1;
    public static final Integer ARMY_PERIOD_INDEX = 2;
    public static final Integer FULL_NAME_INDEX = 3;
    public static final Integer MID_INDEX = 4;
    public static final Integer PHONE_NUMBER_INDEX = 5;
    public static final Integer HOME_TOWN_INDEX = 6;
    public static final Integer RELEASE_DATE_INDEX = 7;
    public static final Integer BRANCH_INDEX = 8;

    // Request ID's for actions
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
}
