package com.meetup.kafkalondon;

public abstract class Publisher extends Configurer {

    public Publisher() throws Exception {
    }

    public abstract void send(String event);

    public abstract void shutdown() throws Exception;
}
