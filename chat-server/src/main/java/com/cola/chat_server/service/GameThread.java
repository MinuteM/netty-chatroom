package com.cola.chat_server.service;

import com.cola.chat_server.model.Animal;
import com.cola.chat_server.model.Game;

import java.util.Map;

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
