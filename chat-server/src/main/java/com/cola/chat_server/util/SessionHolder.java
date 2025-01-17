package com.cola.chat_server.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cola.chat_server.model.Animal;
import com.cola.chat_server.model.Game;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * netty会话管理
 * @author 
 *
 */
public class SessionHolder {
	
    /**
     * 存储每个客户端接入进来时的 channel 对象
     * 主要用于使用 writeAndFlush 方法广播信息
     */
    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 用于客户端和服务端握手时存储用户id和netty Channel对应关系
     */
    public static Map<String, Channel> channelMap = new ConcurrentHashMap<String, Channel>();

    /**
     * 服务器上只有一局游戏，先作为缓存
     */
    public static Game game;

    /**
     * 地图长
     */
    public static Integer width = 49;

    /**
     * 地图长
     */
    public static Integer height = 29;

    /**
     * 炸弹爆炸时间
     */
    public static Integer bombTime = 3000;

    /**
     * 爆炸痕迹时间
     */
    public static Integer boomTime = 1000;

    /**
     * 炸弹威力
     */
    public static Integer power = 5;

    /**
     * 线程池
     */
    public static ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    /**
     * 最大炸弹数量
     */
    public static Integer maxBomb = 5;

}
