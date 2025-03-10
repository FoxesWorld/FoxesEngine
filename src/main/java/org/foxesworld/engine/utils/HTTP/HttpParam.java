package org.foxesworld.engine.utils.HTTP;

import java.lang.annotation.*;

@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HttpParam {
    /**
     * Ключ HTTP-параметра.
     * Если не передано значение, используется имя поля.
     */
    String value() default "";
}