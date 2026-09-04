package com.tyler.YouthEngedi.utils;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

public final class IdManager {

    // Initial guest pool starts at negative numbers to prevent collision with database auto-increment keys
    private static final long INITIAL_POOL_START = -100_000L;
    private static final long INITIAL_POOL_SIZE = 1_000;

    private static final ConcurrentLinkedDeque<Long> AVAILABLE_IDS = new ConcurrentLinkedDeque<>();

    private static final AtomicLong OVERFLOW_GENERATOR = new AtomicLong(INITIAL_POOL_START - 1);



    static{
        // Pre-populate pool with reusable IDs [-1,-2,-3, ..., -1000]
        for(long i = -1L; i >= -INITIAL_POOL_SIZE;i--){
            AVAILABLE_IDS.offer(i);
        }
    }

    private IdManager(){}

    public static long delegateIds(){
        Long borrowedId = AVAILABLE_IDS.poll();
        if(borrowedId != null){
            return borrowedId;
        }

        return OVERFLOW_GENERATOR.getAndDecrement();
    }
    public static void releaseId(long id){
        if(id < 0 && id >= -INITIAL_POOL_SIZE){
            if(!AVAILABLE_IDS.contains(id)){
                AVAILABLE_IDS.offer(id);
            }
        }
    }

    public static int getAvailableCount(){
        return AVAILABLE_IDS.size();
    }
}
