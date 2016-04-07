package rhp.aof4oop.framework.core.annotations;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Aof4oopCheck 
{
	String expression();
}
