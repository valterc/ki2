package com.valterc.ki2.input;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;

import com.valterc.ki2.data.switches.SwitchEvent;
import com.valterc.ki2.data.switches.SwitchKeyEvent;
import com.valterc.ki2.karoo.input.KarooKey;

import java.lang.reflect.Method;
import java.util.HashMap;

public class InputAdapter {

    private final Context context;
    private final HashMap<KarooKey, Long> keyDownTimeMap;
    private InputManager inputManager;
    private Method injectInputMethod;

    public InputAdapter(Context context){
        this.context = context;
        this.keyDownTimeMap = new HashMap<>();
        initInputManager();
    }

    private void initInputManager(){
        Object systemService = context.getSystemService(Context.INPUT_SERVICE);
        if (systemService != null) {
            inputManager = (InputManager) systemService;
        }

        Class[] paramTypes = new Class[2];
        paramTypes[0] = InputEvent.class;
        paramTypes[1] = Integer.TYPE;

        try {
            injectInputMethod = inputManager.getClass().getMethod("injectInputEvent", paramTypes);
        } catch (Exception e) {
            Log.e("KI2", e.toString());
        }
    }

    private void injectKeyDown(int keyCode, long eventTime, int repeat){
        try {
            Object[] params = new Object[2];
            params[0] = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, repeat);
            params[1] = 0;

            injectInputMethod.invoke(inputManager, params);
        } catch (Exception e) {
            Log.e("KI2", e.toString());
        }
    }

    private void injectKeyUp(int keyCode, long keyDownTime){
        try {
            Object[] params = new Object[2];
            params[0] = new KeyEvent(keyDownTime, SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, keyCode, 0);
            params[1] = 0;

            injectInputMethod.invoke(inputManager, params);
        } catch (Exception e) {
            Log.e("KI2", e.toString());
        }
    }

    private void injectKeyPress(int keyCode){
        try {
            Object[] params = new Object[2];
            params[0] = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, keyCode, 0);
            params[1] = 0;

            injectInputMethod.invoke(inputManager, params);

            params[0] = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, keyCode, 0);
            params[1] = 0;

            injectInputMethod.invoke(inputManager, params);
        } catch (Exception e) {
            Log.e("KI2", e.toString());
        }
    }

    private void setKeyDown(KarooKey key, int repeat)
    {
        Long time = keyDownTimeMap.get(key);

        if (time == null || repeat == 0) {
            time = System.currentTimeMillis();
            keyDownTimeMap.put(key, time);
        }

        injectKeyDown(key.getKeyCode(), time, repeat);
    }

    private void setKeyUp(KarooKey key)
    {
        long time = System.currentTimeMillis();

        Long keyDownTime = keyDownTimeMap.remove(key);
        if (keyDownTime != null)
        {
            time = keyDownTime;
        }

        injectKeyUp(key.getKeyCode(), time);

    }

    private void keyPressed(KarooKey key)
    {
        injectKeyPress(key.getKeyCode());
    }

    public void executeKeyEvent(SwitchKeyEvent switchKeyEvent) {
        switch (switchKeyEvent.getCommand())
        {
            case SINGLE_CLICK:
                keyPressed(switchKeyEvent.getKey());
                break;

            case DOUBLE_CLICK:
                keyPressed(switchKeyEvent.getKey());
                keyPressed(switchKeyEvent.getKey());
                break;

            case LONG_PRESS_DOWN:
            case LONG_PRESS_CONTINUE:
                setKeyDown(switchKeyEvent.getKey(), switchKeyEvent.getRepeat());
                break;

            case LONG_PRESS_UP:
                setKeyUp(switchKeyEvent.getKey());
                break;
        }
    }
}
