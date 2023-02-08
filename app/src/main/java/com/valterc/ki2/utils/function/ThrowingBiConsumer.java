package com.valterc.ki2.utils.function;

@FunctionalInterface
public interface ThrowingBiConsumer<TData1, TData2> {

    void accept(TData1 data1, TData2 data2) throws Exception;

}
