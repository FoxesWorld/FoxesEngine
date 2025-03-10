package org.foxesworld.engine.utils.HTTP;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface HttpConfig {
    int connectTimeout() default 5000;
    int readTimeout() default 5000;
}
