package com.cola.chat_server.model;

import lombok.Data;

@Data
public class Game {

    /**
     * 游戏状态（运行中、暂停）
     */
    private Boolean status;

    /**
     * 玩家id
     */
    private String userId;

    /**
     * 动物
     */
    private Animal animal;

    /**
     * 地图
     */
    private Map map;
}
