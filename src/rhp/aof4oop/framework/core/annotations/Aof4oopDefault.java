package rhp.aof4oop.framework.core.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Aof4oopDefault 
{
	String	value();
	String[]	classVersion();
}
