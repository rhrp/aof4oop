package rhp.aof4oop.framework.core;

import java.util.List;

import rhp.aof4oop.framework.core.exceptions.EFrameworkFault;

/**
 * Simple query that obtains all instances of a specific class
 * 
 * @author rhp
 *
 */
public class CQuery implements IQuery
{
	private String className;
	
	public CQuery(Class<?> clazz)
	{
		this.className=clazz.getCanonicalName();
	}
	public String getClassName() 
	{
		return className;
	}
	@SuppressWarnings("unchecked")
	public <T> List<T> execute() throws EFrameworkFault
	{
		//Call this other method to avoid problens with pointcut aspect sintaxe 
		return (List<T>) executeQuery();
	}
	private List<?> executeQuery() throws EFrameworkFault
	{
		//This must be executed inside the Storage Aspect
		return null;
	}
	public String toString()
	{
		return "Querie all objects from class "+className;
	}
}
