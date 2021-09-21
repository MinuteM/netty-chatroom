package com.cola.chat_server.model;

import lombok.Data;

import java.util.List;

/**
 * 地图对象
 */
@Data
public class GameMap {

    private Integer width;

    private Integer height;

    /**
     * 障碍物
     */
    private List<Point> pointList;

}
