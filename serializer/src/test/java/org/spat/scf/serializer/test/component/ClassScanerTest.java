/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.test.component;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 * @author Administrator
 */
public class ClassScanerTest {

    public ClassScanerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testScan() throws Exception {
//        List<Class> result = new ArrayList<Class>();
//        ClassLoader cl = Thread.currentThread().getContextClassLoader();
//        ClassScaner cs = new ClassScaner();
//        List<File> classes = cs.scan("org.spat");
//        for (File f : classes) {
//            String cpath = f.getPath().replace(File.separator, ".");
//            cpath = cpath.substring(cpath.lastIndexOf("org.spat"));
//            cpath = cpath.substring(0, cpath.length() - ".class".length());
//            Class c = cl.loadClass(cpath);
//            SCFSerializable ann = (SCFSerializable) c.getAnnotation(SCFSerializable.class);
//            if (ann != null) {
//                result.add(c);
//            }
//        }
    }
}
