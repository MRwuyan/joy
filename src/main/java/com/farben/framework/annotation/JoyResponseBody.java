package com.farben.framework.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JoyResponseBody  {
    String value() default "";
    boolean required() default true;
}
