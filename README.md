a gradle plugin for ASMImplement


See:


```java
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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.ClassNode;

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