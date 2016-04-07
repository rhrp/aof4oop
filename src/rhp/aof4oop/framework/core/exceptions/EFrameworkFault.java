package rhp.aof4oop.framework.core.exceptions;

public class EFrameworkFault extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public EFrameworkFault() 
	{
		super();
	}
	public EFrameworkFault(String message, Throwable cause) 
	{
		super(message, cause);
	}
	public EFrameworkFault(String message) 
	{
		super(message);
	}
	public EFrameworkFault(Throwable cause) 
	{
		super(cause);
	}
}
