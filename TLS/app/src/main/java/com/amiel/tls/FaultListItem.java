package com.amiel.tls;

public class FaultListItem {

    String roomName;
    String gender;
    String roomType;
    String faultIn;
    String airConditionNumber;
    String description;
    Integer rowID;

    public FaultListItem()
    {

    }

    public FaultListItem(String roomName, String gender, String roomType, String faultIn, String airConditionNumber, String description, Integer rowID)
    {
        this.roomName = roomName;
        this.gender = gender;
        this.roomType = roomType;
        this.faultIn = faultIn;
        this.airConditionNumber = airConditionNumber;
        this.description = description;
        this.rowID = rowID;
    }
}
