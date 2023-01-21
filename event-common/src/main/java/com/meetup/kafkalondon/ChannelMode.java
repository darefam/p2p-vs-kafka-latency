package com.meetup.kafkalondon;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public enum ChannelMode {
    tcp, kafka;

    public static boolean isValidMode(String channelMode) {
        for (ChannelMode c : ChannelMode.values()) {
            if (c.name().equals(channelMode)) {
                return true;
            }
        }
        log.error("An invalid channel Mode specified via Environment Variable, CHANNEL_MODE! " +
                  "Valid Values are : {}", Arrays.toString(ChannelMode.values()));
        return false;
    }
}
