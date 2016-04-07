package rhp.aof4oop.oo7.datamodel;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "S3")
public class DummyClass extends Assembly 
{
	public DummyClass()
	{
		super();
	}
	public DummyClass(int id, String type, long buildDate) 
	{
		super(id, type, buildDate);
	}
}
