package com.cola.chat_server.model;

import com.cola.chat_server.util.SessionHolder;
import lombok.Data;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
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
    private List<Point> pointList = Collections.synchronizedList(new LinkedList<>());

    /**
     * 固定障碍物
     */
    private List<Point> fixPointList = new CopyOnWriteArrayList<>();

    /**
     * 土块碎片
     */
    private List<Point> patchList = Collections.synchronizedList(new LinkedList<>());

    public GameMap() {
        this.width = SessionHolder.width;
        this.height = SessionHolder.height;
        for (int i = 0; i < this.width + 2; i++) {
            Point point = new Point();
            point.setBottom(this.height + 1);
            point.setLeft(i);
            this.fixPointList.add(point);
        }
        for (int i = 0; i < this.height + 1; i++) {
            Point point = new Point();
            point.setBottom(i);
            point.setLeft(this.width + 1);
            this.fixPointList.add(point);
        }
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                Random random = new Random();
                int flag = random.nextInt(20);
                if (flag == 0) {
                    Point point = new Point();
                    point.setLeft(i);
                    point.setBottom(j);
                    this.pointList.add(point);
                }
            }
        }
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                Random random = new Random();
                int flag = random.nextInt(20);
                if (flag == 0) {
                    Point point = new Point();
                    point.setLeft(i);
                    point.setBottom(j);
                    if(!this.getPointList().contains(point)){
                        this.fixPointList.add(point);
                    }
                }
            }
        }
    }

}
