package io.github.ecdcaeb.gradle.asmImplement;

import io.github.ecdcaeb.gradle.asmImplement.lib.asm.*;
import io.github.ecdcaeb.gradle.asmImplement.lib.asm.commons.GeneratorAdapter;

import java.io.*;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.Method;
import java.util.stream.Stream;

public class ASMMethodProcessor {
    private static final String ANNOTATION_DESC = "Lio/github/ecdcaeb/asmImplement/ASMImplement;";
    private final ClassLoader classLoader;
    
    public ASMMethodProcessor(ClassLoader loader) {
        this.classLoader = loader;
    }

    public void processDirectory(File classDir) throws Exception {
        try (Stream<Path> walk = Files.walk(classDir.toPath())) {
            walk.filter(path -> path.toString().endsWith(".class"))
                    .forEach(path -> {
                        try {
                            processClassFile(path.toFile());
                        } catch (Exception e) {
                            throw new RuntimeException("Error processing " + path, e);
                        }
                    });
        }
    }

    private void processClassFile(File classFile) throws Exception {
        byte[] bytes = Files.readAllBytes(classFile.toPath());

        final HashMap<String, String[]> methodsToImplement = new HashMap<>();
        {
            ClassReader scanner = new ClassReader(bytes);
            scanner.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                            if (ANNOTATION_DESC.equals(descriptor)) {
                                final String[] str = methodsToImplement.computeIfAbsent(name + desc, (key) -> new String[2]);
                                return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override
                                    public void visit(String name, Object value) {
                                        if ("generatorClass".equals(name)) str[0] = (String) value;
                                        else if ("generatorMethod".equals(name)) str[1] = (String) value;
                                    }
                                };
                            } else return super.visitAnnotation(descriptor, visible);
                        }
                    };
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
        }

        if (!methodsToImplement.isEmpty()) {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(0);
            reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    String methodKey = name + desc;
                    if (methodsToImplement.containsKey(methodKey)) {

                        int newAccess = access & ~(Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT);
                        MethodVisitor mv = super.visitMethod(newAccess, name, desc, signature, exceptions);

                        return new GeneratorAdapter(Opcodes.ASM9, mv, newAccess, name, desc) {
                            @Override
                            public void visitCode() {
                                super.visitCode();
                                String[] strings = methodsToImplement.get(methodKey);
                                ASMMethodProcessor.this.invokeGenerator(strings[0], strings[1], this);
                            }
                        };
                    }
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
            }, 0);
            Files.write(classFile.toPath(), writer.toByteArray());
        }
    }

    private void invokeGenerator(String className, String methodName, MethodVisitor methodVisitor) {
        try {
            Class<?> generatorClass = Class.forName(className, true, this.classLoader);
            for (Method method : generatorClass.getDeclaredMethods()) {
                if ((method.getModifiers() & Modifier.STATIC) != 0 && methodName.equals(method.getName())) {
                    method.invoke(null, methodVisitor);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke generator: " + className + "." + methodName, e);
        }
    }
}