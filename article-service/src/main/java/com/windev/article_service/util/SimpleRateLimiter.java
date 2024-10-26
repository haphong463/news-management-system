package com.windev.article_service.util;

public class SimpleRateLimiter {
    private final long intervalMillis;
    private long nextAvailableTime;

    public SimpleRateLimiter(long intervalMillis) {
        this.intervalMillis = intervalMillis;
        this.nextAvailableTime = System.currentTimeMillis();
    }

    public synchronized void acquire() throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        if (currentTime < nextAvailableTime) {
            long waitTime = nextAvailableTime - currentTime;
            Thread.sleep(waitTime);
        }
        nextAvailableTime = System.currentTimeMillis() + intervalMillis;
    }
}