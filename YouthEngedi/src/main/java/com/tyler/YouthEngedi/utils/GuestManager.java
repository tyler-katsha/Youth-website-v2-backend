package com.tyler.YouthEngedi.utils;

import com.tyler.YouthEngedi.models.dtos.Guest;

import java.util.concurrent.ConcurrentHashMap;

public final class GuestManager {

    private final static ConcurrentHashMap<Long,Guest> GUESTS = new ConcurrentHashMap<>();

    private GuestManager(){}

    public static Guest discardGuest(long fakeUserId){
        Guest removed = GUESTS.remove(fakeUserId);
        if(removed != null){
            IdManager.releaseId(fakeUserId);
        }
        return removed;
    }

    public static void addGuest(Guest guest){
        GUESTS.putIfAbsent(guest.getFakeUserId(), guest);
    }

    public static Guest fetchGuest(long fakeUserId){
        return GUESTS.get(fakeUserId);
    }

    public static boolean hasGuest(long fakeUserId){
        return GUESTS.containsKey(fakeUserId);
    }

    public static int getActiveGuestCount(){
        return GUESTS.size();
    }
    public static ConcurrentHashMap<Long, Guest> guests(){
        return GUESTS;
    }
}
