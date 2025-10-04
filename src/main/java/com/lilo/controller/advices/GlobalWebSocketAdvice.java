package com.lilo.controller.advices;

import com.lilo.shared.WebSocketConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalWebSocketAdvice {
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser("/topic/errors")
    public String handleValidationException(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    }

    @MessageExceptionHandler(MessageConversionException.class)
    @SendToUser(WebSocketConstants.QUEUE_ERRORS)
    public String handleConversionException(MessageConversionException ex/*, Authentication authentication*/) {
//        messagingTemplate.convertAndSendToUser(
//                authentication.getName(),              // The username of the connected client
//                WebSocketConstants.QUEUE_ERRORS, // "/queue/errors"
//                ex.getMessage()                   // The message body
//        );

        return ex.getMessage();
    }
}
