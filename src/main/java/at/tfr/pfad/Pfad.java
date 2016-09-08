package at.tfr.pfad;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation declares the annotated as for business use:<br>
 * - REST: use for getters, declares the designated property for REST update
 * 
 * @author thomas
 */

@Target({ METHOD, CONSTRUCTOR, FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Pfad {

}
