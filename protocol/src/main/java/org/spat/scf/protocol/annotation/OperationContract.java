package org.spat.scf.protocol.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * a annotation for mark method which can invoke from client
 * 
 *
 * @author Service Platform Architecture Team 
 * 
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationContract {
	public String methodName() default AnnotationUtil.DEFAULT_VALUE;
}