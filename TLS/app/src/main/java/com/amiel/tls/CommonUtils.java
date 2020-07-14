package com.amiel.tls;

class CommonUtils {

    private static String UNIDENTIFIED = "לא מזוהה";
    private static String ARMY_PERIOD_MUST = "חובה";
    private static String ARMY_PERIOD_SIGN = "קבע";
    private static String GENDER_BOYS = "בנים";
    private static String GENDER_GIRLS = "בנות";
    private static String ROOM_TYPE_LOTEM = "לוטם";
    private static String ROOM_TYPE_TOPAZ = "טופז";

    static String intToArmyPeriod(Integer armyPeriod)
    {
        String returnVal = UNIDENTIFIED;

        switch(armyPeriod)
        {
            case 0:
                returnVal = ARMY_PERIOD_MUST;
                break;
            case 1:
                returnVal = ARMY_PERIOD_SIGN;
                break;
            default:
                break;
        }

        return returnVal;
    }

    static String intToRoomType(Integer roomType)
    {
        String returnVal = UNIDENTIFIED;

        switch(roomType)
        {
            case 0:
                returnVal = ROOM_TYPE_TOPAZ;
                break;
            case 1:
                returnVal = ROOM_TYPE_LOTEM;
                break;
            default:
                break;
        }

        return returnVal;
    }

    static String intToGender(Integer gender)
    {
        String returnVal = UNIDENTIFIED;

        switch(gender)
        {
            case 0:
                returnVal = GENDER_BOYS;
                break;
            case 1:
                returnVal = GENDER_GIRLS;
                break;
            default:
                break;
        }

        return returnVal;
    }
}
