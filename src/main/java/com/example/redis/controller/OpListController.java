package com.example.redis.controller;

import com.example.redis.config.RedisKey;
import com.example.redis.pojo.Goods;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 场景：商品抢购。
 * 用 List 充当队列，左进右出。
 * 用 apache2-utils 进行测试
 * ab -c 1000 -n 100000 http://127.0.0.1/     ===> 开启 1000 个线程，访问 http://127.0.0.1/ 100000 次。
 */
@RestController
@RequestMapping("/goods")
@Slf4j
public class OpListController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper convert;

    private AtomicInteger count = new AtomicInteger(0);

    /**
     * 业务场景，用户秒杀，用队列模拟
     */
    @GetMapping("/init")
    public void initProduct() {
        log.info("开始初始化商品信息");
        redisTemplate.opsForValue().set("goods_count", "100", 60 * 24, TimeUnit.MINUTES);
        log.info("商品信息初始化完毕");
    }

    @GetMapping("/buy")
    public void buyGoods() throws JsonProcessingException {
        log.info("抢购商品");
        Long goods_count = redisTemplate.opsForValue().decrement("goods_count");
        if (goods_count != null && goods_count >= 0) {
            log.info("成功抢到了商品");
            Goods goods = new Goods("1001", "小王" + count.incrementAndGet(), 10058);
            Long count = redisTemplate.opsForList().leftPush(RedisKey.GOODS_PREFIX.getPrefix(goods.getId()), convert.writeValueAsString(goods));
            log.info("已被抢购{}", count);
        } else {
            log.info("抢购失败");
        }
    }

    @GetMapping("/{start}/{end}")
    public List<Goods> goodsRange(@PathVariable("start") int start, @PathVariable("end") int end) {
        log.info("查看指定范围的商品");
        // start~end,包括 start，包括 end
        List<String> range = redisTemplate.opsForList().range(RedisKey.GOODS_PREFIX.getPrefix("1001"), start, end);
        List<Goods> collect = range.stream().map(item -> {
            try {
                return convert.readValue(item, Goods.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        log.info(String.valueOf(collect.size()));
        return collect;
    }
}
