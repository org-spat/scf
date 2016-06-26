package org.spat.scf.protocol.entity;


public class Out<T> {
	private T outPara;

	public void setOutPara(T t) {
		this.outPara = t;
	}

	public T getOutPara() {
		return outPara;
	}

	public Out(T t){
		setOutPara(t);
	}
}