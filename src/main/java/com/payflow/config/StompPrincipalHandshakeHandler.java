package com.payflow.config;

import org.springframework.lang.NonNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class StompPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(@NonNull ServerHttpRequest request, @NonNull WebSocketHandler wsHandler,
                                       @NonNull Map<String, Object> attributes) {
        String username = (String) attributes.get("username");
        return () -> username != null ? username : "anonymous";
    }
}