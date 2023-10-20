package com.amiel.tls.db;

import androidx.annotation.NonNull;

import com.amiel.tls.CommonUtils;
import com.amiel.tls.db.entities.Person;
import com.amiel.tls.db.entities.Room;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class DBHandler{

    // Constants
    private static String TABLE_ROOMS = "rooms";
    private static String TABLE_PERSONS = "persons";
    private static String TABLE_ROOMS_SIZE = "roomsCount";
    private static String TABLE_PERSONS_SIZE = "personsCount";

    private static String PERSON_MID = "MID";
    private static String ROOM_CURRENT_CAPACITY = "currentCapacity";
    private static String ROOM_NAME = "roomName";
    private static String ROOM_ID = "roomID";
    private static String PERSON_ROOM_LEADER = "roomLeader";
    private static String PERSON_ARMY_PERIOD = "armyPeriod";
    private static String PERSON_BRANCH = "branch";
    private static String PERSON_FULL_NAME = "fullName";
    private static String PERSON_HOME_TOWN = "homeTown";
    private static String PERSON_PHONE_NUMBER = "phoneNumber";
    private static String PERSON_RELEASE_DATE = "releaseDate";
    private static String PERSON_FIRST_LOGIN = "firstLogin";
    private static String PERSON_PASSWORD_HASH = "passwordHash";

    private static DatabaseReference rootRef, tableRef;
    private static Integer roomCount;
    private static Integer personCount;

    public static void addRoom(final Room newRoom)
    {
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child(TABLE_ROOMS_SIZE).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        roomCount = dataSnapshot.getValue(Integer.class);
                        rootRef.child(TABLE_ROOMS_SIZE).setValue(roomCount + 1);

                        tableRef = rootRef.child(TABLE_ROOMS).child(String.valueOf(roomCount));
                        tableRef.setValue(newRoom);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }

    public static void addPerson(final Person newPerson)
    {
        try {
            newPerson.passwordHash = CommonUtils.calculateHash(newPerson.phoneNumber);
            newPerson.isAdmin = false;
            newPerson.firstLogin = false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child(TABLE_PERSONS_SIZE).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        personCount = dataSnapshot.getValue(Integer.class);
                        rootRef.child(TABLE_PERSONS_SIZE).setValue(personCount + 1);

                        tableRef = rootRef.child(TABLE_PERSONS).child(String.valueOf(personCount));
                        tableRef.setValue(newPerson);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

        rootRef.child(TABLE_ROOMS).child(String.valueOf(newPerson.roomID)).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Room room = dataSnapshot.getValue(Room.class);
                        room.currentCapacity = room.currentCapacity + 1;
                        rootRef.child(TABLE_ROOMS).child(String.valueOf(newPerson.roomID)).setValue(room);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }

    public static void addAdmin(final Person newAdmin)
    {
        try {
            newAdmin.passwordHash = CommonUtils.calculateHash(newAdmin.phoneNumber);
            newAdmin.isAdmin = true;
            newAdmin.firstLogin = false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child(TABLE_PERSONS_SIZE).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        personCount = dataSnapshot.getValue(Integer.class);
                        rootRef.child(TABLE_PERSONS_SIZE).setValue(personCount + 1);

                        tableRef = rootRef.child(TABLE_PERSONS).child(String.valueOf(personCount));
                        tableRef.setValue(newAdmin);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }

    public static void removePerson(final Person toRemove)
    {
        rootRef = FirebaseDatabase.getInstance().getReference();
        tableRef = FirebaseDatabase.getInstance().getReference();
        Query personQuery = rootRef.child(TABLE_PERSONS).orderByChild(PERSON_MID).equalTo(toRemove.MID);
        personQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableRef = tableRef.child(TABLE_ROOMS).child(String.valueOf(toRemove.roomID));
                tableRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Room room = dataSnapshot.getValue(Room.class);
                        tableRef.child(ROOM_CURRENT_CAPACITY).setValue(room.currentCapacity - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                for(DataSnapshot currSnap : dataSnapshot.getChildren()) {
                    currSnap.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void removePerson(final Integer roomID, final String MIDtoRemove)
    {
        rootRef = FirebaseDatabase.getInstance().getReference();
        tableRef = FirebaseDatabase.getInstance().getReference();
        Query personQuery = rootRef.child(TABLE_PERSONS).orderByChild(PERSON_MID).equalTo(MIDtoRemove);
        personQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableRef = tableRef.child(TABLE_ROOMS).child(String.valueOf(roomID));
                tableRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Room room = dataSnapshot.getValue(Room.class);
                        tableRef.child(ROOM_CURRENT_CAPACITY).setValue(room.currentCapacity - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                for(DataSnapshot currSnap : dataSnapshot.getChildren()) {
                    currSnap.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void removePerson(final Integer personID)
    {
        rootRef = FirebaseDatabase.getInstance().getReference();
        tableRef = FirebaseDatabase.getInstance().getReference();
        Query personQuery = rootRef.child(TABLE_PERSONS).child(String.valueOf(personID));
        personQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableRef = tableRef.child(TABLE_ROOMS).child(String.valueOf(((Person)(dataSnapshot.getValue())).roomID));
                tableRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Room room = dataSnapshot.getValue(Room.class);
                        tableRef.child(ROOM_CURRENT_CAPACITY).setValue(room.currentCapacity - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                for(DataSnapshot currSnap : dataSnapshot.getChildren()) {
                    currSnap.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void getPerson(final String personMID, final String personName, final OnGetPersonDataListener listener)
    {
        listener.onStart();
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child(TABLE_PERSONS).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<Integer, Person> persons = new HashMap<>();
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            Person currPerson = ds.getValue(Person.class);
                            if(currPerson.fullName.equals(personName) && currPerson.MID.equals(personMID)) {
                                persons.put(Integer.parseInt(ds.getKey()), currPerson);
                            }
                        }
                        try {
                            listener.onSuccess(persons);
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onFailed(databaseError);
                    }
                }
        );
    }

    public static void getPerson(final String personMID, final OnGetPersonDataListener listener)
    {
        listener.onStart();
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child(TABLE_PERSONS).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<Integer, Person> persons = new HashMap<>();
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            Person currPerson = ds.getValue(Person.class);
                            if(currPerson.MID.equals(personMID)) {
                                persons.put(Integer.parseInt(ds.getKey()), currPerson);
                            }
                        }
                        try {
                            listener.onSuccess(persons);
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onFailed(databaseError);
                    }
                }
        );
    }

    public static void updatePerson(final Person personToUpdate)
    {
        rootRef = FirebaseDatabase.getInstance().getReference();
        Query allOtherPersons = rootRef.child(TABLE_PERSONS).orderByChild(ROOM_ID).equalTo(personToUpdate.roomID);
        allOtherPersons.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot currSnap : dataSnapshot.getChildren()) {
                    if(currSnap.child(PERSON_MID).getValue(String.class).equals(personToUpdate.MID)) {
                        currSnap.child(PERSON_ARMY_PERIOD).getRef().setValue(personToUpdate.armyPeriod);
                        currSnap.child(PERSON_BRANCH).getRef().setValue(personToUpdate.branch);
                        currSnap.child(PERSON_FULL_NAME).getRef().setValue(personToUpdate.fullName);
                        currSnap.child(PERSON_HOME_TOWN).getRef().setValue(personToUpdate.homeTown);
                        currSnap.child(PERSON_PHONE_NUMBER).getRef().setValue(personToUpdate.phoneNumber);
                        currSnap.child(PERSON_RELEASE_DATE).getRef().setValue(personToUpdate.releaseDate);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void updatePersonFirstLogin(final String mid, final String newHashedPassword, final Integer roomID)
    {
        rootRef = FirebaseDatabase.getInstance().getReference();
        Query allOtherPersons = rootRef.child(TABLE_PERSONS).orderByChild(ROOM_ID).equalTo(roomID);
        allOtherPersons.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot currSnap : dataSnapshot.getChildren()) {
                    if(currSnap.child(PERSON_MID).getValue(String.class).equals(mid)) {
                        currSnap.child(PERSON_FIRST_LOGIN).getRef().setValue(true);
                        currSnap.child(PERSON_PASSWORD_HASH).getRef().setValue(newHashedPassword);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void removeRoom(final Room toRemove)
    {
        rootRef = FirebaseDatabase.getInstance().getReference();
        tableRef = FirebaseDatabase.getInstance().getReference();
        Query roomQuery = rootRef.child(TABLE_ROOMS).orderByChild(ROOM_NAME).equalTo(toRemove.roomName);
        roomQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot currSnap : dataSnapshot.getChildren()) {
                    Room currRoom = currSnap.getValue(Room.class);
                    if(currRoom.roomGender == toRemove.roomGender && currRoom.roomType == toRemove.roomType) {
                        tableRef.child(TABLE_PERSONS).orderByChild(ROOM_ID).equalTo(currSnap.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot currSnap : dataSnapshot.getChildren()) {
                                    currSnap.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        currSnap.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void removeRoom(final Integer roomID)
    {
        rootRef = FirebaseDatabase.getInstance().getReference();
        Query roomQuery = rootRef.child(TABLE_ROOMS).child(String.valueOf(roomID));
        roomQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot currSnap : dataSnapshot.getChildren()) {
                    currSnap.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query allPersons = rootRef.child(TABLE_PERSONS).orderByChild(ROOM_ID).equalTo(roomID);
        allPersons.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot currSnap : dataSnapshot.getChildren()) {
                    currSnap.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void setNewRoomLeader(final Person newRoomLeader) {
        rootRef = FirebaseDatabase.getInstance().getReference();
        Query allOtherPersons = rootRef.child(TABLE_PERSONS).orderByChild(ROOM_ID).equalTo(newRoomLeader.roomID);
        allOtherPersons.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot currSnap : dataSnapshot.getChildren()) {
                    if(currSnap.child(PERSON_MID).getValue(String.class).equals(newRoomLeader.MID)) {
                        currSnap.child(PERSON_ROOM_LEADER).getRef().setValue(true);
                    } else {
                        currSnap.child(PERSON_ROOM_LEADER).getRef().setValue(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void setNewRoomLeader(final Integer roomID, final String newLeaderMID) {
        rootRef = FirebaseDatabase.getInstance().getReference();
        Query allOtherPersons = rootRef.child(TABLE_PERSONS).orderByChild(ROOM_ID).equalTo(roomID);
        allOtherPersons.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot currSnap : dataSnapshot.getChildren()) {
                    if(currSnap.child(PERSON_MID).getValue(String.class).equals(newLeaderMID)) {
                        currSnap.child(PERSON_ROOM_LEADER).getRef().setValue(true);
                    } else {
                        currSnap.child(PERSON_ROOM_LEADER).getRef().setValue(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void getAllAvailableRooms(final OnGetRoomDataListener listener)
    {
        listener.onStart();
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child(TABLE_ROOMS).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<Integer, Room> availableRooms = new HashMap<>();
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            Room currRoom = ds.getValue(Room.class);
                            if(currRoom.currentCapacity < currRoom.maxCapacity) {
                                availableRooms.put(Integer.parseInt(ds.getKey()), currRoom);
                            }
                        }
                        listener.onSuccess(availableRooms);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onFailed(databaseError);
                    }
                }
        );
    }

    public static void getAllRoomsByFilter(final Integer roomType, final Integer roomGender, final OnGetRoomDataListener listener)
    {
        listener.onStart();
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child(TABLE_ROOMS).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<Integer, Room> availableRooms = new HashMap<>();
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            Room currRoom = ds.getValue(Room.class);
                            if(currRoom.roomType == roomType
                            && currRoom.roomGender == roomGender) {
                                availableRooms.put(Integer.parseInt(ds.getKey()), currRoom);
                            }
                        }
                        listener.onSuccess(availableRooms);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onFailed(databaseError);
                    }
                }
        );
    }

    public static void getLeaderInRoom(final Integer roomID, final OnGetPersonDataListener listener)
    {
        listener.onStart();
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child(TABLE_PERSONS).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<Integer, Person> persons = new HashMap<>();
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            Person currPerson = ds.getValue(Person.class);
                            if(currPerson.roomID == roomID && currPerson.roomLeader) {
                                persons.put(Integer.parseInt(ds.getKey()), currPerson);
                            }
                        }
                        try {
                            listener.onSuccess(persons);
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onFailed(databaseError);
                    }
                }
        );
    }

    public static void getAllPersonsInRoom(final Integer roomID, final OnGetPersonDataListener listener)
    {
        listener.onStart();
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child(TABLE_PERSONS).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<Integer, Person> persons = new HashMap<>();
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            Person currPerson = ds.getValue(Person.class);
                            if(currPerson.roomID == roomID) {
                                persons.put(Integer.parseInt(ds.getKey()), currPerson);
                            }
                        }
                        try {
                            listener.onSuccess(persons);
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onFailed(databaseError);
                    }
                }
        );
    }

    public static void getAllRoomLeaders(final OnGetPersonDataListener listener) {
        listener.onStart();
        rootRef = FirebaseDatabase.getInstance().getReference();
        Query allOtherPersons = rootRef.child(TABLE_PERSONS).orderByChild(PERSON_ROOM_LEADER).equalTo(true);
        allOtherPersons.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<Integer, Person> roomLeaders = new HashMap<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Person currPerson = ds.getValue(Person.class);
                    roomLeaders.put(Integer.parseInt(ds.getKey()), currPerson);
                }
                try {
                    listener.onSuccess(roomLeaders);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailed(databaseError);
            }
        });
    }

    public interface OnGetRoomDataListener {
        void onStart();
        void onSuccess(Map<Integer, Room> data);
        void onFailed(DatabaseError databaseError);
    }

    public interface OnGetPersonDataListener {
        void onStart();
        void onSuccess(Map<Integer, Person> data) throws NoSuchAlgorithmException;
        void onFailed(DatabaseError databaseError);
    }
}
