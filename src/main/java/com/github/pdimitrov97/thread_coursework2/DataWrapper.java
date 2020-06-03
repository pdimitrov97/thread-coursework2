package com.github.pdimitrov97.thread_coursework2;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

public class DataWrapper <T,G> implements Serializable, Comparable {

    private int segIndex;
    private List<T> list;
    private Function<T, G> fx;

    public DataWrapper(List<T> listObj, Function<T, G> fx) {
        this.list = listObj;
        this.fx = fx;
    }

    public DataWrapper(int segIndex, List<T> listObj, Function<T, G> fx) {
        this.segIndex = segIndex;
        this.list = listObj;
        this.fx = fx;
    }

    public int getSegIndex() {
        return segIndex;
    }

    public void setSegIndex(int segIndex) {
        this.segIndex = segIndex;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Function<T, G> getFx() {
        return fx;
    }

    public void setFx(Function<T, G> fx) {
        this.fx = fx;
    }

    @Override
    public int compareTo(Object o) {
        DataWrapper other = (DataWrapper) o;
        return Integer.compare(this.segIndex, other.segIndex);
    }
}


