package com.cola.chat_server.model;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Game {

//    /**
//     * 游戏状态（运行中、暂停）
//     */
//    private Boolean status;

    /**
     * 动物
     */
    private Map<String,Animal> animalMap = new ConcurrentHashMap<>();

    /**
     * 地图
     */
    private GameMap map;
}
