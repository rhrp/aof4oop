package rhp.aof4oop.framework.aspects;

public abstract aspect APersistence 
{
	protected void debugMsg(String msg)
	{
		System.out.println("["+this.getClass().getName()+"]::"+msg);
	}
	protected void debugException(Exception ex)
	{
		ex.printStackTrace();
	}
}
