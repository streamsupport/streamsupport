package build;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link RetentionPolicy#CLASS} annotation for build time detection of
 * accidental use of APIs not compatible with Java 6 - <b>should not</b>
 * be included in the binary distribution.
 * 
 * @since 1.5.3
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface IgnoreJava8API {
}
