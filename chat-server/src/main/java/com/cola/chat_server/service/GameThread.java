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
import java.util.Map;
import java.util.Random;

public class GameThread extends Thread {
    private Game game;

    public GameThread(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        {
            Map<String, Animal> animalMap = game.getAnimalMap();
            animalMap.forEach(
                    (id, ant) -> {
                        Thread thread = new AnimalThread(game, ant);
                        thread.start();
                    }
            );
        }
    }
}
