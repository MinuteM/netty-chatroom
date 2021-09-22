package com.cola.chat_server.util;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.cola.chat_server.constant.MessageCodeConstant;
import com.cola.chat_server.model.*;
import com.cola.chat_server.service.BombThread;
import com.cola.chat_server.service.GameThread;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class ActiveUtils {

    public synchronized static void createBomb(String myUserId, Game game, Animal animal) {
        if (animal.getLastBomb() > 0) {
            Point point = new Point();
            point.setLeft(animal.getWLocation());
            point.setBottom(animal.getHLocation());
            Bomb bomb = new Bomb();
            bomb.setId(IdWorker.getId());
            bomb.setPoint(point);
            bomb.setUserId(myUserId);
            game.getBombList().add(bomb);
            animal.setLastBomb(animal.getLastBomb() - 1);
            SessionHolder.cachedThreadPool.execute(new BombThread(bomb));
            sendMessage(myUserId, game);
        }
    }

    public synchronized static void sendMessage(String myUserId, Game game) {
        Animal animal = game.getAnimalMap().get(myUserId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", animal.getMessage());
        jsonObject.put("code", MessageCodeConstant.GROUP_CHAT_CODE);
        jsonObject.put("username", "系统管理员");
        jsonObject.put("sendTime", DateUtil.now());
        jsonObject.put("game", game);
//        log.info(JSONUtil.toJsonStr(game));
        SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(jsonObject)));
        SessionHolder.game.getAnimalMap().put(myUserId, animal);
    }

    public synchronized static void createGame(String myUserId) {
        Game game = new Game();
        SessionHolder.game = game;
        GameMap gameMap = new GameMap();
        game.setMap(gameMap);
        Animal animal = new Animal();
        game.getAnimalMap().put(myUserId, animal);
        game.getRecordList().put(myUserId, 0);
        SessionHolder.cachedThreadPool.execute(new GameThread(game));
    }

}
