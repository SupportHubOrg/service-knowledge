package com.supporthub.knowledge.messaging.inbox.core.handler;

import com.supporthub.knowledge.messaging.inbox.core.error.DuplicateInboxHandlerException;
import com.supporthub.knowledge.messaging.inbox.core.error.InboxHandlerNotFoundException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class InboxHandlerRegistry {

    private final Map<InboxHandlerKey, InboxHandlerMethod> handlers = new ConcurrentHashMap<>();

    public void register(InboxHandlerMethod handlerMethod) {
        var existing = handlers.putIfAbsent(handlerMethod.key(), handlerMethod);
        if (existing != null) {
            throw new DuplicateInboxHandlerException("Duplicate inbox handler for " + handlerMethod.key());
        }
    }

    public InboxHandlerMethod get(InboxHandlerKey key) {
        var handler = handlers.get(key);
        if (handler == null) {
            throw new InboxHandlerNotFoundException("No inbox handler registered for " + key);
        }
        return handler;
    }
}
