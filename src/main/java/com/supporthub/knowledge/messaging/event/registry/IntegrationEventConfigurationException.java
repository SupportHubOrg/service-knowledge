package com.supporthub.knowledge.messaging.event.registry;

public class IntegrationEventConfigurationException extends RuntimeException {

    public IntegrationEventConfigurationException(String message) {
        super(message);
    }

    public IntegrationEventConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
