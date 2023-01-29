package com.valterc.ki2.update.post.actions;

import com.valterc.ki2.update.PostUpdateContext;

@FunctionalInterface
public interface IPostUpdateAction {

    void execute(PostUpdateContext context);

}
