package com.sohu.sns.monitor.controller.annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestParams {
	String path();
	String[] method();
	String[] required();
	boolean  isCheckToken() default false;
    boolean isCheckReplay() default false;
}
