package ru.idfedorov09.telegram.bot.service;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import redis.clients.jedis.Jedis;

@Service
public class UserQueue {

    @Autowired
    private RedisService redisService;

    @Autowired
    private Gson gson;

    public static final String QUEUE_PREFIX = "frjekcs_ewer_idfed09_user_bot_que_";

    public String getQueueKey(String chatId){
        return QUEUE_PREFIX+chatId;
    }
    public String popString(String chatId){
        return redisService.lpop(getQueueKey(chatId));
    }

    public Update popUpdate(String chatId){
        return gson.fromJson(this.popString(chatId), Update.class);
    }

    public void push(Update update, String chatId){
        String jsonUpdate = gson.toJson(update, Update.class);
        this.push(jsonUpdate, chatId);
    }

    public void push(String string, String chatId){
        redisService.rpush(getQueueKey(chatId), string);
    }

}
