package com.example.redis.controller;

import com.example.redis.config.RedisKey;
import com.example.redis.pojo.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis 的 set
 * 求共同好友
 */
@Slf4j
@RestController("/set")
@SuppressWarnings("all")
public class OpSetController implements InitializingBean {

    private List<User> userList;
    UserNetWork user1;
    UserNetWork user2;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class UserNetWork {
        User user;
        List<User> friends;
    }

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private ObjectMapper convert;


    @GetMapping("/init")
    public void saveFriendsNetWork() throws JsonProcessingException {

        toArraySaveToRedis(user1);
        toArraySaveToRedis(user2);
    }

    private void toArraySaveToRedis(UserNetWork user) {
        String[] friends = user.friends.stream().map(item -> {
            try {
                return convert.writeValueAsString(item);
            } catch (JsonProcessingException e) {
                log.error(e.getOriginalMessage());
            }
            return null;
        }).toArray(String[]::new);

        redisTemplate.opsForSet().add(RedisKey.USER_PREFIX.getPrefix(user.getUser().getId()), friends);
    }

    @GetMapping("/friends")
    public List<User> findCommentFriends() {
        Set<String> intersect = redisTemplate.opsForSet().intersect(RedisKey.USER_PREFIX.getPrefix(user1.getUser().getId()),
                RedisKey.USER_PREFIX.getPrefix(user2.getUser().getId()));
        List<User> collect = intersect.stream().map(item -> {
            try {
                return convert.readValue(item, User.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("start init userList");
        this.userList = Arrays.asList(
                new User("001", "小明", 10),
                new User("002", "小红", 11),
                new User("003", "小蓝", 11),
                new User("004", "小绿", 11),
                new User("005", "小白", 11),
                new User("005", "小球", 11));
        log.info("end init userList");
        user1 = new UserNetWork(userList.get(0), userList.subList(3, userList.size()));
        user2 = new UserNetWork(userList.get(1), userList.subList(4, userList.size()));
    }
}
