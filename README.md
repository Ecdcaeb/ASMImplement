a gradle plugin for ASMImplement


See:
```gradle
pluginManagement {
    repositories {
        maven {
            url 'https://ecdcaeb.github.io/maven/'
        }
    }
}

plugins {
    id 'io.github.ecdcaeb.gradle.asmImplement' version '0.1.3'
}
/*
buildscript {
    repositories {
        maven {
            url 'https://ecdcaeb.github.io/maven/'
        }
    }
    dependencies {
        classpath "io.github.ecdcaeb.gradle.asmImplement:ASMImplementPlugin:0.1.3"
    }
}
apply plugin: "io.github.ecdcaeb.gradle.asmImplement"
*/

repositories {
    maven {
        url 'https://ecdcaeb.github.io/maven/'
    }
}

dependencies {
    implementation 'io.github.ecdcaeb.gradle.asmImplement:ASMImplementAPI:0.1.2'
}
```


```java
import io.github.ecdcaeb.gradle.asmImplement.ASMImplement;

public class Calculator {
    @ASMImplement(
        generatorClass = "com.example.generators.ComputeFlagsGenerator",
        generatorMethod = "generate"
    )
    public native int computeFlags(int data);
}
```
```java
package com.example.generators;

import io.github.ecdcaeb.gradle.asmImplement.lib.asm.tree.*;
import io.github.ecdcaeb.gradle.asmImplement.lib.asm.*;

public class ComputeFlagsGenerator {
    public static void generate(MethodNode method, ClassNode classNode) {
        method.visitVarInsn(Opcodes.ILOAD, 1);
        method.visitVarInsn(Opcodes.BIPUSH, 100);
        method.visitInsn(Opcodes.IADD);
        method.visitInsn(Opcodes.IRETURN);

        method.visitMax(1, 1);
    }
}
```