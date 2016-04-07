package rhp.aof4oop.framework.core.datamodel;

import java.io.IOException;

import javassist.CannotCompileException;
import javassist.NotFoundException;

/**
 * TODO: classVersion and OID
 * @author rhp
 *
 */
public class CInstanceMetaObject extends CMetaObject
{
	private 	long Id;					// Logical Id
	private		String	classCanonicalName;	// Class canonical name of the instance
	private		String	className;			// Class name of the instance
	private		String	classVersion;		// Physical Version TODO: This field must be converted to a collection to accommodate several object versions
	private     String[] typeParameters;
	private		long	OID;			// Physical Id TODO: This field must be converted to a collection to accommodate several object versions
	
	private CInstanceMetaObject(long id,String classCanonicalName,String className, String classVersion,String[] typeParameters, long OID) 
	{
		super();
		this.Id = id;
		this.classCanonicalName=classCanonicalName;
		this.className = className;
		this.classVersion = classVersion;
		this.typeParameters=typeParameters;
		this.OID = OID;
	}
	public static CInstanceMetaObject factory(CClassVersionMetaObject ocv,String[] typeParameters,long OID) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		return new CInstanceMetaObject((int)OID+200000000,ocv.getClassCanonicalName(),ocv.getClassName(),ocv.getClassVersion(),typeParameters, OID);
	}
	public long getId() 
	{
		return Id;
	}
	
	public String getClassCanonicalName() 
	{
		return classCanonicalName;
	}
	public String getClassName() 
	{
		return className;
	}
	public String getClassVersion() 
	{
		return classVersion;
	}
	public void setClassVersion(String classVersion) 
	{
		this.classVersion = classVersion;
	}
	public long getOID() 
	{
		return OID;
	}
	public void setOID(long oID) 
	{
		OID = oID;
	}
	public String[] getTypeParameters()
	{
		return typeParameters;
	}
	public boolean hasTypeParameters()
	{
		return typeParameters!=null && typeParameters.length>0;
	}
	public String toString()
	{
		String out;
		out="LOID{"+Id+"} class="+className;
		if(typeParameters!=null)
		{
			out+="<";
			for(int i=0;i<typeParameters.length;i++)
			{
				out+=(i>0?",":"")+typeParameters[i];
			}
			out+=">";
		}
		else
		{
			out+="<none>";
		}
		
		out+="  version="+classVersion+"  OID{"+OID+"}";
		
		return out;
	}
}
