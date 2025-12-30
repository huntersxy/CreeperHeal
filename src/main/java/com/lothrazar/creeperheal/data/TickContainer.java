package com.lothrazar.creeperheal.data;

public class TickContainer<T> {
    private int tick;
    private T data;

    public TickContainer(int tick, T data) {
        this.tick = tick;
        this.data = data;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public T getData() {
        return data;
    }
}