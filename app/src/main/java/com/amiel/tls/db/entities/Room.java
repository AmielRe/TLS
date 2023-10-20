package com.amiel.tls.db.entities;

import java.io.Serializable;

public class Room implements Serializable {

    public String roomName;
    public Integer maxCapacity;
    public Integer currentCapacity;
    public Integer roomType;
    public Integer roomGender;

    public Room()
    {

    }

    public Room(String roomName, Integer maxCapacity, Integer currentCapacity, Integer roomType, Integer roomGender)
    {
        this.roomName = roomName;
        this.maxCapacity = maxCapacity;
        this.currentCapacity = currentCapacity;
        this.roomType = roomType;
        this.roomGender = roomGender;
    }
}
