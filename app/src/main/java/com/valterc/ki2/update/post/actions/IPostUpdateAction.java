package com.valterc.ki2.update.post.actions;

import androidx.annotation.NonNull;

import com.valterc.ki2.update.post.PostUpdateContext;

@FunctionalInterface
public interface IPostUpdateAction {

    void execute(@NonNull PostUpdateContext context);

}
