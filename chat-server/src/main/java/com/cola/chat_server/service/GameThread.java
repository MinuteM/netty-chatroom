package com.cola.chat_server.service;

import cn.hutool.core.util.ObjectUtil;
import com.cola.chat_server.model.Animal;
import com.cola.chat_server.model.Game;
import com.cola.chat_server.util.SessionHolder;

import java.util.Map;

public class GameThread extends Thread {
    private Game game;

    public GameThread(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        {
            Map<String, Animal> animalMap = null;
            for (; ; ) {
                if (ObjectUtil.isNotEmpty(game)) {
                    animalMap = game.getAnimalMap();
                    break;
                }
            }
            animalMap.forEach(
                    (id, ant) -> {
                        SessionHolder.cachedThreadPool.execute(new AnimalThread(game, ant, id));
                    }
            );
        }
    }
}
