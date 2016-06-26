package org.spat.scf.serializer.serializer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spat.scf.serializer.annotation.SCFMember;
import org.spat.scf.serializer.annotation.SCFNotMember;
import org.spat.scf.serializer.annotation.SCFSerializable;
import org.spat.scf.serializer.component.SCFInStream;
import org.spat.scf.serializer.component.SCFOutStream;
import org.spat.scf.serializer.exception.ClassNoMatchException;
import org.spat.scf.serializer.exception.DisallowedSerializeException;
import org.spat.scf.serializer.utility.StrHelper;
import org.spat.scf.serializer.utility.TypeHelper;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Administrator
 */
class ObjectSerializer extends SerializerBase {

    @Override
    public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
        if (obj == null) {
            SerializerFactory.GetSerializer(null).WriteObject(null, outStream);
            return;
        }
        Class<?> type = obj.getClass();
        TypeInfo typeInfo = GetTypeInfo(type);
        outStream.WriteInt32(typeInfo.TypeId);
        if (outStream.WriteRef(obj)) {
            return;
        }
        Integer fsortid = 1;
        for (Field f : typeInfo.Fields) {
            Object value = f.get(obj);
            SCFMember ann = f.getAnnotation(SCFMember.class);
            if(ann == null) {
            	throw new Exception("This Field can not find @SCFMember:" + f.getType().getSimpleName());
            }
            if (value == null) {  	
            	fsortid++;//写入值为null的对象也需要标号 用于兼容老版本
                SerializerFactory.GetSerializer(null).WriteObject(null, outStream);
            } else {
                if (value instanceof ISCFSerializer) {
                    ((ISCFSerializer) value).Serialize(outStream);
                } else {
                    Class valueType = value.getClass();
                    outStream.WriteInt32(TypeHelper.GetTypeId(valueType));
                    if(ann.sortId() != -1) {
                    	outStream.WriteInt32(ann.sortId());
                    }else {
                    	//兼容老版本， 按顺序写入编号
                    	outStream.WriteInt32(fsortid++);
                    }
                    SerializerFactory.GetSerializer(valueType).WriteObject(value, outStream);
                }
            }
        }
    }

    @Override
    public Object ReadObject(SCFInStream inStream, Class defType) throws Exception {
        int typeId = inStream.ReadInt32();
        if (typeId == 0) {
            return null;
        }
        Class type = TypeHelper.GetType(typeId);
        
        if (type == null) {
            throw new ClassNotFoundException("Cannot find class with typId,target class:" + defType.getName() + ",typeId:" + typeId);
        }
        if (!defType.isAssignableFrom(type) && defType != type) {
            throw new ClassNoMatchException("Class not match!class:" + type.getName() + ",require " + defType.getName());
        }
        byte isRef = (byte) inStream.read();
        int hashcode = inStream.ReadInt32();
        if (isRef > 0) {
            return inStream.GetRef(hashcode);
        }
        TypeInfo typeInfo = GetTypeInfo(type);
        Object obj = type.newInstance();
        Integer index = 1;
        /*
         * 为了兼容protocol-1.6.0和1.6.1包中ResponseProtocol类的 sortid不一致导致的bug
         * 从二进制流中需要读取ResponseProtocol类时强制尝试两种转换方式
        */
        //*/
        SCFInStream inStream_bak = inStream.Clone();
        try {
        	for(int i = 0; i < typeInfo.Fields.size(); i++) {
            	Field f = typeInfo.Fields.get(i);
            	if(inStream == null || inStream.available() == 0) {
            		break;
            	}
                int ptypeId = inStream.ReadInt32();
                if (ptypeId == 0) {
                	index++; //读取值为null的对象 用于兼容老版本
                    f.set(obj, null);
                    continue;
                }
                int sortID = inStream.ReadInt32();
                
                SCFMember ann = f.getAnnotation(SCFMember.class);
                if(ann == null) {
                	throw new Exception("This Field can not find @SCFMember:" + f.getType().getSimpleName());
                }else {
                	if(ann.sortId() != -1) {
    	            	while(sortID != ann.sortId()){
    	            		if(i >= typeInfo.Fields.size() - 1) {
    	            			break;
    	            		}
    	            		f = typeInfo.Fields.get(++i);
    	            		ann = f.getAnnotation(SCFMember.class);
    	            		if(ann == null) {
    	            			throw new Exception("This Field can not find @SCFMember:" + f.getType().getSimpleName());
    	            		}
    	            	}
                	}else {
                		//兼容老版本， 按顺序读取编号
                		while(sortID != index++) {
                			f = typeInfo.Fields.get(++i);
                			if(i >= typeInfo.Fields.size()) {
                				break;
                			}
                		}
                	}
                	Class ptype = TypeHelper.GetType(ptypeId);
                    if (ptype == null) {
                    	throw new ClassNotFoundException("Cannot find class with typId,target class: " + f.getType().getName() + ",typeId:" + ptypeId);
                    }
                    if (ISCFSerializer.class.isAssignableFrom(ptype)) {
                    	ISCFSerializer value = (ISCFSerializer) ptype.newInstance();
                    	value.Derialize(inStream);
                    	f.set(obj, value);
                    } else {
                    	Object value = SerializerFactory.GetSerializer(ptype).ReadObject(inStream, f.getType());
                    	f.set(obj, value);
                    }
                	
                }
            }
		} catch (Exception e) {
			if(typeId == 2100563169) {
				inStream.close();
				inStream = inStream_bak;
				try {
					for(int i = typeInfo.Fields.size() - 1; i >= 0 ; i--) {
			        	Field f = typeInfo.Fields.get(i);
			        	if(inStream == null || inStream.available() == 0) {
			        		break;
			        	}
			            int ptypeId = inStream.ReadInt32();
			            if (ptypeId == 0) {
			            	index++; //读取值为null的对象 用于兼容老版本
			                f.set(obj, null);
			                continue;
			            }
			            int sortID = inStream.ReadInt32();
			            
			            SCFMember ann = f.getAnnotation(SCFMember.class);
			            if(ann == null) {
			            	throw new Exception("This Field can not find @SCFMember:" + f.getType().getSimpleName());
			            }else {
			            	if(ann.sortId() != -1) {
//				            	while(sortID != ann.sortId()){
//				            		if(i >= typeInfo.Fields.size() - 1) {
//				            			break;
//				            		}
//				            		f = typeInfo.Fields.get(++i);
//				            		ann = f.getAnnotation(SCFMember.class);
//				            		if(ann == null) {
//				            			throw new Exception("This Field can not find @SCFMember:" + f.getType().getSimpleName());
//				            		}
//				            	}
			            	}else {
			            		//兼容老版本， 按顺序读取编号
			            		while(sortID != index++) {
			            			f = typeInfo.Fields.get(++i);
			            			if(i >= typeInfo.Fields.size()) {
			            				break;
			            			}
			            		}
			            	}
			            	Class ptype = TypeHelper.GetType(ptypeId);
			                if (ptype == null) {
			                	throw new ClassNotFoundException("Cannot find class with typId,target class: " + f.getType().getName() + ",typeId:" + ptypeId);
			                }
			                if (ISCFSerializer.class.isAssignableFrom(ptype)) {
			                	ISCFSerializer value = (ISCFSerializer) ptype.newInstance();
			                	value.Derialize(inStream);
			                	f.set(obj, value);
			                } else {
			                	Object value = SerializerFactory.GetSerializer(ptype).ReadObject(inStream, f.getType());
			                	f.set(obj, value);
			                }
			            	
			            }
			        }
				} catch (Exception ex) {
					throw ex;
				}
			} else {
				throw e;
			}
		}
        /*/
		for(int i = 0; i < typeInfo.Fields.size(); i++) {
        	Field f = typeInfo.Fields.get(i);
        	if(inStream == null || inStream.available() == 0) {
        		break;
        	}
            int ptypeId = inStream.ReadInt32();
            if (ptypeId == 0) {
            	index++; //读取值为null的对象 用于兼容老版本
                f.set(obj, null);
                continue;
            }
            int sortID = inStream.ReadInt32();
            
            SCFMember ann = f.getAnnotation(SCFMember.class);
            if(ann == null) {
            	throw new Exception("This Field can not find @SCFMember:" + f.getType().getSimpleName());
            }else {
            	if(ann.sortId() != -1) {
	            	while(sortID != ann.sortId()){
	            		if(i >= typeInfo.Fields.size() - 1) {
	            			break;
	            		}
	            		f = typeInfo.Fields.get(++i);
	            		ann = f.getAnnotation(SCFMember.class);
	            		if(ann == null) {
	            			throw new Exception("This Field can not find @SCFMember:" + f.getType().getSimpleName());
	            		}
	            	}
            	}else {
            		//兼容老版本， 按顺序读取编号
            		while(sortID != index++) {
            			f = typeInfo.Fields.get(++i);
            			if(i >= typeInfo.Fields.size()) {
            				break;
            			}
            		}
            	}
            	Class ptype = TypeHelper.GetType(ptypeId);
                if (ptype == null) {
                	throw new ClassNotFoundException("Cannot find class with typId,target class: " + f.getType().getName() + ",typeId:" + ptypeId);
                }
                if (ISCFSerializer.class.isAssignableFrom(ptype)) {
                	ISCFSerializer value = (ISCFSerializer) ptype.newInstance();
                	value.Derialize(inStream);
                	f.set(obj, value);
                } else {
                	Object value = SerializerFactory.GetSerializer(ptype).ReadObject(inStream, f.getType());
                	f.set(obj, value);
                }
            	
            }
        }
        //*/
        
        
        
        inStream.SetRef(hashcode, obj);
        return obj;
    }
    private static Map<Class<?>, TypeInfo> TypeInfoMap = new HashMap<Class<?>, TypeInfo>();

    private TypeInfo GetTypeInfo(Class<?> type) throws ClassNotFoundException, DisallowedSerializeException {
        if (TypeInfoMap.containsKey(type)) {
            return TypeInfoMap.get(type);
        }
        SCFSerializable cAnn = type.getAnnotation(SCFSerializable.class);
        if (cAnn == null) {
            throw new DisallowedSerializeException();
        }
        int typeId = TypeHelper.GetTypeId(type);
        TypeInfo typeInfo = new TypeInfo(typeId);
        //Field[] fields = type.getDeclaredFields();
        ArrayList<Field> fields = new ArrayList<Field>();
        ArrayList<Class> cls = new ArrayList<Class>();
        Class<?> temType = type;
        while(temType.getSuperclass() != null) {
        	cls.add(temType);
        	temType = temType.getSuperclass();
        }
        for(int i = cls.size(); i > 0; i--) {
        	Class temType1 = cls.get(i - 1);
            Field[] fs = temType1.getDeclaredFields();
            for (Field f : fs) {
                fields.add(f);
            }
        }

        Map<Integer, Field> mapFildes = new HashMap<Integer, Field>();
        List<Integer> indexIds = new ArrayList<Integer>();
        if (cAnn.defaultAll()) {
            for (Field f : fields) {
                SCFNotMember ann = f.getAnnotation(SCFNotMember.class);
                if (ann != null) {
                    continue;
                }
                f.setAccessible(true);
                Integer indexId = StrHelper.GetHashcode(f.getName().toLowerCase());
                mapFildes.put(indexId, f);
                indexIds.add(indexId);
            }
        } else {
        	Integer indexId = 1;
            for (Field f : fields) {
                SCFMember ann = f.getAnnotation(SCFMember.class);
                if (ann == null) {
                    continue;
                }
                f.setAccessible(true);
                String name = ann.name();

                if (ann.name() == null || ann.name().length() == 0) {
                    name = f.getName();
                } 
                /*
                 * 2013-5-10修改，支持服务器端增加多个字段客户端不需要更新功能
                 */
               try{
                    if(ann.sortId() != -1) {
                    	indexIds.add(ann.sortId());
                    	mapFildes.put(ann.sortId(), f);
                    }else {
                    	//兼容老版本 没有标注sortId的按顺序放入List
                    	mapFildes.put(indexId, f);
                    	indexIds.add(indexId);
                    	indexId++;
                    }         
                }catch(Exception e) {
                	e.printStackTrace();
                }
            }
        }
        int len = indexIds.size();
        for (int i = 0; i < len; i++) {
            for (int j = i + 1; j < len; j++) {
                Integer item = indexIds.get(j);
                if (indexIds.get(i) > item) {
                    indexIds.set(j, indexIds.get(i));
                    indexIds.set(i, item);
                }
            }
        }
        for (Integer sortID : indexIds) {
        	typeInfo.Fields.add(mapFildes.get(sortID));
        }
        TypeInfoMap.put(type, typeInfo);
        return typeInfo;
    }
}

class TypeInfo {

    public int TypeId;

    public TypeInfo(int typeId) {
        TypeId = typeId;
    }
    public List<Field> Fields = new ArrayList<Field>();
}
