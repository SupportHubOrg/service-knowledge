package com.supporthub.knowledge.messaging.inbox.core.handler;

import com.supporthub.knowledge.messaging.inbox.core.context.InboxEventContext;
import com.supporthub.knowledge.messaging.inbox.core.error.InboxHandlerInvocationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public record InboxHandlerMethod(
        InboxHandlerKey key,
        Class<?> payloadType,
        Object bean,
        Method method,
        boolean acceptsContext
) {

    public void invoke(Object payload, InboxEventContext context) {
        try {
            method.setAccessible(true);
            if (acceptsContext) {
                method.invoke(bean, payload, context);
                return;
            }
            method.invoke(bean, payload);
        } catch (InvocationTargetException exception) {
            var cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new InboxHandlerInvocationException("Inbox handler failed for " + key.eventType(), cause);
        } catch (IllegalAccessException exception) {
            throw new InboxHandlerInvocationException("Cannot invoke inbox handler for " + key.eventType(), exception);
        }
    }
}
