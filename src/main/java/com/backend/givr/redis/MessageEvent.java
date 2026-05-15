package com.backend.givr.redis;

import lombok.Data;

public class MessageEvent {
    public enum Event {Join, Message, Leave}
    @Data
    public static class Message{
        private String content;
        private String userId;
        private Integer projectId;
    }

    private Event type;
    private Message payload;
}
