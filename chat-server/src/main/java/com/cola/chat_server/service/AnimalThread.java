package com.cola.chat_server.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSONObject;
import com.cola.chat_server.constant.MessageCodeConstant;
import com.cola.chat_server.model.Animal;
import com.cola.chat_server.model.Game;
import com.cola.chat_server.util.ActiveUtils;
import com.cola.chat_server.util.SessionHolder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnimalThread extends Thread {
    private Animal animal;

    private Game game;

    private String userId;

    public AnimalThread(Game game, Animal animal, String userId) {
        this.animal = animal;
        this.game = game;
        this.userId = userId;
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
                    Thread.sleep(100);
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
                try {
                    ReflectUtil.invoke(animal, method);
                } catch (UtilException e) {
                    // todo
                }
                jsonObject.put("msg", animal.getMessage());
                jsonObject.put("sendTime", DateUtil.now());
                jsonObject.put("game", game);
                SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(jsonObject)));
                // 随机施放炸弹
                Boolean m = random.nextInt(5) == 0;
                if (m) {
                    ActiveUtils.createBomb(userId, game, animal);
                }
            }
        }
    }
}
