package org.spat.scf.client.proxy.component;

/**
 * invoke result
 * <typeparam name="T">result data type</typeparam>
 */
public class InvokeResult<T> {

    public InvokeResult(Object result, Object[] outPara) {
        Result = (T) result;
        OutPara = outPara;
    }
    private T Result;
    private Object[] OutPara;

    public Object[] getOutPara() {
        return OutPara;
    }

    public void setOutPara(Object[] OutPara) {
        this.OutPara = OutPara;
    }

    public T getResult() {
        return Result;
    }

    public void setResult(T Result) {
        this.Result = Result;
    }
}
