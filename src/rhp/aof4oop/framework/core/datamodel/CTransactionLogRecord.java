package rhp.aof4oop.framework.core.datamodel;

import java.lang.reflect.Field;

import rhp.aof4oop.framework.core.CPersistentRoot;
import rhp.aof4oop.framework.core.exceptions.EFrameworkFault;

/**
 * Records all data regarding an object member's state
 * @author rhp
 *
 */
public class CTransactionLogRecord 
{
	private String trxName;	// Name of transaction that performs the update
	private long   trxId;	// Trx's Id
	private long   trxLevel;	// Trx's Level
	private Object object;	// Object that owns the member
	private String memberName;	// member's name
	private Object value;	// member's value
	private Object previousValue;	// member's value
	
	
	public CTransactionLogRecord(String trxName,long trxId,int trxLevel, Object object, String memberName,Object value) 
	{
		super();
		this.trxName = trxName;
		this.trxId=trxId;
		this.trxLevel=trxLevel;
		this.object = object;
		this.memberName = memberName;
		this.value=value;
		try
		{
			Field f=CPersistentRoot.getField(object.getClass(), memberName);
			f.setAccessible(true);
			this.previousValue =f.get(object);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new EFrameworkFault(e);
		}
	}

	
	public long getTrxId() {
		return trxId;
	}


	public long getTrxLevel() {
		return trxLevel;
	}


	public String getTrxName() {
		return trxName;
	}


	public Object getObject() {
		return object;
	}


	public String getMemberName() {
		return memberName;
	}


	public Object getValue() {
		return value;
	}
	public Object getPreviousValue() 
	{
		return previousValue;
	}


	public String toString()
	{
		return trxName+" :: "+object.getClass().getCanonicalName()+" -> "+memberName+" = "+(value==null?"NULL":(value instanceof String || value instanceof Integer?value:value.getClass().getCanonicalName()));
	}
	public void rollback()
	{
		// For all datatypes, restore the member's value 
		try
		{
			Field f=CPersistentRoot.getField(object.getClass(), memberName);
			f.setAccessible(true);
			f.set(object,previousValue);
		}
		catch(Exception e)
		{
			throw new EFrameworkFault(e);	
		}
	}
}
