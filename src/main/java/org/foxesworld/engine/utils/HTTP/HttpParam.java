package org.foxesworld.engine.utils.HTTP;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@SuppressWarnings("unused")
public @interface HttpParam {
    String key();
}