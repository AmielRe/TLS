package com.amiel.tls;

public class WaitingListPerson {

    public Integer rowID;
    public String fullName;
    public String MID;
    public String releaseDate;
    public String branch;
    public String phoneNumber;
    public String homeTown;
    public String armyPeriod;
    public String gender;

    public WaitingListPerson()
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
