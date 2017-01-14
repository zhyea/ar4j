package com.zhyea.ar4j.core.cache;

public interface Cache {

    <T> void put(String key, T value, long ttl);

    <T> void put(String key, T value);

    <T> T get(String key);

}
