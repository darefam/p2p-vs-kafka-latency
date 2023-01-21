package com.meetup.kafkalondon.event;

import com.meetup.kafkalondon.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Random;

@Slf4j
final public class LoadGenerator {
    public static final Random RANDOM = new Random();
    private static final String[] meetupType = {"Online", "In-Person"};
    private static final String[] meetupGroup = {"Apache Kafka", "Scala", "Kubernetes", "Python", "Java", "Go",
                                                 "Apache Flink", "RedPanda", "Apache Spark", "Akka"
    };
    public static final int KEY_RANGE = meetupGroup.length;
    /**
     * Duration in sec
     */
    final int duration;
    /**
     * Load in packets per sec
     */
    final int load;
    final Publisher publisher;

    public LoadGenerator(Publisher publisher, int load, int duration) {
        this.publisher = publisher;
        this.load = load;
        this.duration = duration;
    }

    public void generateAndSend() throws InterruptedException {
        final int num_packets = load * duration;
        int randIndex;
        long pause = 1000 / load;
        for (int i = 0; i < num_packets; i++) {
            JSONObject jsonData = new JSONObject();
            randIndex = RANDOM.nextInt(KEY_RANGE);
            jsonData.put("meetup-group", meetupGroup[randIndex]);
            jsonData.put("meetup-type", meetupType[randIndex % 2]);
            jsonData.put("attendance", 100 + randIndex);
            jsonData.put("publish-time", Instant.now().getNano());
            log.info("Publishing event: {}", jsonData);
            publisher.send(jsonData.toString());
            Thread.sleep(pause);
        }
    }
}
