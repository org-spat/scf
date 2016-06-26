/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.component;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spat.scf.serializer.annotation.SCFSerializable;
import org.spat.scf.serializer.classes.DBNull;
import org.spat.scf.serializer.classes.GKeyValuePair;
import org.spat.scf.serializer.exception.DisallowedSerializeException;
import org.spat.scf.serializer.utility.StrHelper;

/**
 *
 * @author Administrator
 */
public final class TypeMap {

    private static Map<Class, ClassItem> TypeIdMap = new HashMap<Class, ClassItem>();
    private static Map<Integer, ClassItem> IdTypeMap = new HashMap<Integer, ClassItem>();

    public static void InitTypeMap() {
        TypeIdMap.clear();
        IdTypeMap.clear();
        ArrayList<ClassItem> ClassList = new ArrayList<ClassItem>();
        ClassList.add(new ClassItem(1, DBNull.class));//????
        ClassList.add(new ClassItem(2, Object.class));
        ClassList.add(new ClassItem(3, Boolean.class, boolean.class));
        ClassList.add(new ClassItem(4, Character.class, char.class));
        ClassList.add(new ClassItem(5, Byte.class, byte.class));
        ClassList.add(new ClassItem(7, Short.class, short.class));
        ClassList.add(new ClassItem(9, Integer.class, int.class));
        ClassList.add(new ClassItem(11, Long.class, long.class));
        ClassList.add(new ClassItem(13, Float.class, float.class));
        ClassList.add(new ClassItem(14, Double.class, double.class));
        ClassList.add(new ClassItem(15, BigDecimal.class));
        ClassList.add(new ClassItem(16, Date.class, java.sql.Date.class, java.sql.Time.class, java.sql.Timestamp.class));
        ClassList.add(new ClassItem(18, String.class));
        ClassList.add(new ClassItem(19, List.class));
        ClassList.add(new ClassItem(22, GKeyValuePair.class));//????
        ClassList.add(new ClassItem(23, Array.class));
        ClassList.add(new ClassItem(24, Map.class));
//        新添加
        ClassList.add(new ClassItem(26, Set.class));
        
        for (ClassItem item : ClassList) {
            int id = item.getTypeId();
            Class[] types = item.getTypes();
            for (Class c : types) {
                TypeIdMap.put(c, item);
            }
            IdTypeMap.put(id, item);
        }

        String scanType = System.getProperty("scf.serializer.scantype");
        if (scanType != null && scanType.equals("asyn")) {
            Thread th = new Thread(new Runnable() {

                @Override
                public void run() {
                    System.out.println("Scan jar files begin!");
                    try {
                        LoadCustmeType();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    System.out.println("Scan jar files completed!");
                }
            });
            th.start();
        } else {
            System.out.println("Scan jar files begin!");
            try {
                LoadCustmeType();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("Scan jar files completed!");
        }
    }

    private static void LoadCustmeType() throws URISyntaxException, IOException, MalformedURLException, ClassNotFoundException {
        ClassScaner cs = new ClassScaner();
        String basePakage = System.getProperty("scf.serializer.basepakage");
        System.out.println("scf.serializer.basepakage ");
        if (basePakage == null) {
            basePakage = StrHelper.EmptyString;
        }
        @SuppressWarnings("rawtypes")
		Set<Class> classes = cs.scan(basePakage.split(";"));
        for (Class c : classes) {
            System.out.println("scaning " + c.getPackage().getName() + "." + c.getName());
            try {
                SCFSerializable ann = (SCFSerializable) c.getAnnotation(SCFSerializable.class);
                if (ann != null) {
                    String name = ann.name();
                    if (name.equals(StrHelper.EmptyString)) {
                        name = c.getSimpleName();
                    }
                    int typeId = StrHelper.GetHashcode(name);
                    TypeIdMap.put(c, new ClassItem(typeId, c));
                    IdTypeMap.put(typeId, new ClassItem(typeId, c));
                    System.out.println("scf SCFSerializable entity :" + name + "  typeId :" + typeId);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static Class getClass(int typeId) {
        /*****************兼容之前版本***********************/
        if(typeId==6){
            typeId =5;
        }else if(typeId==20 || typeId == 21){
            typeId = 19;
        }else if(typeId == 25){
            typeId = 24;
        }
        /**************************************************/
        ClassItem ci = IdTypeMap.get(typeId);
        if (ci != null) {
            return ci.getType();
        }
        return null;
    }
    
    /**
     * TypeIdMap 中根据Class type取出typeId
     * */
    public static int getTypeId(Class type) throws DisallowedSerializeException {
        int typeId = 0;
        if (type.isArray()) {
            type = Array.class;
        } else if (Map.class.isAssignableFrom(type)) {
            type = Map.class;
        } else if (List.class.isAssignableFrom(type)) {
            type = List.class;
        } else if (Set.class.isAssignableFrom(type)) {
        	type = Set.class;
        }
        ClassItem ci = TypeIdMap.get(type);
        if (ci != null) {
            typeId = ci.getTypeId();
        } else {
            SCFSerializable ann = (SCFSerializable) type.getAnnotation(SCFSerializable.class);
            if (ann == null) {
                throw new DisallowedSerializeException(type);
            }
            String name = ann.name();
            if (name.equals(StrHelper.EmptyString)) {
                name = type.getSimpleName();
            }
            typeId = StrHelper.GetHashcode(name);
            setTypeMap(type, typeId);
        }
        return typeId;
    }

    public static void setTypeMap(Class type, int typeId) {
        ClassItem ci = new ClassItem(typeId, type);
        TypeIdMap.put(type, ci);
        IdTypeMap.put(typeId, ci);
    }
}

class ClassItem {

    private Class[] Types;
    private int TypeId;

    public ClassItem(int typeids, Class... types) {
        Types = types;
        TypeId = typeids;
    }

    /**
     * @return the Types
     */
    public Class getType() {
        return Types[0];
    }

    public Class[] getTypes() {
        return Types;
    }

    /**
     * @return the TypeId
     */
    public int getTypeId() {
        return TypeId;
    }
}
