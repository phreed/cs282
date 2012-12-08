package edu.vanderbilt.cs282.feisele.lab06.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation makes it possible to specify where software patterns are
 * used.
 * 
 * 
 * Used to determine the effectiveness of design pattern implementation.
 * 
 * <p>
 * For example:
 * 
 * <code>
 * DesignPattern ( name = "foo", 
 *    namespace = "posa2", 
 *    pattern = "proactor" 
 *    role = "completion-handler")</code>
 * 
 * <p>
 * The fully qualified name for the design pattern instance is (namespace, pattern, name).
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR,
		ElementType.ANNOTATION_TYPE, ElementType.PACKAGE, ElementType.FIELD,
		ElementType.LOCAL_VARIABLE })
@Documented
public @interface DesignPattern {

	/** the pattern instance name */
	String name();

	/**
	 * the name of the defining source
	 * <dl>
	 * <dt>gof</dt>
	 * <dd>"Design Patterns", Gamma et al</dd>
	 * <dt>posa2</dt>
	 * <dd>"Pattern-Oriented Software Architecture, V2", Schmidt et al</dd>
	 * </dl>
	 */
	String namespace() default "gof";

	/** the primary name */
	String pattern() default "strategy";

	/** the context specific name */
	String alias() default "";

	/** the pattern specific role played by objects of this class */
	String role() default "";

}
