package org.spat.scf.server.convert;

/**
 * a interface for description convert object to target type
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public interface IConvert {
	
	public String convertToString(Object obj);
	
	public int convertToint(Object obj);
	
	public Integer convertToInteger(Object obj);
	
	public long convertTolong(Object obj);
	
	public Long convertToLong(Object obj);

	public short convertToshort(Object obj);
	
	public Short convertToShort(Object obj);

	public float convertTofloat(Object obj);
	
	public Float convertToFloat(Object obj);

	public boolean convertToboolean(Object obj);
	
	public Boolean convertToBoolean(Object obj);
	
	public double convertTodouble(Object obj);
	
	public Double convertToDouble(Object obj);
	
	public byte convertTobyte(Object obj);
	
	public Byte convertToByte(Object obj);
	
	public char convertTochar(Object obj);
	
	public Character convertToCharacter(Object obj);
	
	public Object convertToT(Object obj, Class<?> clazz) throws Exception;
	
	public Object convertToT(Object obj, Class<?> containClass, Class<?> itemClass) throws Exception;

//	添加
	public Object convertToT(Object obj, String clazz) throws Exception;
}