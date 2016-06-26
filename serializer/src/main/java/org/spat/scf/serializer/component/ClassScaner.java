/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.spat.scf.serializer.utility.ClassHelper;
import org.spat.scf.serializer.utility.FileHelper;
import org.spat.scf.serializer.utility.StrHelper;

/**
 *
 * @author Administrator
 */
class ClassScaner {

    private ClassLoader cl = Thread.currentThread().getContextClassLoader();
    private final String KEY_WORD = "SCFSerializable";//类文件中包含的关键字

    public Set<Class> scan(String... basePakages) throws URISyntaxException, IOException, ClassNotFoundException {
        Set<Class> classes = new LinkedHashSet<Class>();
        if (basePakages != null && basePakages.length > 0 && (!StrHelper.isEmptyOrNull(basePakages[0]))) {
            for (String pack : basePakages) {
                classes.addAll(scanByPakage(pack));
            }
        } else {
            System.err.println("开始扫描全部引用jar包，如果扫描过程过长请在启动vm参数中设置scf.serializer.basepakage或者设置scf.serializer.scantype=asyn使用异步模式扫描。");
            classes.addAll(scanByURLClassLoader());
            if (classes.size() == 0) {
                classes.addAll(scanByJarPath(ClassHelper.getJarPath(ClassScaner.class)));
            }
        }
        return classes;
    }

    /**
     * 从包package中获取所有的Class
     *
     * @param pack
     * @return
     */
    public Set<Class> scanByPakage(String pack) throws URISyntaxException, MalformedURLException, FileNotFoundException, ClassNotFoundException {

        // 第一个class类的集合   
        Set<Class> classes = new LinkedHashSet<Class>();
        // 获取包的名字 并进行替换
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    //String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    getClassFromURL(url, pack, classes);
                } else if ("jar".equals(protocol)) {
                    try {
                        // 获取jar
                        JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        classes.addAll(ClassHelper.GetClassFromJar(jar, KEY_WORD, pack));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    private Set<Class> scanByJarPath(String jarPath) throws IOException, ClassNotFoundException {
    	System.out.println("jarPath:" + jarPath);
        Set<Class> classes = new LinkedHashSet<Class>();
        List<File> jarFiles = FileHelper.getFiles(jarPath, "jar");
        if (jarFiles == null) {
            System.err.println("No find jar from path:" + jarPath);
        } else {
	        for (File f : jarFiles) {
	            classes.addAll(ClassHelper.GetClassFromJar(f.getPath(), KEY_WORD));
	        }
        }
        return classes;
    }

    private Set<Class> scanByURLClassLoader() throws URISyntaxException, IOException, ClassNotFoundException {
        Set<Class> classes = new LinkedHashSet<Class>();
        URL[] urlAry = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
        for (URL url : urlAry) {
        	if(!url.getPath().equalsIgnoreCase("/")) {
	        	System.out.println("scanByURLClassLoader:" + URLDecoder.decode(url.getPath(), "utf-8"));
	            if (url.getPath().endsWith(".jar")) {
	                classes.addAll(ClassHelper.GetClassFromJar(URLDecoder.decode(url.getPath(), "utf-8"), KEY_WORD));
	            } else {
	                getClassFromURL(url, "", classes);
	            }
        	}
        }
        return classes;
    }

    private void getClassFromURL(URL url, String basePak, Set<Class> classes) throws MalformedURLException, URISyntaxException, FileNotFoundException, IOException, ClassNotFoundException {
        if(url == null) {
        	System.err.println("url is null when getClassFromURL");
        	return;
        }
        String path = URLDecoder.decode(url.getPath(), "utf-8");
        if(path == null || path.equalsIgnoreCase("")) {
        	System.err.println("path is null when getClassFromURL (url:" + url + ")");
        	return;
        }
        
    	File f = new File(path);
        if (f.isDirectory()) {
            List<File> files = FileHelper.getFiles(f.getAbsolutePath(), "class");
            for (File file : files) {
                Class c = getClassFromFile(file, url, basePak);
                if (c != null) {
                    classes.add(c);
                }
            }
        } else if (f.getName().endsWith(".class")) {
            Class c = getClassFromFile(f, url, basePak);
            if (c != null) {
                classes.add(c);
            }
        }
    }

    private Class getClassFromFile(File f, URL baseURL, String basePak) throws ClassNotFoundException, URISyntaxException, FileNotFoundException, IOException {
    	if (!isSerializable(f)) {
            return null;
        }
        String filePath = f.getAbsolutePath();
        filePath = filePath.replace("\\", ".");
        String dirPath = baseURL.toURI().getPath();
        if(dirPath==null){
        	return null;
        }
        if (dirPath.startsWith("/")) {
            dirPath = dirPath.substring(1);
        }
        dirPath = dirPath.replace("/", ".");
        filePath = filePath.replace(dirPath, "");
        if (filePath.endsWith(".class")) {
            filePath = filePath.substring(0, filePath.length() - ".class".length());
        }
        Class c = cl.loadClass(basePak + filePath);
        return c;
    }

    private static boolean isSerializable(File f) throws FileNotFoundException, IOException {
        if (!f.getAbsolutePath().endsWith(".class")) {
            return false;
        }
        boolean result = false;
        StringBuffer sb = new StringBuffer();
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(f);
            br = new BufferedReader(fr);
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
                if (sb.indexOf("SCFSerializable") > -1) {
                    result = true;
                    break;
                }
            }
        } finally {
            if (fr != null) {
                fr.close();
            }
            if (br != null) {
                br.close();
            }
        }
        return result;
    }
}
