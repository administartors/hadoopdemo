package com.demo;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

    private static Pattern pattern = Pattern.compile("[^\u4E00-\u9FA5]");
    private static Matcher matcher;

    private static boolean check(String s){
        matcher = pattern.matcher(s);
        while (matcher.find()){
            return false;
        }

        return true;
    }

    @Test
    public void testMatch(){
        System.out.println(check("萨达萨达"));
    }
}
