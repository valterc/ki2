package com.valterc.ki2.input;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import com.valterc.ki2.data.input.KarooKeyEvent;
import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.karoo.Ki2Context;

import java.lang.reflect.Method;
import java.util.HashMap;

@SuppressLint("LogNotTimber")
public class InputAdapter {

    private final Context context;
    private final HashMap<KarooKey, Long> keyDownTimeMap;
    private final VirtualInputAdapter virtualInputAdapter;
    private InputManager inputManager;
    private Method injectInputMethod;

    public InputAdapter(Ki2Context ki2Context) {
        this.context = ki2Context.getSdkContext();
        this.keyDownTimeMap = new HashMap<>();
        this.virtualInputAdapter = new VirtualInputAdapter(ki2Context);
        initInputManager();
    }

    @SuppressWarnings({"rawtypes", "JavaReflectionMemberAccess"})
    private void initInputManager() {
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

    private void injectKeyDown(int keyCode, long eventTime, int repeat) {
        try {
            Object[] params = new Object[2];
            params[0] = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, repeat);
            params[1] = 0;

            injectInputMethod.invoke(inputManager, params);
        } catch (Exception e) {
            Log.e("KI2", e.toString());
        }
    }

    private void injectKeyUp(int keyCode, long keyDownTime) {
        try {
            Object[] params = new Object[2];
            params[0] = new KeyEvent(keyDownTime, SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, keyCode, 0);
            params[1] = 0;

            injectInputMethod.invoke(inputManager, params);
        } catch (Exception e) {
            Log.e("KI2", e.toString());
        }
    }

    private void injectKeyUp(int keyCode, long keyDownTime, long eventTime) {
        try {
            Object[] params = new Object[2];
            params[0] = new KeyEvent(keyDownTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0);
            params[1] = 0;

            injectInputMethod.invoke(inputManager, params);
        } catch (Exception e) {
            Log.e("KI2", e.toString());
        }
    }

    private void injectKeyPress(int keyCode, long eventTime) {
        try {
            Object[] params = new Object[2];
            params[0] = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0);
            params[1] = 0;

            injectInputMethod.invoke(inputManager, params);

            params[0] = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0);
            params[1] = 0;

            injectInputMethod.invoke(inputManager, params);
        } catch (Exception e) {
            Log.e("KI2", e.toString());
        }
    }

    private void setKeyDown(KarooKey key, int repeat) {
        Long time = keyDownTimeMap.get(key);

        if (time == null || repeat == 0) {
            time = System.currentTimeMillis();
            keyDownTimeMap.put(key, time);
        }

        injectKeyDown(key.getKeyCode(), time, repeat);
    }

    private void setKeyUp(KarooKey key) {
        long time = System.currentTimeMillis();

        Long keyDownTime = keyDownTimeMap.remove(key);
        if (keyDownTime != null) {
            time = keyDownTime;
        }

        injectKeyUp(key.getKeyCode(), time);
    }

    private void keyPressed(KarooKey key, long eventTime) {
        injectKeyPress(key.getKeyCode(), eventTime);
    }

    private void simulateLongKeyPress(KarooKey key, long eventTime) {
        injectKeyDown(key.getKeyCode(), eventTime, 0);
        injectKeyDown(key.getKeyCode(), eventTime + ViewConfiguration.getLongPressTimeout(), 1);
        injectKeyUp(key.getKeyCode(), eventTime, eventTime + ViewConfiguration.getLongPressTimeout() * 2L);
    }

    public void executeKeyEvent(KarooKeyEvent keyEvent) {
        if (keyEvent.getKey().isVirtual()) {
            virtualInputAdapter.handleVirtualKeyEvent(keyEvent);
            return;
        }

        for (int i = 0; i < keyEvent.getReplicate(); i++) {
            long eventTime = SystemClock.uptimeMillis() + (long) ViewConfiguration.getKeyRepeatTimeout() * i;
            switch (keyEvent.getAction()) {
                case SINGLE_PRESS:
                    keyPressed(keyEvent.getKey(), eventTime);
                    break;

                case DOUBLE_PRESS:
                    keyPressed(keyEvent.getKey(), eventTime);
                    keyPressed(keyEvent.getKey(), eventTime);
                    break;

                case LONG_PRESS_DOWN:
                case LONG_PRESS_CONTINUE:
                    setKeyDown(keyEvent.getKey(), keyEvent.getRepeat());
                    break;

                case LONG_PRESS_UP:
                    setKeyUp(keyEvent.getKey());
                    break;

                case SIMULATE_LONG_PRESS:
                    simulateLongKeyPress(keyEvent.getKey(), eventTime);
                    break;

            }
        }
    }

}
