package com.cola.chat_server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cola.chat_server.constant.MessageCodeConstant;
import com.cola.chat_server.model.Animal;
import com.cola.chat_server.model.Bomb;
import com.cola.chat_server.model.Game;
import com.cola.chat_server.model.Point;
import com.cola.chat_server.util.SessionHolder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class BombThread extends Thread {
    private Bomb bomb;

    public BombThread(Bomb bomb) {
        this.bomb = bomb;
    }

    @Override
    public void run() {
        {
            Game game = SessionHolder.game;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", StrUtil.format("{}施放了一个炸弹", bomb.getUserId()));
            jsonObject.put("code", MessageCodeConstant.GROUP_CHAT_CODE);
            jsonObject.put("username", "系统管理员");
            jsonObject.put("sendTime", DateUtil.now());
            jsonObject.put("game", game);
//            SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(jsonObject)));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            jsonObject.put("msg", StrUtil.format("{}的炸弹爆炸了", bomb.getUserId()));
            jsonObject.put("sendTime", DateUtil.now());
            game.setBombList(game.getBombList().stream().filter(item -> !item.getId().equals(bomb.getId())).collect(Collectors.toList()));
            jsonObject.put("game", game);
            SessionHolder.game = game;
            SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(jsonObject)));
            // 爆炸效果
            List<Bomb> boomList = game.getBoomList();
            boomList.addAll(createBoom(3, bomb, jsonObject));
            game.setBoomList(boomList);
            jsonObject.put("game", game);
            SessionHolder.game = game;
            SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(jsonObject)));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            game.setBoomList(game.getBoomList().stream().filter(item -> !item.getId().equals(bomb.getId())).collect(Collectors.toList()));
            jsonObject.put("game", game);
            SessionHolder.game = game;
            SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(jsonObject)));
        }
    }

    private List<Bomb> createBoom(Integer range, Bomb bomb, JSONObject jsonObject) {
        //同时判定死亡
        List<Bomb> bombList = new CopyOnWriteArrayList<>();
        for (int i = 1; i <= range; i++) {
            Bomb bomb1 = new Bomb();
            BeanUtil.copyProperties(bomb, bomb1);
            Point point1 = new Point();
            BeanUtil.copyProperties(bomb.getPoint(), point1);
            Integer num1 = point1.getLeft() - i;
            point1.setLeft(num1);
            bomb1.setPoint(point1);

            Bomb bomb2 = new Bomb();
            BeanUtil.copyProperties(bomb, bomb2);
            Point point2 = new Point();
            BeanUtil.copyProperties(bomb.getPoint(), point2);
            Integer num2 = point2.getLeft() + i;
            point2.setLeft(num2);
            bomb2.setPoint(point2);

            Bomb bomb3 = new Bomb();
            BeanUtil.copyProperties(bomb, bomb3);
            Point point3 = new Point();
            BeanUtil.copyProperties(bomb.getPoint(), point3);
            Integer num3 = point3.getBottom() - i;
            point3.setBottom(num3);
            bomb3.setPoint(point3);

            Bomb bomb4 = new Bomb();
            BeanUtil.copyProperties(bomb, bomb4);
            Point point4 = new Point();
            BeanUtil.copyProperties(bomb.getPoint(), point4);
            Integer num4 = point4.getBottom() + i;
            point4.setBottom(num4);
            bomb4.setPoint(point4);

            if(point1.getLeft() >=0 && point1.getLeft() <= SessionHolder.width && point1.getBottom() >= 0 && point1.getBottom() <= SessionHolder.height){
                bombList.add(bomb1);
            }
            if(point2.getLeft() >=0 && point2.getLeft() <= SessionHolder.width && point2.getBottom() >= 0 && point2.getBottom() <= SessionHolder.height){
                bombList.add(bomb2);
            }
            if(point3.getLeft() >=0 && point3.getLeft() <= SessionHolder.width && point3.getBottom() >= 0 && point3.getBottom() <= SessionHolder.height){
                bombList.add(bomb3);
            }
            if(point4.getLeft() >=0 && point4.getLeft() <= SessionHolder.width && point4.getBottom() >= 0 && point4.getBottom() <= SessionHolder.height){
                bombList.add(bomb4);
            }
        }
        bombList.add(bomb);
        Map<String, Animal> map = SessionHolder.game.getAnimalMap();
        for (Bomb bomb1 : bombList) {
            map.forEach((key, value) -> {
                if (value.getHLocation().equals(bomb1.getPoint().getBottom()) && value.getWLocation().equals(bomb1.getPoint().getLeft())) {
                    // 被炸到
                    Game game = SessionHolder.game;
                    game.setDead(StrUtil.format("{}被{}炸死了", key, bomb1.getUserId()));
                    jsonObject.put("game", game);
                    SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(jsonObject)));
                }
            });
        }
        return bombList;
    }
}
