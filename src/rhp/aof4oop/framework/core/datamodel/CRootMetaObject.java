package rhp.aof4oop.framework.core.datamodel;

public class CRootMetaObject
{
	private String containerName;	// String that identifies the root
	private String objectClassName;		// Object class name 
	private String objectClassVersion;	// Object class version
	private long[] logicalObjectId;			// Pointer to an Instance Meta-Object
	private byte type;			// OBJECT/ARRAY
	
	private static byte TYPE_OBJECT	=	1;
	private static byte TYPE_ARRAY	=	2;

	public CRootMetaObject(String containerName,String objectClassName,String objectClassVersion,long logicalObjectId) 
	{
		super();
		this.containerName = containerName;
		this.logicalObjectId = new long[]{logicalObjectId};
		this.type=TYPE_OBJECT;
		this.objectClassName=objectClassName;
		this.objectClassVersion=objectClassVersion;
	}
	public CRootMetaObject(String containerName,String objectClassName,String objectClassVersion,long[] logicalArrayId) 
	{
		super();
		this.containerName = containerName;
		this.logicalObjectId = logicalArrayId;
		this.type=TYPE_ARRAY;
		this.objectClassName=objectClassName;
		this.objectClassVersion=objectClassVersion;
	}
	public String getContainerName() 
	{
		return containerName;
	}
	public void setContainerName(String containerName) 
	{
		this.containerName = containerName;
	}
	public boolean isArray()
	{
		return (type==TYPE_ARRAY);
	}
	public long getLogicalObjectId() 
	{
		if(type==TYPE_OBJECT)
		{
			return logicalObjectId[0];
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}
	public long[] getLogicalArrayId() 
	{
		if(type==TYPE_ARRAY)
		{
			return logicalObjectId;
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}
	
	public String getObjectClassName() {
		return objectClassName;
	}
	public String getObjectClassVersion() {
		return objectClassVersion;
	}
	public String toString()
	{
		return "root["+containerName+"] = LOID{"+logicalObjectId+"}";
	}
}
