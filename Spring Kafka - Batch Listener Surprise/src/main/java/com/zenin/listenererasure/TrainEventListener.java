package com.zenin.listenererasure;

import com.zenin.events.Train;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TrainEventListener {

    /**
     * In Km/h
     */
    public static final int SPEED_LIMIT = 80;

    @KafkaListener(topicPattern = ".*")
    public void processTrainEvent(List<Train> trains) {
        log.info("Received [{}] train events", trains.size());
        for (Train train : trains) {
            analyzeTrain(train);
        }
        log.info("Done analyzing events");
    }

    private void analyzeTrain(Train train) {
        log.info("Received a train event: [{}]", train);

        if (train.getSpeedInMetric() > SPEED_LIMIT) {
            log.warn("Speed alert! Train [{}] exceeded the speed limit. Limit [{}], Actual [{}]",
                    train.getId(),
                    SPEED_LIMIT,
                    train.getSpeedInMetric()
            );
        }
    }
}
