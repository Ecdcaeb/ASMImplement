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
        method.instructions.clear();
        
        method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
        method.instructions.add(new IntInsnNode(Opcodes.BIPUSH, 100));
        method.instructions.add(new InsnNode(Opcodes.IADD));
        method.instructions.add(new InsnNode(Opcodes.IRETURN));
        
        
        // 添加本地变量表（可选）
        method.localVariables = new ArrayList<>();
        method.localVariables.add(new LocalVariableNode(
            "data", "I", null, 
            new LabelNode(), new LabelNode(), 1
        ));
    }
}
```