
package org.spat.scf.client.proxy.component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.spat.scf.protocol.entity.Out;

/**
 * Parameter
 * @author Service Platform Architecture Team 
 */
public class Parameter {

    private Class<?> clazz;
    private Type type;
    private Object value;
    private ParaType paraType;
    private boolean isGeneric;
    private String simpleName;
    private Class<?> containerClass;
    private Class<?> itemClass;
    private Class<?> [] itemClass_;


    public Parameter(Object value, Class<?> clazz, Type type) throws ClassNotFoundException {
        this.setValue(value);
        this.setClazz(clazz);
        this.setType(type);
        this.setParaType(ParaType.In);
        init(value, clazz, type);
    }

    public Parameter(Object value, Class<?> clazz, Type type, ParaType ptype) throws ClassNotFoundException {
        this.setClazz(clazz);
        this.setType(type);
        this.setValue(value);
        this.setParaType(ptype);
        init(value, clazz, type);
    }
    
    /**
     * @param value null
     * @param clazz 方法返回类型(ex:interface java.util.Map)
     * @param type 方法 底层方法的正式返回类型的 Type 对象 (ex:java.util.Map<java.lang.String, java.lang.String>)
     * @throws ClassNotFoundException
     */
    //进行修改的函数
    private void init(Object value, Class<?> clazz, Type type) throws ClassNotFoundException {
        if (!clazz.equals(type) && !clazz.getCanonicalName().equalsIgnoreCase(type.toString())) {
        	
            String sn = "";
            if( value instanceof Out) {
            	String itemName = type.toString().replaceAll(clazz.getCanonicalName(), "").replaceAll("\\<", "").replaceAll("\\>", "");
                sn = itemName.substring(itemName.lastIndexOf(".") + 1);
            } else {
            	String itemName = type.toString();
            	sn = getSimpleParaName(itemName);
//    			兼容jdk1.6和jdk1.7
            	if (sn.indexOf("<[") !=-1 || sn.indexOf(";>") !=-1) {
            		sn = sn.replaceAll("\\[L", "").replaceAll("\\[", "");
            	}
			
            	if (sn.indexOf("[]>") !=-1 || sn.indexOf("[],") !=-1 ) {
            		sn = sn.replace("[]>", ";>").replace("[],", ";,");
            	}
            }
            this.setSimpleName(sn);
            this.setContainerClass(clazz);
            this.setIsGeneric(true);
        } else {
            this.setIsGeneric(false);
            this.setItemClass(clazz);
            this.setSimpleName(clazz.getSimpleName());
        }
     
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public ParaType getParaType() {
        return paraType;
    }

    public void setParaType(ParaType paraType) {
        this.paraType = paraType;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Class<?> getContainerClass() {
        return containerClass;
    }

    public void setContainerClass(Class<?> containerClass) {
        this.containerClass = containerClass;
    }

    public Class<?> getItemClass() {
        return itemClass;
    }

    public void setItemClass(Class<?> itemClass) {
        this.itemClass = itemClass;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public boolean isIsGeneric() {
        return isGeneric;
    }

    public void setIsGeneric(boolean isGeneric) {
        this.isGeneric = isGeneric;
    }

	public Class<?>[] getItemClass_() {
		return itemClass_;
	}

	public void setItemClass_(Class<?>[] itemClass_) {
		this.itemClass_ = itemClass_;
	}
    
	public static String getSimpleParaName(String paraName) {
		paraName = paraName.replaceAll(" ",	"");
		if(paraName.indexOf(".") > 0) {
			String[] pnAry = paraName.split("");
			List<String> originalityList = new ArrayList<String>();
			List<String> replaceList = new ArrayList<String>();
			
			String tempP = "";
			for(int i=0; i<pnAry.length; i++) {
				if(pnAry[i].equalsIgnoreCase("<")) {
					originalityList.add(tempP);
					replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
					tempP = "";
				} else if(pnAry[i].equalsIgnoreCase(">")) {
					originalityList.add(tempP);
					replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
					tempP = "";
				} else if(pnAry[i].equalsIgnoreCase(",")){
					originalityList.add(tempP);
					replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
					tempP = "";
				} else if(i == pnAry.length - 1){
					originalityList.add(tempP);
					replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
					tempP = "";
				} else {
					if(!pnAry[i].equalsIgnoreCase("[") && !pnAry[i].equalsIgnoreCase("]")) {
						tempP += pnAry[i];
					}
				}
			}
			
			for(int i=0; i<replaceList.size(); i++) {
				paraName = paraName.replaceAll(originalityList.get(i), replaceList.get(i));
			}
			return paraName;
		} else {
			return paraName;
		}
	}
}