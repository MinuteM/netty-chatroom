package com.cola.chat_server.handler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.cola.chat_server.model.*;
import com.cola.chat_server.service.BombThread;
import com.cola.chat_server.service.GameThread;
import com.cola.chat_server.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.cola.chat_server.constant.MessageCodeConstant;
import com.cola.chat_server.constant.MessageTypeConstant;
import com.cola.chat_server.constant.WebSocketConstant;
import com.cola.chat_server.service.WebSocketInfoService;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;


/**
 * Netty ChannelHandler，用来处理客户端和服务端的会话生命周期事件（握手、建立连接、断开连接、收消息等）
 *
 * @Author
 * @Description 接收请求，接收 WebSocket 信息的控制类
 */
@Slf4j
public class WebSocketSimpleChannelInboundHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketSimpleChannelInboundHandler.class);
    // WebSocket 握手工厂类
    private WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(WebSocketConstant.WEB_SOCKET_URL, null, false);
    private WebSocketServerHandshaker handshaker;
    private WebSocketInfoService websocketInfoService = new WebSocketInfoService();

    /**
     * 处理客户端与服务端之间的 websocket 业务
     */
    private void handWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        //判断是否是关闭 websocket 的指令
        if (frame instanceof CloseWebSocketFrame) {
            //关闭握手
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            websocketInfoService.clearSession(ctx.channel());
            return;
        }
        //判断是否是ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 判断是否Pong消息
        if (frame instanceof PongWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        //判断是否是二进制消息，如果是二进制消息，抛出异常
        if (!(frame instanceof TextWebSocketFrame)) {
            System.out.println("目前我们不支持二进制消息");
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            throw new RuntimeException("【" + this.getClass().getName() + "】不支持消息");
        }
        // 获取并解析客户端向服务端发送的 json 消息
        String message = ((TextWebSocketFrame) frame).text();
        logger.info("消息：{}", message);
        JSONObject json = JSONObject.parseObject(message);
        try {
            String uuid = UUID.randomUUID().toString();
            String time = DateUtils.date2String(new Date(), "yyyy-MM-dd HH:mm:ss");
            json.put("id", uuid);
            json.put("sendTime", time);

            int code = json.getIntValue("code");
            switch (code) {
                //群聊
                case MessageCodeConstant.GROUP_CHAT_CODE:
                    //向连接上来的客户端广播消息
                    SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(json)));
                    String myUserId = json.getString("sendUserId");
                    Game game = SessionHolder.game;
                    Animal animal = game.getAnimalMap().get(myUserId);
                    if ("pause".equals(json.getString("msg"))) {
                        animal.setMoveStatus(false);
                        SessionHolder.game = game;
                        game.getAnimalMap().put(myUserId, animal);
                    }
                    if ("continue".equals(json.getString("msg"))) {
                        animal.setMoveStatus(true);
                        SessionHolder.game = game;
                        game.getAnimalMap().put(myUserId, animal);
                        SessionHolder.cachedThreadPool.execute(new GameThread(game));
                    }
                    if ("left".equals(json.getString("msg"))) {
                        animal.setMoveStatus(false);
                        animal.moveLeft();
                        move(myUserId, game, animal);
                    }
                    if ("right".equals(json.getString("msg"))) {
                        animal.setMoveStatus(false);
                        animal.moveRight();
                        move(myUserId, game, animal);
                    }
                    if ("up".equals(json.getString("msg"))) {
                        animal.setMoveStatus(false);
                        animal.moveUp();
                        move(myUserId, game, animal);
                    }
                    if ("down".equals(json.getString("msg"))) {
                        animal.setMoveStatus(false);
                        animal.moveDown();
                        move(myUserId, game, animal);
                    }
                    if ("bomb".equals(json.getString("msg"))) {
                        //超过最大数量不能放
                        ActiveUtils.createBomb(myUserId, game, animal);
                    }
                    if ("restart".equals(json.getString("msg"))) {
                        ActiveUtils.createGame(myUserId);
                    }
                    if ("leftWall".equals(json.getString("msg"))) {
                        animal.setMoveStatus(false);
                        animal.leftWall();
                        game.getAnimalMap().put(myUserId, animal);
                        ActiveUtils.sendMessage(myUserId, game);
                    }
                    if ("rightWall".equals(json.getString("msg"))) {
                        animal.setMoveStatus(false);
                        animal.rightWall();
                        game.getAnimalMap().put(myUserId, animal);
                        ActiveUtils.sendMessage(myUserId, game);
                    }
                    if ("upWall".equals(json.getString("msg"))) {
                        animal.setMoveStatus(false);
                        animal.upWall();
                        game.getAnimalMap().put(myUserId, animal);
                        ActiveUtils.sendMessage(myUserId, game);
                    }
                    if ("downWall".equals(json.getString("msg"))) {
                        animal.setMoveStatus(false);
                        animal.downWall();
                        game.getAnimalMap().put(myUserId, animal);
                        ActiveUtils.sendMessage(myUserId, game);
                    }
                    break;
                //私聊
                case MessageCodeConstant.PRIVATE_CHAT_CODE:
                    //接收人id
                    String receiveUserId = json.getString("receiverUserId");
                    String sendUserId = json.getString("sendUserId");
                    String msg = JSONObject.toJSONString(json);
                    // 点对点挨个给接收人发送消息
                    for (Map.Entry<String, Channel> entry : SessionHolder.channelMap.entrySet()) {
                        String userId = entry.getKey();
                        Channel channel = entry.getValue();
                        if (receiveUserId.equals(userId)) {
                            channel.writeAndFlush(new TextWebSocketFrame(msg));
                        }
                    }
                    // 如果发给别人，给自己也发一条
                    if (!receiveUserId.equals(sendUserId)) {
                        SessionHolder.channelMap.get(sendUserId).writeAndFlush(new TextWebSocketFrame(msg));
                    }
                    break;
                case MessageCodeConstant.SYSTEM_MESSAGE_CODE:
                    //向连接上来的客户端广播消息
                    SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(json)));
                    break;
                //pong
                case MessageCodeConstant.PONG_CHAT_CODE:
                    Channel channel = ctx.channel();
                    // 更新心跳时间
                    NettyAttrUtil.refreshLastHeartBeatTime(channel);
                default:
            }
        } catch (Exception e) {
            logger.error("转发消息异常:", e);
            e.printStackTrace();
        }
    }

    private void move(String myUserId, Game game, Animal animal) {
        for (Bomb bomb1 : game.getBoomList()) {
            if (animal.getHLocation().equals(bomb1.getPoint().getBottom()) && animal.getWLocation().equals(bomb1.getPoint().getLeft())) {
                // 被炸到
                game.setDead(StrUtil.format("{}被{}炸死了", myUserId, bomb1.getUserId()));
            }
        }
        game.getAnimalMap().put(myUserId, animal);
        ActiveUtils.sendMessage(myUserId, game);
    }

    /**
     * 客户端与服务端创建连接的时候调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //创建新的 WebSocket 连接，保存当前 channel
        logger.info("————客户端与服务端连接开启————");
//        // 设置高水位
//        ctx.channel().config().setWriteBufferHighWaterMark();
//        // 设置低水位
//        ctx.channel().config().setWriteBufferLowWaterMark();
    }

    /**
     * 客户端与服务端断开连接的时候调用
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("————客户端与服务端连接断开————");
        websocketInfoService.clearSession(ctx.channel());
    }

    /**
     * 服务端接收客户端发送过来的数据结束之后调用
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 工程出现异常的时候调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("异常:", cause);
        ctx.close();
    }

    /**
     * 服务端处理客户端websocket请求的核心方法
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        if (o instanceof FullHttpRequest) {
            //处理客户端向服务端发起 http 请求的业务
            handHttpRequest(channelHandlerContext, (FullHttpRequest) o);
        } else if (o instanceof WebSocketFrame) {
            //处理客户端与服务端之间的 websocket 业务
            handWebsocketFrame(channelHandlerContext, (WebSocketFrame) o);
        }
    }

    /**
     * 处理客户端向服务端发起 http 握手请求的业务
     * WebSocket在建立握手时，数据是通过HTTP传输的。但是建立之后，在真正传输时候是不需要HTTP协议的。
     * <p>
     * WebSocket 连接过程：
     * 首先，客户端发起http请求，经过3次握手后，建立起TCP连接；http请求里存放WebSocket支持的版本号等信息，如：Upgrade、Connection、WebSocket-Version等；
     * 然后，服务器收到客户端的握手请求后，同样采用HTTP协议回馈数据；
     * 最后，客户端收到连接成功的消息后，开始借助于TCP传输信道进行全双工通信。
     */
    private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws InterruptedException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        // 如果请求失败或者该请求不是客户端向服务端发起的 http 请求，则响应错误信息
        if (!request.decoderResult().isSuccess()
                || !("websocket".equals(request.headers().get("Upgrade")))) {
            // code ：400
            sendHttpResponse(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        //新建一个握手
        handshaker = factory.newHandshaker(request);
        if (handshaker == null) {
            //如果为空，返回响应：不受支持的 websocket 版本
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            //否则，执行握手
            Map<String, String> params = RequestParamUtil.urlSplit(request.uri());
            String userId = params.get("userId");
            userId = URLDecoder.decode(userId);
            Channel channel = ctx.channel();
            NettyAttrUtil.setUserId(channel, userId);
            NettyAttrUtil.refreshLastHeartBeatTime(channel);
            handshaker.handshake(ctx.channel(), request);
            SessionHolder.channelGroup.add(ctx.channel());
            SessionHolder.channelMap.put(userId, ctx.channel());
            logger.info("握手成功，客户端请求uri：{}", request.uri());

            // 推送用户上线消息，更新客户端在线用户列表
            Set<String> userList = SessionHolder.channelMap.keySet();
            WsMessage msg = new WsMessage();
            Map<String, Object> ext = new HashMap<String, Object>();
            ext.put("userList", userList);
            msg.setExt(ext);
            msg.setCode(MessageCodeConstant.SYSTEM_MESSAGE_CODE);
            msg.setType(MessageTypeConstant.UPDATE_USERLIST_SYSTEM_MESSGAE);
            SessionHolder.channelGroup.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(msg)));
            //看看缓存能不能拿到
            Game game = SessionHolder.game;
            if (ObjectUtil.isEmpty(game)) {
                ActiveUtils.createGame(userId);
            } else {
                // 存在一局游戏但是玩家没有角色，创建一个角色
                Animal animal = game.getAnimalMap().get(userId);
                if (ObjectUtil.isEmpty(animal)) {
                    animal = new Animal();
                    game.getRecordList().put(userId, 0);
                    SessionHolder.game.getAnimalMap().put(userId, animal);
                }
            }
            SessionHolder.cachedThreadPool.execute(new GameThread(game));
        }
    }


    /**
     * 服务端向客户端响应消息
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, DefaultFullHttpResponse response) {
        if (response.status().code() != 200) {
            //创建源缓冲区
            ByteBuf byteBuf = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8);
            //将源缓冲区的数据传送到此缓冲区
            response.content().writeBytes(byteBuf);
            //释放源缓冲区
            byteBuf.release();
        }
        //写入请求，服务端向客户端发送数据
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(response);
        if (response.status().code() != 200) {
            /**
             * 如果请求失败，关闭 ChannelFuture
             * ChannelFutureListener.CLOSE 源码：future.channel().close();
             */
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
