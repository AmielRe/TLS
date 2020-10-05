package com.amiel.tls;

public class WaitingListPerson {

    Integer rowID;
    String fullName;
    String MID;
    String releaseDate;
    String branch;
    String phoneNumber;
    String homeTown;
    String armyPeriod;
    String gender;

    WaitingListPerson()
    {

    }

    public WaitingListPerson(Integer rowID, String fullName, String MID, String releaseDate, String branch, String phoneNumber, String homeTown, String armyPeriod, String gender)
    {
        this.rowID = rowID;
        this.fullName = fullName;
        this.MID = MID;
        this.releaseDate = releaseDate;
        this.branch = branch;
        this.phoneNumber = phoneNumber;
        this.homeTown = homeTown;
        this.armyPeriod = armyPeriod;
        this.gender = gender;
    }
}
