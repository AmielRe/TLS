package com.amiel.tls;

import android.content.Context;

class CommonUtils {

    static String intToArmyPeriod(Context context, Integer armyPeriod)
    {
        String returnVal = context.getString(R.string.Unidentified);

        switch(armyPeriod)
        {
            case Constants.ARMY_PERIOD_MUST_INT:
                returnVal = context.getString(R.string.must);
                break;
            case Constants.ARMY_PERIOD_SIGN_INT:
                returnVal = context.getString(R.string.sign);
                break;
            default:
                break;
        }

        return returnVal;
    }

    static String intToRoomType(Context context, Integer roomType)
    {
        String returnVal = context.getString(R.string.Unidentified);

        switch(roomType)
        {
            case Constants.ROOM_TYPE_TOPAZ_INT:
                returnVal = context.getString(R.string.topaz);
                break;
            case Constants.ROOM_TYPE_LOTEM_INT:
                returnVal = context.getString(R.string.lotem);
                break;
            default:
                break;
        }

        return returnVal;
    }

    static String intToGender(Context context, Integer gender)
    {
        String returnVal = context.getString(R.string.Unidentified);

        switch(gender)
        {
            case Constants.GENDER_BOYS_INT:
                returnVal = context.getString(R.string.boys);
                break;
            case Constants.GENDER_GIRLS_INT:
                returnVal = context.getString(R.string.girls);
                break;
            default:
                break;
        }

        return returnVal;
    }
}
