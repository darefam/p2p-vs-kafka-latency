package com.meetup.kafkalondon;

public abstract class Receiver extends Configurer {

    public Receiver() throws Exception {
    }

    public abstract void emitMetrics() throws Exception;

    public abstract void shutdown() throws Exception;
}
