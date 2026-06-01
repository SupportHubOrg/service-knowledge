package com.supporthub.knowledge.messaging.event;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IntegrationEventRegistry {

    private static final String EVENT_PAYLOAD_PACKAGE = "com.supporthub.knowledge.messaging.event";

    private final IntegrationEventProperties properties;
    private volatile Map<Class<?>, IntegrationEventContract> contractsByPayloadType = Map.of();

    @PostConstruct
    void initialize() {
        var discoveredPayloadTypes = findIntegrationEventPayloadTypes();
        if (discoveredPayloadTypes.isEmpty()) {
            throw new IntegrationEventConfigurationException("No integration event payloads found");
        }

        var contracts = new LinkedHashMap<Class<?>, IntegrationEventContract>();
        var payloadTypesByKey = new HashMap<String, Class<?>>();
        var payloadTypesByEventIdentity = new HashMap<EventIdentity, Class<?>>();

        discoveredPayloadTypes.forEach(payloadType -> {
            var annotation = payloadType.getAnnotation(IntegrationEvent.class);
            validateUniqueKey(payloadTypesByKey, annotation.value(), payloadType);
            var contract = resolve(payloadType, annotation);
            validateUniqueEventIdentity(payloadTypesByEventIdentity, contract, payloadType);
            contracts.put(payloadType, contract);
        });

        contractsByPayloadType = Map.copyOf(contracts);
    }

    public IntegrationEventContract get(Class<?> payloadType) {
        var contract = contractsByPayloadType.get(payloadType);
        if (contract == null) {
            throw new IntegrationEventConfigurationException(
                    "Integration event payload is not registered: " + payloadType.getName()
            );
        }
        return contract;
    }

    private IntegrationEventContract resolve(Class<?> payloadType, IntegrationEvent annotation) {
        var key = annotation.value();
        var configuredContract = contracts().get(key);
        if (configuredContract == null) {
            throw new IntegrationEventConfigurationException("Integration event contract is not configured: " + key);
        }
        if (configuredContract.eventType() == null || configuredContract.eventType().isBlank()) {
            throw new IntegrationEventConfigurationException("Integration event type is not configured: " + key);
        }
        if (configuredContract.version() < 1) {
            throw new IntegrationEventConfigurationException("Integration event version must be positive: " + key);
        }

        return new IntegrationEventContract(key, configuredContract.eventType(), configuredContract.version());
    }

    private List<Class<?>> findIntegrationEventPayloadTypes() {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(IntegrationEvent.class));

        return scanner.findCandidateComponents(EVENT_PAYLOAD_PACKAGE)
                .stream()
                .map(this::loadClass)
                .sorted(Comparator.comparing(Class::getName))
                .toList();
    }

    private Class<?> loadClass(BeanDefinition beanDefinition) {
        var className = beanDefinition.getBeanClassName();
        if (className == null) {
            throw new IntegrationEventConfigurationException("Cannot resolve integration event payload class name");
        }
        try {
            return ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
        } catch (ClassNotFoundException exception) {
            throw new IntegrationEventConfigurationException("Cannot load integration event payload: " + className, exception);
        }
    }

    private void validateUniqueKey(Map<String, Class<?>> payloadTypesByKey, String key, Class<?> payloadType) {
        var existingPayloadType = payloadTypesByKey.putIfAbsent(key, payloadType);
        if (existingPayloadType != null) {
            throw new IntegrationEventConfigurationException(
                    "Duplicate integration event key '%s' on %s and %s".formatted(
                            key,
                            existingPayloadType.getName(),
                            payloadType.getName()
                    )
            );
        }
    }

    private void validateUniqueEventIdentity(Map<EventIdentity, Class<?>> payloadTypesByEventIdentity,
                                             IntegrationEventContract contract,
                                             Class<?> payloadType) {
        var eventIdentity = new EventIdentity(contract.eventType(), contract.version());
        var existingPayloadType = payloadTypesByEventIdentity.putIfAbsent(eventIdentity, payloadType);
        if (existingPayloadType != null) {
            throw new IntegrationEventConfigurationException(
                    "Duplicate integration event type/version '%s/%d' on %s and %s".formatted(
                            contract.eventType(),
                            contract.version(),
                            existingPayloadType.getName(),
                            payloadType.getName()
                    )
            );
        }
    }

    private Map<String, IntegrationEventProperties.ContractProperties> contracts() {
        if (properties.contracts() == null) {
            return Map.of();
        }
        return properties.contracts();
    }

    private record EventIdentity(
            String eventType,
            int version
    ) {
    }
}
