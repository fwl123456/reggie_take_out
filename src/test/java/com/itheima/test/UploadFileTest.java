package com.itheima.test;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class UploadFileTest {
    @Test
    public void test1() {
        String fileName = "ererewe.jpg";
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(suffix);
    }

    @Test
    public void test2() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("1");
        for (String s : arrayList) {
            System.out.println(s);
        }
    }
}
