package com.zenin.listenererasure;

import com.zenin.events.Train;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;

@Configuration
public class SenderTestBeans {
    public static final String PRIMITIVE_SENDER = "primitive";
    public static final String STRING_SENDER = "string";
    @Autowired
    private KafkaProperties kafkaProperties;

    @Bean
    public MockSchemaRegistryClient mockSchemaRegistry() {
        return new MockSchemaRegistryClient();
    }

    @Bean
    public KafkaAvroSerializer kafkaAvroSerializer() {
        return new KafkaAvroSerializer(mockSchemaRegistry(), kafkaProperties.buildProducerProperties());
    }

    @Bean
    public KafkaAvroDeserializer kafkaAvroDeserializer() {
        return new KafkaAvroDeserializer(mockSchemaRegistry(), kafkaProperties.buildConsumerProperties());
    }

    @Bean
    public DefaultKafkaProducerFactory confluentProducer() {
        return new DefaultKafkaProducerFactory<>(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                kafkaAvroSerializer());
    }

    @Bean
    public DefaultKafkaConsumerFactory kafkaAvroConsumerFactory(){
        return new DefaultKafkaConsumerFactory(
                kafkaProperties.buildConsumerProperties(),
                new StringDeserializer(),
                new ErrorHandlingDeserializer2(kafkaAvroDeserializer())
        );
    }

    @Bean
    @Qualifier(PRIMITIVE_SENDER)
    public KafkaTemplate<String, Object> confluentPrimitiveKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                kafkaAvroSerializer()
        ));
    }

    @Bean
    @Qualifier(STRING_SENDER)
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                new StringSerializer()
        ));
    }

    @Bean
    @Primary
    public KafkaTemplate<String, Train> avroKafkaTemplate() {
        return new KafkaTemplate<String, Train>(confluentProducer()
        );
    }
}
