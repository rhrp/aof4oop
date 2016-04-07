package rhp.aof4oop.framework.core.annotations;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * TODO::008: The Aof4oopPersistent annotation does not work. See AspectJ limitation for advice local variables.
 * @author rhp
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.LOCAL_VARIABLE})
public @interface Aof4oopPersistent 
{
	String key();
}