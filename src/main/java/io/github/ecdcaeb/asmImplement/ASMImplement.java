package io.github.ecdcaeb.asmImplement;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface ASMImplement {
    String generatorClass();
    String generatorMethod();
}