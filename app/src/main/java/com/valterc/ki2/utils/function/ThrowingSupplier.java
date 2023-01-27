package com.valterc.ki2.utils.function;

public interface ThrowingSupplier<TReturn> {

    TReturn get() throws Exception;

}
