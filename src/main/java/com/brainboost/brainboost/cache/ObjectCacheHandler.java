package com.brainboost.brainboost.cache;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ObjectCacheHandler  {

    private final Map<Long, Object> objectCache = new HashMap<>();


    public void addToCache(Long key,Object value){
        objectCache.put(key,value);
    }

    public void removeCache(Long key) {
        objectCache.remove(key);
    }

    public Object getCache (Long key){
        return objectCache.get(key);
    }



}
