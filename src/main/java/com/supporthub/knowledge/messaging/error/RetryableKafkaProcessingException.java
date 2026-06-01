package com.supporthub.knowledge.messaging.error;

public class RetryableKafkaProcessingException extends RuntimeException {

    public RetryableKafkaProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
