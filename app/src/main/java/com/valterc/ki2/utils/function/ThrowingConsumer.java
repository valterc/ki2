package com.valterc.ki2.utils.function;

@FunctionalInterface
public interface ThrowingConsumer<TData> {

    void accept(TData data) throws Exception;

}
