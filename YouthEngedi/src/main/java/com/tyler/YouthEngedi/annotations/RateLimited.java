package com.tyler.YouthEngedi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    long capacity() default 10;
    long tokens() default 10;
    long duration() default 1;
    ChronoUnit unit() default ChronoUnit.MINUTES;
}
