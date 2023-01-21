package com.meetup.kafkalondon;

import com.meetup.kafkalondon.kafka.KafkaReceiver;
import com.meetup.kafkalondon.tcp.TcpReceiver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventReceiverApplication {

    public EventReceiverApplication() {
    }

    public static void main(String[] args) throws Exception {
        log.info("Initializing Event Receiver....");
        final String channelMode = CommonUtil.getChannelMode();
        if (ChannelMode.isValidMode(channelMode)) {
            final Receiver receiver;
            if (channelMode.equals(ChannelMode.tcp.name())) {
                receiver = new TcpReceiver();
            } else {
                receiver = new KafkaReceiver();
            }
            receiver.start();
            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> {
                        try {
                            receiver.emitMetrics();
                        } catch (Exception e) {}
                    },
                    "event-receiver-shutdown-thread"));
        } else {
            System.exit(0);
        }
    }
}
