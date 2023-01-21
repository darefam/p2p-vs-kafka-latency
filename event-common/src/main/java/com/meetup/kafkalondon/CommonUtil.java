package com.meetup.kafkalondon;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final public class CommonUtil {

    public static String getChannelMode() {
        final String channelMode = System.getenv().getOrDefault("CHANNEL_MODE", "tcp");
        log.info("Channel Mode : {}", channelMode);
        return channelMode;
    }
}
