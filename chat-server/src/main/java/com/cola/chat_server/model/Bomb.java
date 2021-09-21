package com.cola.chat_server.model;

import lombok.Data;

@Data
public class Bomb {
    /**
     * id
     */
    private Long id;
    /**
     * 点
     */
    private Point point;

    /**
     * userId
     */
    private String userId;
}
