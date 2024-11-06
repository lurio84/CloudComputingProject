package com.cloudComputing.collaborativeNotes.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import com.cloudComputing.collaborativeNotes.models.Message;

@Controller
public class WebSocketController {

    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public Message handleMessage(Message message) {
        // Aquí puedes procesar el mensaje antes de enviarlo a los suscriptores
        return message;
    }
}