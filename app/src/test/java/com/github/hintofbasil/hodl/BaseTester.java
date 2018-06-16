package com.github.hintofbasil.hodl;

import com.github.hintofbasil.hodl.helpers.SqlHelperSingleton;

import org.junit.After;

import java.lang.reflect.Field;

public class BaseTester {

    @After
    public void resetDB(){
        try {
            Class clazz = SqlHelperSingleton.class;
            Field instance = clazz.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

}
