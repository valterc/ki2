package com.valterc.ki2.services.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import timber.log.Timber;

public class ServiceHandler {

    private static final long RETRY_DELAY_MS = 100;
    private static final long RETRY_MAX_ATTEMPTS = 2;

    private Looper looper;
    private Handler handler;

    public ServiceHandler() {
        Thread thread = new Thread(() -> {
            Looper.prepare();
            this.looper = Looper.myLooper();
            this.handler = new RunnableHandler(this.looper);
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

    public void postRetriableAction(UnsafeRunnable runnable) {
        Consumer<Integer> retriableRunnable = new Consumer<Integer>() {
            @Override
            public void accept(Integer retry) {
                try {
                    runnable.run();
                } catch (Exception e) {
                    if (retry < RETRY_MAX_ATTEMPTS) {
                        postDelayedAction(() -> this.accept(retry + 1), RETRY_DELAY_MS);
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        handler.post(() -> retriableRunnable.accept(0));
    }

    private static class RunnableHandler extends Handler {

        public RunnableHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message message) {
            try {
                super.handleMessage(message);
            } catch (Exception e) {
                Timber.e(e, "Error handling message");
            }
        }

    }

}
