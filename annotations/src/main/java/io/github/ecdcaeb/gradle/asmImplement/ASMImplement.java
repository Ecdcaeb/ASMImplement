package io.github.ecdcaeb.gradle.asmImplement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@SuppressWarnings("unused")
public @interface ASMImplement {
    String generatorClass();
    String generatorMethod();
}