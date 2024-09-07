package com.valterc.ki2.services.handler;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.valterc.ki2.utils.SafeHandler;
import com.valterc.ki2.utils.function.ThrowingRunnable;

import java.util.Objects;
import java.util.function.Consumer;

import timber.log.Timber;

@SuppressLint("LogNotTimber")
public class ServiceHandler {

    private static final long RETRY_DELAY_MS = 100;
    private static final long RETRY_MAX_ATTEMPTS = 2;

    private final Thread thread;
    private Looper looper;
    private Handler handler;

    public ServiceHandler() {
        thread = new Thread(() -> {
            Looper.prepare();
            this.looper = Objects.requireNonNull(Looper.myLooper());
            this.handler = new SafeHandler(this.looper);
            Looper.loop();
        }, "ServiceHandler");
        thread.start();

        while (this.handler == null) {
            Thread.yield();
        }
    }

    public void dispose() {
        this.looper.quit();
    }

    public void postAction(Runnable runnable) {
        handler.post(runnable);
    }

    public void postDelayedAction(Runnable runnable, long delayMs) {
        handler.postDelayed(runnable, delayMs);
    }

    public void postRetriableAction(ThrowingRunnable runnable) {
        Consumer<Integer> retriableRunnable = new Consumer<Integer>() {
            @Override
            public void accept(Integer retry) {
                try {
                    runnable.run();
                } catch (Exception e) {
                    if (retry < RETRY_MAX_ATTEMPTS) {
                        postDelayedAction(() -> this.accept(retry + 1), RETRY_DELAY_MS);
                    } else {
                        Timber.e(e, "Error in service handler retriable invocation");
                    }
                }
            }
        };

        handler.post(() -> retriableRunnable.accept(0));
    }

    public boolean isOnServiceHandlerThread() {
        return Thread.currentThread() == thread;
    }

}
