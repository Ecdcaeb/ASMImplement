package io.github.ecdcaeb.gradle.asmImplement;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class ASMImplementPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().withType(JavaCompile.class).configureEach(compile -> {
            compile.doLast("processASMAnnotations", task -> {
                try {
                    ArrayList<URL> urls = new ArrayList<>();

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
                    
                    new ASMMethodProcessor(loader).processDirectory(
                        compile.getDestinationDirectory().get().getAsFile()
                    );
                } catch (Exception e) {
                    throw new RuntimeException("ASM processing failed", e);
                }
            });
        });
    }
}