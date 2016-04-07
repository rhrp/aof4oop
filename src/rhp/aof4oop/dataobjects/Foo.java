package rhp.aof4oop.dataobjects;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "A")
public class Foo<T> 
{
	T foo;
	
	public T getFoo()
	{
		return foo;
	}
	public void setFoo(T foo)
	{
		this.foo=foo;
	}
}
