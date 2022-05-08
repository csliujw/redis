package com.example.redis.config;


public enum RedisKey {
    USER_PREFIX("user:add:"),
    GOODS_PREFIX("goods:add:");


    private String prefix;

    public String getPrefix(String append) {
        return prefix + append;
    }

    RedisKey(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return prefix;
    }
}
