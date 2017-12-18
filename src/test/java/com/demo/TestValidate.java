package com.demo;

import org.junit.Test;

import java.io.*;

public class TestValidate {
    public static void createValidateFile(){
        BufferedReader br = null;
        BufferedWriter bwTest = null;
        BufferedWriter bwTestResult = null;
        try {
            br = new BufferedReader(new FileReader(
                    "src"+ File.separator+"resources"+File.separator+"smalldata"));
            bwTest = new BufferedWriter(new FileWriter(
                    "output" + File.separator + "test.txt"));
            bwTestResult = new BufferedWriter(new FileWriter(
                    "output" + File.separator + "testResult.txt"));

            // 读取1000行, 分割, 分别存储
            for (int i=0; i<1000; i++){
                String[] ss = br.readLine().split("\t");
                bwTestResult.write(ss[0] + "\n");
                bwTest.write(ss[1] + "\n");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                br.close();
                bwTest.flush();
                bwTestResult.flush();
                bwTest.close();
                bwTestResult.close();
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void test(){
        createValidateFile();
    }

}
