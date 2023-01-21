package com.meetup.kafkalondon;

import com.meetup.kafkalondon.kafka.KafkaPublisher;
import com.meetup.kafkalondon.tcp.TcpPublisher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventPublisherApplication {

    public EventPublisherApplication() {
    }

    public static void main(String[] args) throws Exception {
        log.info("Initializing Event Publisher....");
        final String channelMode = CommonUtil.getChannelMode();
        if (ChannelMode.isValidMode(channelMode)) {
            final Publisher publisher;
            if (channelMode.equals(ChannelMode.tcp.name())) {
                publisher = new TcpPublisher();
            } else {
                publisher = new KafkaPublisher();
            }
            publisher.start();
            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> {
                        try {
                            publisher.shutdown();
                        } catch (Exception e) {
                        }
                    },
                    "event-publisher-shutdown-thread"));
        } else {
            System.exit(0);
        }
    }
}
