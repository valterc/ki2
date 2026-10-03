package com.valterc.ki2.data.action;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.valterc.ki2.external.ExternalActionTarget;

public class Ki2ActionEvent {

    public enum Type {
        KAROO,
        EXTERNAL
    }

    @NonNull
    private final Type type;
    @Nullable
    private final KarooActionEvent karooActionEvent;
    @Nullable
    private final ExternalActionTarget externalActionTarget;
    private final int replicate;

    private Ki2ActionEvent(@NonNull Type type,
                           @Nullable KarooActionEvent karooActionEvent,
                           @Nullable ExternalActionTarget externalActionTarget,
                           int replicate) {
        this.type = type;
        this.karooActionEvent = karooActionEvent;
        this.externalActionTarget = externalActionTarget;
        this.replicate = replicate;
    }

    @NonNull
    public static Ki2ActionEvent forKaroo(@NonNull KarooActionEvent event) {
        return new Ki2ActionEvent(Type.KAROO, event, null, event.getReplicate());
    }

    @NonNull
    public static Ki2ActionEvent forExternal(@NonNull ExternalActionTarget target) {
        return new Ki2ActionEvent(Type.EXTERNAL, null, target, 1);
    }

    @NonNull
    public static Ki2ActionEvent forExternal(@NonNull ExternalActionTarget target, int replicate) {
        return new Ki2ActionEvent(Type.EXTERNAL, null, target, replicate);
    }

    @NonNull
    public Type getType() {
        return type;
    }

    @Nullable
    public KarooActionEvent getKarooActionEvent() {
        return karooActionEvent;
    }

    @Nullable
    public ExternalActionTarget getExternalActionTarget() {
        return externalActionTarget;
    }

    public int getReplicate() {
        return replicate;
    }

    @NonNull
    public Ki2ActionEvent withReplicate(int newReplicate) {
        if (type == Type.KAROO && karooActionEvent != null) {
            return new Ki2ActionEvent(type, new KarooActionEvent(karooActionEvent, newReplicate), null, newReplicate);
        }
        if (type == Type.EXTERNAL && externalActionTarget != null) {
            return new Ki2ActionEvent(type, null, externalActionTarget, newReplicate);
        }
        return this;
    }
}
