package org.spat.scf.protocol.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * a annotation for mark implement class
 * 
 *
 * @author Service Platform Architecture Team 
 * 
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceBehavior {
	public String lookUP() default AnnotationUtil.DEFAULT_VALUE;
}