package io.github.ecdcaeb.asmImplement;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.artifacts.Configuration;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ASMImplementPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().withType(JavaCompile.class).configureEach(compile -> {
            compile.doLast("processASMAnnotations", task -> {
                try {
                    for (File file : project.getConfigurations().getByName("runtimeClasspath").resolve()) {
                        urls.add(file.toURI().toURL());
                    }

                    for (File file : project.getConfigurations().getByName("compileClasspath").resolve()) {
                        urls.add(file.toURI().toURL());
                    }

                    urls.add(compile.getDestinationDirectory().get().getAsFile().toURI().toURL());
                    
                    URLClassLoader loader = new URLClassLoader(
                        urls.toArray(new URL[0]),
                        getClass().getClassLoader()
                    );
                    
                    new AsmMethodProcessor(loader).processDirectory(
                        compile.getDestinationDirectory().get().getAsFile()
                    );
                } catch (Exception e) {
                    throw new RuntimeException("ASM processing failed", e);
                }
            });
        });
    }
}