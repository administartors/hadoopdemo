package com.demo;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestHashMap {

    private static HashMap<String, Double> map = new HashMap<>(8);

    @Test
    public void test(){
        map.put("1", 1d);
        map.put("2", 1d);
        map.put("3", 1d);
        map.put("4", 1d);
        map.put("5", 1d);

        for (Map.Entry<String, Double> entry : map.entrySet()){
            map.replace(entry.getKey(), 2d);
        }

        for (Map.Entry<String, Double> entry : map.entrySet()){
            System.out.println(entry.getValue());
        }
    }
}
