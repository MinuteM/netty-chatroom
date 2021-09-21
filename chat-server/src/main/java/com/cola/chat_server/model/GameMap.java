package com.cola.chat_server.model;

import com.cola.chat_server.util.SessionHolder;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private List<Point> pointList = new CopyOnWriteArrayList<>();

    public GameMap() {
        this.width = SessionHolder.width;
        this.height = SessionHolder.height;
        for (int i = 0; i < this.width + 2; i++) {
            Point point = new Point();
            point.setBottom(this.height + 1);
            point.setLeft(i);
            this.pointList.add(point);
        }
        for (int i = 0; i < this.height + 1; i++) {
            Point point = new Point();
            point.setBottom(i);
            point.setLeft(this.width + 1);
            this.pointList.add(point);
        }
        for (int i = 10; i < 30 + 1; i++) {
            Point point = new Point();
            point.setBottom(20);
            point.setLeft(i);
            this.pointList.add(point);
        }

        for (int i = 10; i < 30 + 1; i++) {
            Point point = new Point();
            point.setBottom(10);
            point.setLeft(i);
            this.pointList.add(point);
        }
    }

}
