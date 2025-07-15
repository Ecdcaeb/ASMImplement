package io.github.ecdcaeb.gradle.asmImplement;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
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
        ClassReader cr = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode, ClassReader.EXPAND_FRAMES);

        boolean modified = false;
        for (MethodNode method : classNode.methods) {
            AnnotationNode asmAnnotation = findAsmAnnotation(method);
            if (asmAnnotation != null) {
                String generatorClass = getAnnotationValue(asmAnnotation, "generatorClass");
                String generatorMethod = getAnnotationValue(asmAnnotation, "generatorMethod");
                
                if (generatorClass != null && generatorMethod != null) {
                    invokeGenerator(generatorClass, generatorMethod, method, classNode);
                    
                    removeAsmAnnotation(method);
                    
                    method.access &= ~(Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT);
                    
                    modified = true;
                }
            }
        }

        if (modified) {
            ClassWriter cw = new ClassWriter(0);
            classNode.accept(cw);
            Files.write(classFile.toPath(), cw.toByteArray());
        }
    }

    private AnnotationNode findAsmAnnotation(MethodNode method) {
        if (method.visibleAnnotations == null) return null;
        
        for (AnnotationNode ann : method.visibleAnnotations) {
            if (ANNOTATION_DESC.equals(ann.desc)) {
                return ann;
            }
        }
        return null;
    }

    private String getAnnotationValue(AnnotationNode annotation, String key) {
        if (annotation.values == null) return null;
        
        for (int i = 0; i < annotation.values.size(); i += 2) {
            if (key.equals(annotation.values.get(i))) {
                return (String) annotation.values.get(i + 1);
            }
        }
        return null;
    }

    private void removeAsmAnnotation(MethodNode method) {
        if (method.visibleAnnotations == null) return;
        
        Iterator<AnnotationNode> it = method.visibleAnnotations.iterator();
        while (it.hasNext()) {
            AnnotationNode ann = it.next();
            if (ANNOTATION_DESC.equals(ann.desc)) {
                it.remove();
                break;
            }
        }
    }

    private void invokeGenerator(String className, String methodName, MethodNode targetMethod, ClassNode classNode) {
        try {
            Class<?> generatorClass = Class.forName(className, true, classLoader);
            for (Method method : generatorClass.getDeclaredMethods()) {
                if ((method.getModifiers() & Modifier.STATIC) != 0 && methodName.equals(method.getName())) {
                    if (method.getParameterCount() == 1) {
                        method.invoke(null, targetMethod);
                    } else if (method.getParameterCount() == 2) {
                        method.invoke(null, targetMethod, classNode);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke generator: " + className + "." + methodName, e);
        }
    }
}