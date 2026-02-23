package com.pm.analyticsservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    @PostConstruct
    public void init() {
        log.info("KafkaConsumer initialized and ready " +
                "to listen to topic 'patient' with groupId 'analytics-service'");
    }

    @KafkaListener(topics = "patient", groupId = "analytics-service")
    public void consumeEvent(byte[] event) {
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);

            // perform any business logic related to analytics here
            log.info("Receive Patient Event: [PatientId={}, PatientName={}, PatientEmail={}]",
                    patientEvent.getPatientId(), patientEvent.getName(), patientEvent.getEmail());

        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing event {}", e.getMessage());
        }
    }
}
