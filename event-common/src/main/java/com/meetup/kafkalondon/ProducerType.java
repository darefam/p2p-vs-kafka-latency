package com.meetup.kafkalondon;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public enum ProducerType {
    nack, ackLeader, ackAll, idempotent;

    public static boolean isValidProducerType(String producerType) {
        for (ProducerType c : ProducerType.values()) {
            if (c.name().equals(producerType)) {
                return true;
            }
        }
        log.error("An invalid producer type specified via Environment Variable, PRODUCER_TYPE! " +
                  "Valid Values are : {}", Arrays.toString(ProducerType.values()));
        return false;
    }
}
