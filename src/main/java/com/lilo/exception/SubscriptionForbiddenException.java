package com.lilo.exception;

import lombok.Getter;
import org.springframework.messaging.MessageDeliveryException;
public class SubscriptionForbiddenException extends MessageDeliveryException {


    public SubscriptionForbiddenException(String description) {
        super(description);
    }
}