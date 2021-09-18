package com.cola.chat_server.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSONObject;
import com.cola.chat_server.constant.MessageCodeConstant;
import com.cola.chat_server.model.Animal;
import com.cola.chat_server.model.Game;
import com.cola.chat_server.util.SessionHolder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameThread extends Thread {
    private Game game;

    public GameThread(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        {
            JSONObject jsonObject = new JSONObject();
            Animal animal = game.getAnimal();
            jsonObject.put("msg", animal.getMessage());
            jsonObject.put("code", MessageCodeConstant.GROUP_CHAT_CODE);
            jsonObject.put("username", "系统管理员");
            jsonObject.put("sendTime", DateUtil.now());
            jsonObject.put("left", animal.getWLocation());
            jsonObject.put("bottom", animal.getHLocation());
            SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(jsonObject)));
            while (game.getStatus()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                List<String> methodList = new ArrayList<>();
                methodList.add("moveDown");
                methodList.add("moveLeft");
                methodList.add("moveRight");
                methodList.add("moveUp");
                Random random = new Random();
                int n = random.nextInt(methodList.size());
                String method = methodList.get(n);
                ReflectUtil.invoke(animal, method);
                jsonObject.put("msg", animal.getMessage());
                jsonObject.put("sendTime", DateUtil.now());
                jsonObject.put("left", animal.getWLocation());
                jsonObject.put("bottom", animal.getHLocation());
                SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(jsonObject)));
            }
        }
    }
}
