package com.example.redis.controller;

import com.example.redis.config.RedisKey;
import com.example.redis.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/hash")
@Slf4j
public class OpHashController {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/save")
    public void saveUser() {
        Map<String, Object> map = new HashMap<>();
        User user = new User("100", "payphone", 10);
        map.put("name", user.getName());
        map.put("age", String.valueOf(user.getAge()));
        redisTemplate.opsForHash().putAll(RedisKey.USER_PREFIX.getPrefix(user.getId()), map);
        String name = (String) redisTemplate.opsForHash().get(RedisKey.USER_PREFIX.getPrefix(user.getId()), "name");
        String age = (String) redisTemplate.opsForHash().get(RedisKey.USER_PREFIX.getPrefix(user.getId()), "age");
        log.info("age:{}, name:{}", age, name);
    }

    @GetMapping("/modify/{id}/{name}")
    public Boolean modifyUser(@PathVariable("id") String id, @PathVariable("name") String name) {
        redisTemplate.opsForHash().put(RedisKey.USER_PREFIX.getPrefix(id), "name", name);
        // 为 哈希 key 设置过期时间
        redisTemplate.expire(RedisKey.USER_PREFIX.getPrefix(id), 10, TimeUnit.SECONDS);
        return true;
    }
}
