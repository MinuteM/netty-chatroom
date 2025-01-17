package com.cola.chat_server.model;

import lombok.Data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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

    /**
     * 炸弹位置
     */
    private List<Bomb> bombList = Collections.synchronizedList(new LinkedList<>());

    /**
     * 爆炸效果
     */
    private List<Bomb> boomList = Collections.synchronizedList(new LinkedList<>());

    /**
     * 死亡讯息
     */
    private String dead = "无人死亡";

    /**
     * 记分牌
     */
    private Map<String,Integer> recordList = new ConcurrentHashMap<>();
}
