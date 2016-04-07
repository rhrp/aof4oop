package rhp.aof4oop.framework.core.datamodel;

public class CAttributeMetaObject extends CMetaObject
{
	private long parentObjectId;	// Logical OID of the parent
	private String member;			// Member name in parent object
	private long objectId;			// Logical OID of the linked object pointed by (parentObjectId,member)
	private String objectClassName;		// Object class name 
	private String objectClassVersion;	// Object class version
	private long[] arrayObjectId;			// Array of Logical OIDs
	private boolean typeArray;			// If true, is an array
	
	/**
	 * Create a normal link
	 * @param parentObjectId
	 * @param member
	 * @param objectId
	 * @param className
	 * @param version
	 */
	public CAttributeMetaObject(long parentObjectId, String member, long objectId,String className, String version)
	{
		super();
		this.parentObjectId = parentObjectId;
		this.member = member;
		this.objectId = objectId;
		this.objectClassName = className;
		this.objectClassVersion = version;
		this.arrayObjectId=null;
		this.typeArray=false;
	}
	/**
	 * Create an array link
	 * @param parentObjectId
	 * @param member
	 * @param objectId
	 * @param className
	 * @param version
	 */
	public CAttributeMetaObject(long parentObjectId, String member,String className, String version,long[] arrayObjectId)
	{
		super();
		this.parentObjectId = parentObjectId;
		this.member = member;
		this.objectId = 0;// Arrays do not have an instance meta-object
		this.objectClassName = className;
		this.objectClassVersion = version;
		this.arrayObjectId=arrayObjectId;
		this.typeArray=true;
	}
	public long getParentObjectId() 
	{
		return parentObjectId;
	}

	public String getMember() {
		return member;
	}

	public long getObjectId() {
		return objectId;
	}
	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}
	public String getObjectClassName() {
		return objectClassName;
	}
	public String getObjectVersion() {
		return objectClassVersion;
	}
	public String toObjectClassVersion()
	{
		return CClassVersionMetaObject.toVersionClassName(getObjectClassName(),getObjectVersion());
	}
	public void setArrayObjectId(long[] arrayObjectId)
	{
		this.arrayObjectId=arrayObjectId;
	}
	
	public long[] getArrayObjectId() {
		return arrayObjectId;
	}
	public boolean isTypeArray() 
	{
		return typeArray;
	}
	private String showArray()
	{
		if(arrayObjectId==null)
		{
			return "NULL";
		}
		else if(arrayObjectId.length==0)
		{
			return "";
		}
		else if(arrayObjectId.length<10)
		{
			String out="";
			for(int i=0;i<arrayObjectId.length;i++)
			{
				out+=(i==0?"":",")+arrayObjectId[i];
			}
			return out;
		}
		else
		{
			return "...";
		}
	}
	public static String calcKey(CAttributeMetaObject amo)
	{
		return calcKey(amo.getParentObjectId(),amo.getMember());
	}
	public static String calcKey(long parentLOID,String member)
	{
		return "loid("+parentLOID+")."+member;
	}
	public String calcKey()
	{
		return calcKey(getParentObjectId(),getMember());
	}
	public String toString()
	{
		if(isTypeArray())
		{
			return "ParentOID{"+getParentObjectId()+"}."+member+" array="+isTypeArray()+",size="+(arrayObjectId!=null?""+arrayObjectId.length:"NULL")+" --> LOID{"+showArray()+"}{"+getObjectClassName()+"$"+getObjectVersion()+"}";
		}
		else
		{
			return "ParentOID{"+getParentObjectId()+"}."+member+" array="+isTypeArray()+" --> LOID{"+getObjectId()+"}{"+getObjectClassName()+"$"+getObjectVersion()+"}";
		}
	}
}
