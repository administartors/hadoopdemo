package com.demo;

import org.junit.Test;

public class TestMath {
    @Test
    public void testLog(){
        System.out.println(Math.log(15873.0 / 200000));
    }

    @Test
    public void testLikelihood(){
        System.out.println(Math.log(
                3.0 / 593691 + 80234
        ));
    }
}
