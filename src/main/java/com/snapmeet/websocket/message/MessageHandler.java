package com.snapmeet.websocket.message;

import com.snapmeet.entity.dto.MessageSendDto;
import org.springframework.stereotype.Component;

@Component("MessageHandler")
public interface MessageHandler {
    void listenMessage();
    void sendMessage(MessageSendDto messageSendDto);
}
