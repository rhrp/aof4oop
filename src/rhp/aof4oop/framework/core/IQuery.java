package rhp.aof4oop.framework.core;

import java.util.List;

import rhp.aof4oop.framework.core.exceptions.EFrameworkFault;

public interface IQuery 
{
	public <T> List<T> execute() throws EFrameworkFault;
}
