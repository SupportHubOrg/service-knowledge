package com.supporthub.knowledge.messaging.inbox.core.handler;

import com.supporthub.knowledge.messaging.event.registry.IntegrationEventConfigurationException;
import com.supporthub.knowledge.messaging.event.registry.IntegrationEventContract;
import com.supporthub.knowledge.messaging.event.registry.IntegrationEventRegistry;
import com.supporthub.knowledge.messaging.inbox.core.annotation.InboxConsumer;
import com.supporthub.knowledge.messaging.inbox.core.annotation.InboxHandler;
import com.supporthub.knowledge.messaging.inbox.core.context.InboxEventContext;
import com.supporthub.knowledge.messaging.inbox.core.error.InvalidInboxHandlerMethodException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@RequiredArgsConstructor
public class InboxHandlerBeanPostProcessor implements BeanPostProcessor {

    private final InboxHandlerRegistry registry;
    private final ObjectProvider<IntegrationEventRegistry> integrationEventRegistry;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        var consumer = bean.getClass().getAnnotation(InboxConsumer.class);
        if (consumer == null) {
            return bean;
        }

        ReflectionUtils.doWithMethods(bean.getClass(), method -> register(bean, consumer.value(), method),
                method -> method.isAnnotationPresent(InboxHandler.class));
        return bean;
    }

    private void register(Object bean, String consumer, Method method) {
        validateReturnType(method);
        var parameterTypes = method.getParameterTypes();
        if (parameterTypes.length < 1 || parameterTypes.length > 2) {
            throw invalid(method, "must have one payload parameter and optional InboxEventContext");
        }

        var payloadType = parameterTypes[0];
        if (Object.class.equals(payloadType)) {
            throw invalid(method, "must declare a concrete payload type");
        }

        var contract = integrationEventContract(method, payloadType);

        var acceptsContext = false;
        if (parameterTypes.length == 2) {
            acceptsContext = InboxEventContext.class.equals(parameterTypes[1]);
            if (!acceptsContext) {
                throw invalid(method, "second parameter must be InboxEventContext");
            }
        }

        registry.register(new InboxHandlerMethod(
                new InboxHandlerKey(consumer, contract.eventType(), contract.version()),
                payloadType,
                bean,
                method,
                acceptsContext
        ));
    }

    private IntegrationEventContract integrationEventContract(Method method, Class<?> payloadType) {
        try {
            return integrationEventRegistry.getObject().get(payloadType);
        } catch (IntegrationEventConfigurationException exception) {
            throw invalid(method, exception.getMessage());
        }
    }

    private void validateReturnType(Method method) {
        if (!Void.TYPE.equals(method.getReturnType())) {
            throw invalid(method, "must return void");
        }
    }

    private InvalidInboxHandlerMethodException invalid(Method method, String reason) {
        return new InvalidInboxHandlerMethodException("Invalid @InboxHandler method " + method + ": " + reason);
    }
}
