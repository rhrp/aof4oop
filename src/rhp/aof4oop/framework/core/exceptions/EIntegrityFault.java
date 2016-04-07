package rhp.aof4oop.framework.core.exceptions;

public class EIntegrityFault extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4373659305506020175L;

	public EIntegrityFault() 
	{
		super();
	}
	public EIntegrityFault(String message, Throwable cause) 
	{
		super(message, cause);
	}
	public EIntegrityFault(String message) 
	{
		super(message);
	}
	public EIntegrityFault(Throwable cause) 
	{
		super(cause);
	}
}
