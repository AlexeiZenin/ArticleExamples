package com.zenin.listenererasure;

import com.zenin.events.Train;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TrainEventListener {

    @KafkaListener(topicPattern = ".*")
    public void processTrainEvent(List<Train> trains) {
        log.info("Received a train [{}] events", trains.size());
        for (Train train : trains) {
            analyzeTrain(train);
        }
        log.info("Done analyzing events");
    }

    private void analyzeTrain(Train train) {
        log.info("Received a train event: [{}]", train);

        double momentum = getMetersPerSecond(train) * getWeightInKilos(train);
        if (momentum > train.getMomentumLimit()) {
            log.warn("Momentum alert! Train [{}] exceeded its momentum limit. Limit [{}], Actual [{}]",
                    train.getId(),
                    train.getMomentumLimit(),
                    momentum
            );
        }
    }

    private int getWeightInKilos(Train train) {
        return train.getWeightInTonnes() * 1000;
    }

    private double getMetersPerSecond(Train train) {
        return train.getSpeedInMetric() / 3.6;
    }
}
