package com.supporthub.knowledge.messaging.error;

public class NonRetryableKafkaProcessingException extends RuntimeException {

    public NonRetryableKafkaProcessingException(String message) {
        super(message);
    }

    public NonRetryableKafkaProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
