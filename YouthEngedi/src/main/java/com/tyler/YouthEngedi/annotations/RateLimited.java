package com.tyler.YouthEngedi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    String key() default "";
    int capacity() default 10;
    int refillTokens() default 10;
    String refillDuration() default "1m";
}
