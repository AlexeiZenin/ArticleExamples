package com.zenin.listenererasure;

import com.zenin.events.Train;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

@EmbeddedKafka(topics = TrainIntegrationTest.TRAIN_EVENTS_TOPIC)
@SpringBootTest
@ActiveProfiles("test")
@Import(SenderTestBeans.class)
class TrainIntegrationTest {

    public static final String TRAIN_EVENTS_TOPIC = "train-events";

    @SpyBean
    private TrainEventListener trainEventListener;

    @Autowired
    private KafkaTemplate<String, Train> avroRecordTemplate;

    @Autowired
    @Qualifier(SenderTestBeans.STRING_SENDER)
    private KafkaTemplate<String, String> stringTemplate;

    @Autowired
    @Qualifier(SenderTestBeans.PRIMITIVE_SENDER)
    private KafkaTemplate<String, Object> primitiveTemplate;

    @Test
    void sendTrainEvents() throws ExecutionException, InterruptedException {
        Train train = Train.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSpeedInMetric(85)
                .setWeightInTonnes(11700)
                .build();

        avroRecordTemplate.send(TRAIN_EVENTS_TOPIC, train).get();

        verify(trainEventListener, after(5000).times(1)).processTrainEvent(anyList());
    }

    @Test
    void sendStringGarbage() throws ExecutionException, InterruptedException {
        stringTemplate.send(TRAIN_EVENTS_TOPIC, "garbage").get();

        verify(trainEventListener, after(5000).times(1)).processTrainEvent(anyList());
    }

    @Test
    void sendStringConfluent() throws ExecutionException, InterruptedException {
        primitiveTemplate.send(TRAIN_EVENTS_TOPIC, "garbage").get();

        verify(trainEventListener, after(5000).times(1)).processTrainEvent(anyList());
    }
}
