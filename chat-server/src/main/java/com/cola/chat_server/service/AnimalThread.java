package com.cola.chat_server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSONObject;
import com.cola.chat_server.constant.MessageCodeConstant;
import com.cola.chat_server.model.Animal;
import com.cola.chat_server.model.Bomb;
import com.cola.chat_server.model.Game;
import com.cola.chat_server.model.Point;
import com.cola.chat_server.util.ActiveUtils;
import com.cola.chat_server.util.SessionHolder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 如果在炸弹的爆炸范围内要躲开
                // 获取未来有可能的四个位置
                // 当前位置
                Integer x = animal.getWLocation();
                Integer y = animal.getHLocation();
                Map<String, Point> fourPointList = new HashMap<>();
                Point point = new Point();
                point.setLeft(x);
                point.setBottom(y - 1);
                fourPointList.put("moveDown", point);

                Point point1 = new Point();
                point1.setLeft(x);
                point1.setBottom(y + 1);
                fourPointList.put("moveUp", point1);

                Point point2 = new Point();
                point2.setLeft(x - 1);
                point2.setBottom(y);
                fourPointList.put("moveLeft", point2);

                Point point3 = new Point();
                point3.setLeft(x + 1);
                point3.setBottom(y);
                fourPointList.put("moveRight", point3);

                List<String> methodList = new ArrayList<>();
                methodList.add("moveDown");
                methodList.add("moveLeft");
                methodList.add("moveRight");
                methodList.add("moveUp");

                List<Bomb> boomList = new LinkedList<>();

                boomList.addAll(SessionHolder.game.getBoomList());

                List<Point> pointList = SessionHolder.game.getMap().getPointList();

                List<Point> fixPointList = SessionHolder.game.getMap().getFixPointList();

                try {
                    // 预判炸弹未来爆炸point
                    game.getBombList().forEach(
                            item -> {
                                Boolean up = true;
                                Boolean down = true;
                                Boolean left = true;
                                Boolean right = true;
                                Integer range = SessionHolder.power;
                                for (int i = 1; i <= range; i++) {
                                    if (left) {
                                        Point p1 = new Point();
                                        Bomb bomb1 = new Bomb();
                                        bomb1.setPoint(p1);
                                        p1.setLeft(item.getPoint().getLeft() - 1);
                                        p1.setBottom(item.getPoint().getBottom());
                                        if (p1.getLeft() >= 0 && p1.getLeft() <= SessionHolder.width && p1.getBottom() >= 0 && p1.getBottom() <= SessionHolder.height) {
                                            boomList.add(bomb1);
                                        }
                                        if (pointList.contains(p1)) {
                                            left = false;
                                            boomList.remove(bomb1);
                                        }
                                        if (fixPointList.contains(p1)) {
                                            left = false;
                                            boomList.remove(bomb1);
                                        }
                                    }

                                    if (right) {
                                        Point p2 = new Point();
                                        Bomb bomb2 = new Bomb();
                                        bomb2.setPoint(p2);
                                        p2.setLeft(item.getPoint().getLeft() + 1);
                                        p2.setBottom(item.getPoint().getBottom());
                                        if (p2.getLeft() >= 0 && p2.getLeft() <= SessionHolder.width && p2.getBottom() >= 0 && p2.getBottom() <= SessionHolder.height) {
                                            boomList.add(bomb2);
                                        }
                                        if (pointList.contains(p2)) {
                                            right = false;
                                            boomList.remove(bomb2);
                                        }
                                        if (fixPointList.contains(p2)) {
                                            right = false;
                                            boomList.remove(bomb2);
                                        }
                                    }

                                    if (down) {
                                        Point p3 = new Point();
                                        Bomb bomb3 = new Bomb();
                                        bomb3.setPoint(p3);
                                        p3.setLeft(item.getPoint().getLeft());
                                        p3.setBottom(item.getPoint().getBottom() - 1);
                                        if (p3.getLeft() >= 0 && p3.getLeft() <= SessionHolder.width && p3.getBottom() >= 0 && p3.getBottom() <= SessionHolder.height) {
                                            boomList.add(bomb3);
                                        }
                                        if (pointList.contains(p3)) {
                                            down = false;
                                            boomList.remove(bomb3);
                                        }
                                        if (fixPointList.contains(p3)) {
                                            down = false;
                                            boomList.remove(bomb3);
                                        }
                                    }

                                    if (up) {
                                        Point p4 = new Point();
                                        Bomb bomb4 = new Bomb();
                                        bomb4.setPoint(p4);
                                        p4.setLeft(item.getPoint().getLeft());
                                        p4.setBottom(item.getPoint().getBottom() + 1);
                                        if (p4.getLeft() >= 0 && p4.getLeft() <= SessionHolder.width && p4.getBottom() >= 0 && p4.getBottom() <= SessionHolder.height) {
                                            boomList.add(bomb4);
                                        }
                                        if (pointList.contains(p4)) {
                                            up = false;
                                            boomList.remove(bomb4);
                                        }
                                        if (fixPointList.contains(p4)) {
                                            up = false;
                                            boomList.remove(bomb4);
                                        }
                                    }
                                }
                            }
                    );
                } catch (ConcurrentModificationException e) {
                    // todo
                }

                try {
                    boomList.forEach(
                            item -> {
                                fourPointList.forEach(
                                        (method, fourPoint) -> {
                                            if (item.getPoint().equals(fourPoint)) {
                                                methodList.remove(method);
                                            }
                                        }
                                );
                            }
                    );
                } catch (ConcurrentModificationException e) {
                    // todo
                }

                try {
                    SessionHolder.game.getMap().getPointList().forEach(
                            item -> {
                                fourPointList.forEach(
                                        (method, fourPoint) -> {
                                            if (item.equals(fourPoint)) {
                                                methodList.remove(method);
                                            }
                                        }
                                );
                            }
                    );
                } catch (ConcurrentModificationException e) {
                    // todo
                }

                try {
                    SessionHolder.game.getMap().getFixPointList().forEach(
                            item -> {
                                fourPointList.forEach(
                                        (method, fourPoint) -> {
                                            if (item.equals(fourPoint)) {
                                                methodList.remove(method);
                                            }
                                        }
                                );
                            }
                    );
                } catch (ConcurrentModificationException e) {
                    // todo
                }

                fourPointList.forEach(
                        (method, fourPoint) -> {
                            if (fourPoint.getLeft() < 0 || fourPoint.getBottom() < 0) {
                                methodList.remove(method);
                            }
                        }
                );

                if (methodList.size() == 0) {
                    methodList.add("moveDown");
                    methodList.add("moveLeft");
                    methodList.add("moveRight");
                    methodList.add("moveUp");
                }

                Random random = new Random();
                if (methodList.size() > 0) {
                    int n = random.nextInt(methodList.size());
                    String method = methodList.get(n);
                    try {
                        ReflectUtil.invoke(animal, method);
                    } catch (UtilException e) {
                        // todo
                    }
                }

                jsonObject.put("msg", animal.getMessage());
                jsonObject.put("sendTime", DateUtil.now());
                jsonObject.put("game", game);
                SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(jsonObject)));
                // 随机施放炸弹，没移动不放炸弹
                Boolean m = random.nextInt(5) == 0;
                if (m && methodList.size() > 0) {
                    ActiveUtils.createBomb(userId, game, animal);
                }
                // 随机放墙
                Boolean n = random.nextInt(5) == 0;
                List<String> methodList1 = new ArrayList<>();
                methodList1.add("downWall");
                methodList1.add("leftWall");
                methodList1.add("rightWall");
                methodList1.add("upWall");

                if (n && methodList.size() > 0) {
                    int k = random.nextInt(methodList1.size());
                    String method = methodList1.get(k);
                    jsonObject.put("game", game);
                    SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(jsonObject)));
                    try {
                        ReflectUtil.invoke(animal, method);
                    } catch (UtilException e) {
                        // todo
                    }
                }
            }
        }
    }
}
