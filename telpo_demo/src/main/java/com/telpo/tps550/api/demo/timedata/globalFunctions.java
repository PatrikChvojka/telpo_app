package com.telpo.tps550.api.demo.timedata;

import java.util.Arrays;

public class globalFunctions {

    public static byte[] trim(byte[] bytes)
    {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0){ --i; }
        return Arrays.copyOf(bytes, i + 1);
    }

    public static int api_call (String $result){
        int randomNumber = (int) ((Math.random() * (2 - 0)) + 0);
        return randomNumber;
    }
}
