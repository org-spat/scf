package org.spat.scf.server.deploy.hotdeploy;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A ClassHelper for get class from jar
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class ClassHelper {

	public static Set<Class<?>> getClassFromJar(String jarPath, String... regex) throws IOException, ClassNotFoundException {
        JarFile jarFile = new JarFile(jarPath); // read jar file
        Enumeration<JarEntry> entries = jarFile.entries();
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            String name = jarEntry.getName();
            if (name.endsWith(".class")) {
                String className = name.replaceAll(".class", "").replaceAll("/", ".");
                Class<?> type = null;
                try {
                    type = Class.forName(className);
                } catch (Throwable ex) {
                	
                }
                if (type != null) {
                    classes.add(type);
                }
            }
        }
        jarFile.close();
        return classes;
    }
}