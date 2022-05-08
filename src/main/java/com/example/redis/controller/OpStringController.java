package com.example.redis.controller;

import com.example.redis.config.RedisKey;
import com.example.redis.pojo.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/op/string")
public class OpStringController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    User hello = new User("123", "hello", 20);
    @Autowired
    ObjectMapper convert;

    @GetMapping("/save/user")
    public User save() throws JsonProcessingException {
        redisTemplate.opsForValue().set(RedisKey.USER_PREFIX.getPrefix(hello.getId()), convert.writeValueAsString(hello));
        return convert.readValue(redisTemplate.opsForValue().get(RedisKey.USER_PREFIX + hello.getId()), User.class);
    }


    @GetMapping("/save/user/ex")
    public User saveEx() throws JsonProcessingException {
        System.out.println(redisTemplate.opsForValue().setIfAbsent(RedisKey.USER_PREFIX.getPrefix(hello.getId()), convert.writeValueAsString(hello),
                5, TimeUnit.SECONDS));
        return convert.readValue(redisTemplate.opsForValue().get(RedisKey.USER_PREFIX + hello.getId()), User.class);
    }

    @GetMapping("/msave/user/ex")
    public List<User> memberSaveEx() throws JsonProcessingException {
        Map<String, String> save = new HashMap<>();
        save.put(RedisKey.USER_PREFIX.getPrefix(hello.getId() + "1"), convert.writeValueAsString(hello));
        save.put(RedisKey.USER_PREFIX.getPrefix(hello.getId() + "2"), convert.writeValueAsString(hello));
        save.put(RedisKey.USER_PREFIX.getPrefix(hello.getId() + "3"), convert.writeValueAsString(hello));

        redisTemplate.opsForValue().multiSet(save);

        List<String> list = redisTemplate.opsForValue().multiGet(Arrays.asList(RedisKey.USER_PREFIX.getPrefix(hello.getId() + "1"),
                RedisKey.USER_PREFIX.getPrefix(hello.getId() + "2"),
                RedisKey.USER_PREFIX.getPrefix(hello.getId() + "3")));

        List<User> collect = list.stream().peek(System.out::println).map(i -> {
            try {
                return convert.readValue(i, User.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        return collect;
    }
}
