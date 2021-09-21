package com.cola.chat_server.model;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.cola.chat_server.constant.CommonUtils;
import com.cola.chat_server.util.SessionHolder;
import lombok.Data;

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
        this.wLocation = 0;
        this.hLocation = 0;
        this.moveStatus = true;
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
     * 向上移动一个单位
     */
    public void moveUp() {
        if (this.hLocation == SessionHolder.height) {
            this.message = StrUtil.format("蚂蚁：{}，触碰边界无法移动，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
            return;
        }
        Integer left = this.wLocation;
        Integer bottom = this.hLocation;
        if (SessionHolder.game.getMap().getPointList().stream().filter(item -> item.getLeft().equals(left) && item.getBottom().equals(bottom + 1)).count() > 0) {
            return;
        }
        this.hLocation++;
        this.message = StrUtil.format("蚂蚁：{}，向上移动一个单位，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
    }

    /**
     * 向左移动一个单位
     */
    public void moveLeft() {
        if (this.wLocation == 0) {
            this.message = StrUtil.format("蚂蚁：{}，触碰边界无法移动，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
            return;
        }
        Integer left = this.wLocation;
        Integer bottom = this.hLocation;
        if (SessionHolder.game.getMap().getPointList().stream().filter(item -> item.getLeft().equals(left - 1) && item.getBottom().equals(bottom)).count() > 0) {
            return;
        }
        this.wLocation--;
        this.message = StrUtil.format("蚂蚁：{}，向左移动一个单位，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
    }

    /**
     * 向右移动一个单位
     */
    public void moveRight() {
        if (this.wLocation == SessionHolder.width) {
            this.message = StrUtil.format("蚂蚁：{}，触碰边界无法移动，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
            return;
        }
        Integer left = this.wLocation;
        Integer bottom = this.hLocation;
        if (SessionHolder.game.getMap().getPointList().stream().filter(item -> item.getLeft().equals(left + 1) && item.getBottom().equals(bottom)).count() > 0) {
            return;
        }
        this.wLocation++;
        this.message = StrUtil.format("蚂蚁：{}，向右移动一个单位，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
    }

    /**
     * 向下移动一个单位
     */
    public void moveDown() {
        if (this.hLocation == 0) {
            this.message = StrUtil.format("蚂蚁：{}，触碰边界无法移动，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
            return;
        }
        Integer left = this.wLocation;
        Integer bottom = this.hLocation;
        if (SessionHolder.game.getMap().getPointList().stream().filter(item -> item.getLeft().equals(left) && item.getBottom().equals(bottom - 1)).count() > 0) {
            return;
        }
        this.hLocation--;
        this.message = StrUtil.format("蚂蚁：{}，向下移动一个单位，当前位置({},{})", this.getName(), String.valueOf(this.getWLocation()), String.valueOf(this.getHLocation()));
    }
}

