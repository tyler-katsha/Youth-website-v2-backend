package com.tyler.YouthEngedi.utils;

import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.Guest;
import com.tyler.YouthEngedi.models.events.ContinueAsGuestEvent;
import com.tyler.YouthEngedi.models.events.UserLoginEvent;
import com.tyler.YouthEngedi.models.events.UserLogoutEvent;

import java.time.LocalDateTime;

public final class WebSocketHelper {

    public static UserLoginEvent buildLogin(User user){
        return UserLoginEvent.builder()
                .email(user.getEmail())
                .message(user.getEmail() + " has logged in at " + TimeUtils.formatDateTime(LocalDateTime.now()))
                .userId(user.getId())
                .timeStamp(System.currentTimeMillis())
                .build();
    }

    public static UserLogoutEvent buildLogout(User user){
        return UserLogoutEvent.builder()
                .email(user.getEmail())
                .message(user.getEmail() + " has logged out at " + TimeUtils.formatDateTime(LocalDateTime.now()))
                .userId(user.getId())
                .timeStamp(System.currentTimeMillis())
                .build();
    }

    public static ContinueAsGuestEvent buildGuest(User fakeUser){
        return ContinueAsGuestEvent
                .builder()
                .email(fakeUser.getEmail())
                .message(fakeUser.getEmail() + " has logged in at " + TimeUtils.formatDateTime(LocalDateTime.now()))
                .userId(fakeUser.getId())
                .timeStamp(System.currentTimeMillis())
                .build();
    }

    public static ContinueAsGuestEvent buildGuestDestroyed(Guest existingGuest) {
        return ContinueAsGuestEvent
                .builder()
                .email(existingGuest.getFakeEmail())
                .message(existingGuest.getFakeEmail() + " has logged out at " + TimeUtils.formatDateTime(LocalDateTime.now()))
                .userId(existingGuest.getFakeUserId())
                .timeStamp(System.currentTimeMillis())
                .build();
    }
}
