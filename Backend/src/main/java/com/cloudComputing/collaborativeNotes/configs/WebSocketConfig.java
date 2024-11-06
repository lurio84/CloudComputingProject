package com.cloudComputing.collaborativeNotes.configs;

import com.cloudComputing.collaborativeNotes.handlers.SimpleTextWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SimpleTextWebSocketHandler(), "/ws")
                .setAllowedOrigins("*"); // Permitimos todas las conexiones para pruebas
    }
}

