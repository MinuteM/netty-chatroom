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

public class AnimalThread extends Thread {
    private Animal animal;

    private Game game;

    public AnimalThread(Game game, Animal animal) {
        this.animal = animal;
        this.game = game;
    }

    @Override
    public void run() {
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", animal.getMessage());
            jsonObject.put("code", MessageCodeConstant.GROUP_CHAT_CODE);
            jsonObject.put("username", "系统管理员");
            jsonObject.put("sendTime", DateUtil.now());
            jsonObject.put("game", game);
            SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(jsonObject)));
            while (animal.getMoveStatus()) {
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
                jsonObject.put("game", game);
                SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(jsonObject)));
            }
        }
    }
}
