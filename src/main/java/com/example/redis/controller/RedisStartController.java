package com.example.redis.controller;

import com.example.redis.pojo.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/index")
public class RedisStartController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final String prefix = "redis:study:";

    @GetMapping("/1")

    public String index() {
        redisTemplate.opsForValue().set("first", "first message");
        return "Hello World";
    }

    @GetMapping("/user")
    public User userInformation() throws JsonProcessingException {
        User user = new User("123", "lll", 19);
        ObjectMapper convert = new ObjectMapper();
        redisTemplate.opsForValue().set(prefix + user.getId(), convert.writeValueAsString(user));
        String strUser = redisTemplate.opsForValue().get(prefix + user.getId());
        User str2user = convert.readValue(strUser, User.class);
        return str2user;
    }
}
