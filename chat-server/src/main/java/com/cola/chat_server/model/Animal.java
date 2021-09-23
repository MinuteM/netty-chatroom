package com.cola.chat_server.model;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.cola.chat_server.constant.CommonUtils;
import com.cola.chat_server.util.SessionHolder;
import lombok.Data;

import java.util.List;
import java.util.Random;

/**
 * 游戏中所有生物的父类
 */
@Data
public class Animal {

    public Animal() {
        this.name = CommonUtils.getNewName();
        this.age = 0;
        this.status = "0";
        this.gender = String.valueOf(CommonUtils.randomNum(0, 1));
        this.id = IdWorker.getId();
        this.lastBomb = SessionHolder.maxBomb;
        // 随机位置
        Random random = new Random();
        Integer x = random.nextInt(SessionHolder.width);
        Integer y = random.nextInt(SessionHolder.height);
        for (; ; ) {
            Boolean flag = false;
            for (Point point : SessionHolder.game.getMap().getPointList()) {
                if (point.getLeft().equals(x) && point.getBottom().equals(y)) {
                    flag = true;
                    break;
                }
            }
            for (Point point : SessionHolder.game.getMap().getFixPointList()) {
                if (point.getLeft().equals(x) && point.getBottom().equals(y)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                break;
            } else {
                x = random.nextInt(SessionHolder.width);
                y = random.nextInt(SessionHolder.height);
            }
        }
        this.wLocation = x;
        this.hLocation = y;
        this.moveStatus = true;
        this.patchNum = 0;
        this.message = StrUtil.format("自动生成一只蚂蚁：{}，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
    }

    private Long id;

    /***
     * 父亲
     */
    private Long dadId;

    /**
     * 母亲
     */
    private Long mumId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 饥饿值
     */
    private Integer hungerValue;

    /**
     * 战斗力
     */
    private Integer combatEffectiveness;

    /**
     * 精力
     */
    private Integer energy;

    /**
     * 状态（0-睡觉、1-活动、2-死亡）
     */
    private String status;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 性别（0-雌性，1-雄性）
     */
    private String gender;

    /**
     * 种族
     */
    private String race;

    /**
     * 横轴位置
     */
    private Integer wLocation;

    /**
     * 消息
     */
    private String message;

    /**
     * 纵轴位置
     */
    private Integer hLocation;

    /**
     * 自动移动
     */
    private Boolean moveStatus;

    /**
     * 剩余炸弹数
     */
    private Integer lastBomb;

    /**
     * 碎片数量
     */
    private Integer patchNum;

    /**
     * 向上移动一个单位
     */
    public synchronized void moveUp() {
        if (this.hLocation.equals(SessionHolder.height)) {
            this.message = StrUtil.format("蚂蚁：{}，触碰边界无法移动，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
            return;
        }
        Integer left = this.wLocation;
        Integer bottom = this.hLocation;
        if (SessionHolder.game.getMap().getPointList().stream().filter(item -> item.getLeft().equals(left) && item.getBottom().equals(bottom + 1)).count() > 0) {
            return;
        }
        if (SessionHolder.game.getMap().getFixPointList().stream().filter(item -> item.getLeft().equals(left) && item.getBottom().equals(bottom + 1)).count() > 0) {
            return;
        }
        if (SessionHolder.game.getBombList().stream().filter(item -> item.getPoint().getLeft().equals(left) && item.getPoint().getBottom().equals(bottom + 1)).count() > 0) {
            return;
        }
        this.hLocation++;
        List<Point> patchList = SessionHolder.game.getMap().getPatchList();
        Point point = new Point();
        point.setLeft(this.wLocation);
        point.setBottom(this.hLocation);
        if (patchList.contains(point)) {
            patchList.remove(point);
            this.patchNum++;
        }
        this.message = StrUtil.format("蚂蚁：{}，向上移动一个单位，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
    }

    /**
     * 向左移动一个单位
     */
    public synchronized void moveLeft() {
        if (this.wLocation == 0) {
            this.message = StrUtil.format("蚂蚁：{}，触碰边界无法移动，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
            return;
        }
        Integer left = this.wLocation;
        Integer bottom = this.hLocation;
        if (SessionHolder.game.getMap().getPointList().stream().filter(item -> item.getLeft().equals(left - 1) && item.getBottom().equals(bottom)).count() > 0) {
            return;
        }
        if (SessionHolder.game.getMap().getFixPointList().stream().filter(item -> item.getLeft().equals(left - 1) && item.getBottom().equals(bottom)).count() > 0) {
            return;
        }
        if (SessionHolder.game.getBombList().stream().filter(item -> item.getPoint().getLeft().equals(left - 1) && item.getPoint().getBottom().equals(bottom)).count() > 0) {
            return;
        }
        this.wLocation--;
        List<Point> patchList = SessionHolder.game.getMap().getPatchList();
        Point point = new Point();
        point.setLeft(this.wLocation);
        point.setBottom(this.hLocation);
        if (patchList.contains(point)) {
            patchList.remove(point);
            this.patchNum++;
        }
        this.message = StrUtil.format("蚂蚁：{}，向左移动一个单位，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
    }

    /**
     * 向右移动一个单位
     */
    public synchronized void moveRight() {
        if (this.wLocation.equals(SessionHolder.width)) {
            this.message = StrUtil.format("蚂蚁：{}，触碰边界无法移动，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
            return;
        }
        Integer left = this.wLocation;
        Integer bottom = this.hLocation;
        if (SessionHolder.game.getMap().getPointList().stream().filter(item -> item.getLeft().equals(left + 1) && item.getBottom().equals(bottom)).count() > 0) {
            return;
        }
        if (SessionHolder.game.getMap().getFixPointList().stream().filter(item -> item.getLeft().equals(left + 1) && item.getBottom().equals(bottom)).count() > 0) {
            return;
        }
        if (SessionHolder.game.getBombList().stream().filter(item -> item.getPoint().getLeft().equals(left + 1) && item.getPoint().getBottom().equals(bottom)).count() > 0) {
            return;
        }
        this.wLocation++;
        List<Point> patchList = SessionHolder.game.getMap().getPatchList();
        Point point = new Point();
        point.setLeft(this.wLocation);
        point.setBottom(this.hLocation);
        if (patchList.contains(point)) {
            patchList.remove(point);
            this.patchNum++;
        }
        this.message = StrUtil.format("蚂蚁：{}，向右移动一个单位，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
    }

    /**
     * 向下移动一个单位
     */
    public synchronized void moveDown() {
        if (this.hLocation == 0) {
            this.message = StrUtil.format("蚂蚁：{}，触碰边界无法移动，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
            return;
        }
        Integer left = this.wLocation;
        Integer bottom = this.hLocation;
        if (SessionHolder.game.getMap().getPointList().stream().filter(item -> item.getLeft().equals(left) && item.getBottom().equals(bottom - 1)).count() > 0) {
            return;
        }
        if (SessionHolder.game.getMap().getFixPointList().stream().filter(item -> item.getLeft().equals(left) && item.getBottom().equals(bottom - 1)).count() > 0) {
            return;
        }
        if (SessionHolder.game.getBombList().stream().filter(item -> item.getPoint().getLeft().equals(left) && item.getPoint().getBottom().equals(bottom - 1)).count() > 0) {
            return;
        }
        this.hLocation--;
        List<Point> patchList = SessionHolder.game.getMap().getPatchList();
        Point point = new Point();
        point.setLeft(this.wLocation);
        point.setBottom(this.hLocation);
        if (patchList.contains(point)) {
            patchList.remove(point);
            this.patchNum++;
        }
        this.message = StrUtil.format("蚂蚁：{}，向下移动一个单位，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
    }

    /**
     * 在左边放墙
     */
    public synchronized void leftWall() {
        //原来没有墙，没有固定墙，没有碎片，没有蚂蚁，没有炸弹
        Point point = new Point();
        point.setLeft(this.wLocation - 1);
        point.setBottom(this.hLocation);
        if (this.patchNum < 1) {
            return;
        }
        if (point.getLeft() < 0 || point.getBottom() < 0) {
            return;
        }
        if (SessionHolder.game.getMap().getPatchList().contains(point)) {
            return;
        }
        if (SessionHolder.game.getMap().getFixPointList().contains(point)) {
            return;
        }
        if (SessionHolder.game.getMap().getPointList().contains(point)) {
            return;
        }
        SessionHolder.game.getAnimalMap().forEach(
                (key, value) -> {
                    if (value.getWLocation().equals(this.wLocation) && value.getHLocation().equals(this.hLocation)) {
                        return;
                    }
                }
        );
        SessionHolder.game.getBombList().forEach(
                item -> {
                    if (item.getPoint().equals(point)) {
                        return;
                    }
                }
        );
        SessionHolder.game.getMap().getPointList().add(point);
        this.patchNum--;
    }

    /**
     * 在右边放墙
     */
    public synchronized void rightWall() {
        //原来没有墙，没有固定墙，没有碎片，没有蚂蚁，没有炸弹
        Point point = new Point();
        point.setLeft(this.wLocation + 1);
        point.setBottom(this.hLocation);
        if (this.patchNum < 1) {
            return;
        }
        if (point.getLeft() < 0 || point.getBottom() < 0) {
            return;
        }
        if (SessionHolder.game.getMap().getPatchList().contains(point)) {
            return;
        }
        if (SessionHolder.game.getMap().getFixPointList().contains(point)) {
            return;
        }
        if (SessionHolder.game.getMap().getPointList().contains(point)) {
            return;
        }
        SessionHolder.game.getAnimalMap().forEach(
                (key, value) -> {
                    if (value.getWLocation().equals(this.wLocation) && value.getHLocation().equals(this.hLocation)) {
                        return;
                    }
                }
        );
        SessionHolder.game.getBombList().forEach(
                item -> {
                    if (item.getPoint().equals(point)) {
                        return;
                    }
                }
        );
        SessionHolder.game.getMap().getPointList().add(point);
        this.patchNum--;
    }

    /**
     * 在上边放墙
     */
    public synchronized void upWall() {
        //原来没有墙，没有固定墙，没有碎片，没有蚂蚁，没有炸弹
        Point point = new Point();
        point.setLeft(this.wLocation);
        point.setBottom(this.hLocation + 1);
        if (this.patchNum < 1) {
            return;
        }
        if (point.getLeft() < 0 || point.getBottom() < 0) {
            return;
        }
        if (SessionHolder.game.getMap().getPatchList().contains(point)) {
            return;
        }
        if (SessionHolder.game.getMap().getFixPointList().contains(point)) {
            return;
        }
        if (SessionHolder.game.getMap().getPointList().contains(point)) {
            return;
        }
        SessionHolder.game.getAnimalMap().forEach(
                (key, value) -> {
                    if (value.getWLocation().equals(this.wLocation) && value.getHLocation().equals(this.hLocation)) {
                        return;
                    }
                }
        );
        SessionHolder.game.getBombList().forEach(
                item -> {
                    if (item.getPoint().equals(point)) {
                        return;
                    }
                }
        );
        SessionHolder.game.getMap().getPointList().add(point);
        this.patchNum--;
    }

    /**
     * 在下边放墙
     */
    public synchronized void downWall() {
        //原来没有墙，没有固定墙，没有碎片，没有蚂蚁，没有炸弹
        Point point = new Point();
        point.setLeft(this.wLocation);
        point.setBottom(this.hLocation - 1);
        if (this.patchNum < 1) {
            return;
        }
        if (point.getLeft() < 0 || point.getBottom() < 0) {
            return;
        }
        if (SessionHolder.game.getMap().getPatchList().contains(point)) {
            return;
        }
        if (SessionHolder.game.getMap().getFixPointList().contains(point)) {
            return;
        }
        if (SessionHolder.game.getMap().getPointList().contains(point)) {
            return;
        }
        SessionHolder.game.getAnimalMap().forEach(
                (key, value) -> {
                    if (value.getWLocation().equals(this.wLocation) && value.getHLocation().equals(this.hLocation)) {
                        return;
                    }
                }
        );
        SessionHolder.game.getBombList().forEach(
                item -> {
                    if (item.getPoint().equals(point)) {
                        return;
                    }
                }
        );
        SessionHolder.game.getMap().getPointList().add(point);
        this.patchNum--;
    }
}

