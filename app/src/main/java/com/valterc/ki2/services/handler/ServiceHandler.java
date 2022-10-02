package com.valterc.ki2.services.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

import timber.log.Timber;

public class ServiceHandler {

    private static final long RETRY_DELAY_MS = 100;
    private static final int MESSAGE_PROCESS_RUNNABLE = 1000;

    private final Thread thread;
    private Handler handler;

    public ServiceHandler() {
        this.thread = new Thread(() -> {
            Looper.prepare();
            this.handler = new RunnableHandler();
            Looper.loop();
        }, "ServiceHandler");
        this.thread.start();

        while (this.handler == null) {
            Thread.yield();
        }
    }

    public void postAction(Runnable runnable) {
        Message message = Message.obtain(handler, runnable);
        message.what = MESSAGE_PROCESS_RUNNABLE;
        handler.sendMessage(message);
    }

    public void postDelayedAction(Runnable runnable, long delayMs) {
        Message message = Message.obtain(handler, runnable);
        message.what = MESSAGE_PROCESS_RUNNABLE;
        handler.sendMessageDelayed(message, delayMs);
    }

    public void postRetriableAction(UnsafeRunnable runnable) {
        Consumer<Integer> p = new Consumer<Integer>() {
            @Override
            public void accept(Integer retry) {
                try {
                    runnable.run();
                } catch (Exception e) {
                    if (retry < 2) {
                        postDelayedAction(() -> this.accept(retry + 1), RETRY_DELAY_MS);
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        Message message = Message.obtain(handler, () -> {
            p.accept(0);
        });
        message.what = MESSAGE_PROCESS_RUNNABLE;
        handler.sendMessage(message);
    }

    private class RunnableHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message message) {
            if (message.what == MESSAGE_PROCESS_RUNNABLE) {
                Runnable runnable = message.getCallback();
                if (runnable != null) {
                    try {
                        message.getCallback().run();
                    } catch (Exception e) {
                        Timber.e(e, "Error executing runnable");
                    }
                } else {
                    Timber.w("Unexpected null runnable");
                }
            } else {
                super.handleMessage(message);
            }
        }

    }

}
