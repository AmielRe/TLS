package com.amiel.tls.db.entities;

public class Person {

    public String fullName;
    public String MID;
    public String releaseDate;
    public String branch;
    public String phoneNumber;
    public String homeTown;
    public Integer armyPeriod;
    public Integer roomID;
    public Boolean roomLeader;
    public Boolean firstLogin;
    public String passwordHash;
    public Boolean isAdmin;

    public Person()
    {

    }

    public Person(String fullName, String MID, String releaseDate, String branch, String phoneNumber, String homeTown, Integer armyPeriod, Integer roomID, Boolean roomLeader, Boolean firstLogin, String passwordHash, Boolean isAdmin)
    {
        this.fullName = fullName;
        this.MID = MID;
        this.releaseDate = releaseDate;
        this.branch = branch;
        this.phoneNumber = phoneNumber;
        this.homeTown = homeTown;
        this.armyPeriod = armyPeriod;
        this.roomID = roomID;
        this.roomLeader = roomLeader;
        this.firstLogin = firstLogin;
        this.passwordHash = passwordHash;
        this.isAdmin = isAdmin;
    }
}
