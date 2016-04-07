package rhp.aof4oop.framework.core.datamodel;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import rhp.aof4oop.framework.core.CClassLoader;
import rhp.aof4oop.framework.core.CPersistentRoot;
import rhp.aof4oop.framework.core.annotations.Aof4oopDefault;
import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;
import rhp.aof4oop.framework.core.exceptions.EFrameworkFault;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * This Meta Class represents an specific Version of a Class
 * @author rhp
 *
 */
public class CClassVersionMetaObject  extends CMetaObject
{
	private String classCanonicalName;
	private String className;
	private String classVersion;
	private byte[] classByteCode;
	
	public CClassVersionMetaObject(String classCanonicalName,String className, String classVersion,byte[] classByteCode) 
	{
		super();
		this.classCanonicalName = classCanonicalName;
		this.className = className;
		this.classVersion = classVersion;
		this.classByteCode = classByteCode;
	}
	public String getClassCanonicalName() 
	{
		return classCanonicalName;
	}
	
	public String getClassName() {
		return className;
	}
	public String getClassVersion() 
	{
		return classVersion;
	}
	public byte[] getClassByteCode() {
		return classByteCode;
	}
	public String toVersionClassName()
	{
		return toVersionClassName(className,classVersion);
	}
	public String toVersionClassCanonicalName()
	{
		return toVersionClassName(classCanonicalName,classVersion);
	}
	public static String toVersionClassName(Class<?> clazz) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		return toVersionClassName(clazz.getName(),calcVersion(clazz));
	}
	public static String toVersionClassName(String classCanonicalName,String classVersion)
	{
		return classCanonicalName+("".equals(classVersion)?"":"$"+classVersion);
	}
	/**
	 * Calc the class version
	 * @param clazzCanonicalName
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 */
	public static String calcVersion(String clazzCanonicalName) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		if(CPersistentRoot.isNonVersionedDataTypeObject(clazzCanonicalName))
		{
			return "";
		}
		ClassPool cp=ClassPool.getDefault();
		return calcVersion(cp.get(clazzCanonicalName));

	}
	/**
	 * TODO: migrate to String
	 * @param clazz
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 */
	public static String calcVersion(CtClass clazz) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		byte [] byte_code;

		
		if(CPersistentRoot.isNonVersionedDataTypeObject(clazz.getName()))
		{
			return "";
		}
		
		//Check for a version alias
		Aof4oopVersionAlias annot = (Aof4oopVersionAlias)clazz.getAnnotation(Aof4oopVersionAlias.class);
		if(annot!=null)
		{
			debugMsg("Class Version Alias="+annot.alias());
			return annot.alias();
		}
		
//		System.out.print("Class: ");
		byte_code=__getByteCode(clazz,0);
//		System.out.println(" byte code length:"+byte_code.length);
		try
		{
			return md5(byte_code);
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new NotFoundException("Error on MD5");// :-((
		}
	}
	private static byte[] __getByteCode(CtClass clazz,int level) throws NotFoundException, IOException, CannotCompileException
	{
		
		if(clazz.getSuperclass().getName().equals("java.lang.Object"))
		{
//			System.out.print(" "+clazz.getName()+"  Object");
			return clazz.toBytecode();
		}
		else
		{
//			System.out.print(" "+clazz.getName());
			byte[] sbc=__getByteCode(clazz.getSuperclass(),level+1);
			byte[] bc=clazz.toBytecode();
			byte[] out=new byte[sbc.length+bc.length];
			for(int i=0;i<sbc.length;i++)
			{
				out[i]=sbc[i];
			}
			int p=sbc.length;
			for(int i=0;i<bc.length;i++)
			{
				out[i+p]=bc[i];
			}
			return out;
		}
	}

	private static String md5(byte[] byteCode) throws NoSuchAlgorithmException
	{
		MessageDigest algorithm = MessageDigest.getInstance("MD5");
		algorithm.reset();
		algorithm.update(byteCode);
		byte messageDigest[] = algorithm.digest();
		BigInteger bigInt = new BigInteger(1,messageDigest);
		String hashtext = bigInt.toString(16);
		// Now we need to zero pad it if you actually want the full 32 chars.
		while(hashtext.length() < 32 )
		{
		  hashtext = "0"+hashtext;
		}
		return hashtext;
	}
	/**
	 * Calc the class version
	 * @param clazz
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 */
	public static String calcVersion(Class<?> clazz) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		return calcVersion(clazz.getCanonicalName());
	}
	/**
	 * Gets the byte code of the versioned class
	 * @param clazzCanonicalName
	 * @param newClassName
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 */
	public static byte[] toByteCodeAndRename(ClassPool cp,String clazzCanonicalName,String newClassName) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		CtClass cc=cp.getAndRename(clazzCanonicalName,newClassName);
		return cc.toBytecode();	
	}
	public static byte[] toByteCodeAndRename(String clazzCanonicalName,String newClassName) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		return toByteCodeAndRename(ClassPool.getDefault(), clazzCanonicalName, newClassName);
	}
	/**
	 * Provides data migration at all levels of the hierarchy of classes
	 * 
	 * @param dbObject
	 * @return
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws ClassNotFoundException 
	 */
	public Object directMapping(Object srcObject,String dstClassName) throws IllegalArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException
	{
		Object obj=null;
		
		//Using CClassLoader Class cache
		long tini=System.currentTimeMillis();

		if(CPersistentRoot.isNonVersionedDataTypeObject(srcObject.getClass().getCanonicalName()))
		{
			System.out.println("The class "+srcObject.getClass().getCanonicalName()+" is non-versioned!!!");
			return srcObject;
		}
		else
		{
			//System.out.println("The class "+srcObject.getClass().getCanonicalName()+" is versioned!!!");
			Class<?> clz;
			//clz=((CClassLoader)ClassLoader.getSystemClassLoader()).getClass(dstClassName);//alterado em 13/Ago/2013
			clz=((CClassLoader)ClassLoader.getSystemClassLoader()).loadClass(dstClassName);
			if(clz==null)
			{
				throw new IllegalArgumentException("The destination Class name "+dstClassName+" does not exists");
			}
			obj=clz.newInstance();
			
			//debugMsg("Simple Convertion -  "+srcObject.getClass().getCanonicalName()+" -->  "+obj.getClass().getCanonicalName());
			__directMapping(srcObject.getClass(),srcObject,obj.getClass(),obj);
		}
		long tend=System.currentTimeMillis();
		CPersistentRoot.statsTimeDirectMapping+=(tend-tini);
		return obj;

	}
	
	/**
	 * Provides data migration at all levels of class hierarchy
	 * @param srcClazz
	 * @param srcObject
	 * @param dstClazz
	 * @param dstObject
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void __directMapping(Class<?> srcClazz,Object srcObject,Class<?> dstClazz,Object dstObject) throws IllegalArgumentException, IllegalAccessException
	{
		Field f_srcObject;
		Field[] flds;

		debugMsg("conv :"+srcClazz.getCanonicalName());
		flds=CPersistentRoot.reflectFields(dstClazz);// get all members in class hierarchy
		for(Field f:flds)
		{
			if(f.getName().startsWith("ajc$"))
			{
				debugMsg("   Field: "+f.getName()+" ignored src("+srcObject+") dst("+dstClazz.getName()+")");
			}
			else if(java.lang.reflect.Modifier.isStatic(f.getModifiers()))
			{
				System.out.println("   Direct Map Field: "+dstClazz.getName()+"."+f.getName()+" "+f.getType().getCanonicalName()+" is static!!!");
			}
			else
			{
//				if(srcClazz.getCanonicalName().startsWith("rhp.aof4oop.apps.openstreetmap.Coordinate"))
//				{
//					System.out.println(" ---------------- "+srcClazz.getCanonicalName()+" :: "+f.getName()+"  of "+f.getType().getCanonicalName()+"  primitive="+f.getType().isPrimitive()+"  /  "+CPersistentRoot.isPrimitiveDataTypeObject(f.getType().getCanonicalName()));
//				}
				if(f.getType().isPrimitive() 
				|| CPersistentRoot.isPrimitiveDataTypeObject(f.getType().getCanonicalName()) 
				|| (f.getType().isArray() && (f.getType().getComponentType().isPrimitive() || CPersistentRoot.isPrimitiveDataTypeObject(f.getType().getComponentType().getName()))
				|| CPersistentRoot.isNonVersionedDataTypeObject(f.getType().getCanonicalName()))
				)
				{
					//primitives and Strings, Longs, floats, etc. => Don't go to cache
					String tmp=null;
					try
					{
						f_srcObject=CPersistentRoot.getField(srcClazz, f.getName());
						tmp=f_srcObject.getType().getCanonicalName();
						f.setAccessible(true);
						f_srcObject.setAccessible(true);
						f.set(dstObject,f_srcObject.get(srcObject));
//						if(srcClazz.getCanonicalName().startsWith("rhp.aof4oop.apps.openstreetmap.Coordinate"))
//						{
//							System.out.println("   Direct Map Field: "+dstClazz.getName()+"."+f.getName()+"="+f_srcObject.get(srcObject));
//						}
					}
					catch(IllegalArgumentException e)
					{
						System.out.println("   Direct Map Field: "+dstClazz.getName()+"."+f.getName()+" "+f.getType().getCanonicalName()+"  <> "+tmp);
					}
					catch (NoSuchFieldException e) 
					{
						//The object's destination field does not exists in source object
						Aof4oopDefault annot=f.getAnnotation(Aof4oopDefault.class);
						if(annot!=null)
						{
							//However, it was an annotation
							for(int i=0;i<annot.classVersion().length;i++)
							{
								if(annot.classVersion()[i].equals("*") || annot.classVersion()[i].equals(getClassVersion()))
								{
									String defaultValue=annot.value();
									System.out.println("   Field: "+f.getName()+" has a Default Value Annotation for this class version. Set to \""+defaultValue+"\"");
									if(f.getType().getCanonicalName().equals("java.lang.String"))
									{
										f.setAccessible(true);
										f.set(dstObject,defaultValue);
									}
									else if(f.getType().getCanonicalName().equals("int"))
									{
										f.setAccessible(true);
										f.set(dstObject,new Integer(defaultValue));
									}
									else
									{
										System.out.println("O tipo não é compativel ="+f.getType().getCanonicalName());
									}
								}
								else
								{
									System.out.println("   Field: "+f.getName()+" has a Default Value Annotation but to another class version \""+annot.classVersion()[i]+"\" this one is \""+getClassVersion()+"\"");
								}
							}
						}
//						else
//						{
//							System.out.println("   Field: "+f.getName()+" ignored src("+srcObject.getClass().getCanonicalName()+")  dst("+dstClazz.getName()+")");
//						}
					}
				}
				else if(f.getType().isArray())
				{
					//TODO: check if is correct!!!
					debugMsg("  Field: "+f.getName()+" is a array of links to another objects it must be ignored ("+srcObject.getClass().getCanonicalName()+")");
				}
				else
				{
					debugMsg("  Field: "+f.getName()+" is a link to another object it must be ignored ("+srcObject.getClass().getCanonicalName()+")");
				}
			}
		}
		//		if(!srcClazz.getSuperclass().getName().equals(Object.class.getCanonicalName()))
		//		{
		//			__directMapping(srcClazz.getSuperclass(),srcObject,dstClazz.getSuperclass(),dstObject);
		//		}
	}

	/**
	 *  Converts the persistent image of the object to the this Class Version
	 */
	public Object readerAdapter(Object dbObject,Object dbParentObject,CClassLoader cl,CInstanceMetaObject oInst,CAttributeMetaObject oParentLink) throws SecurityException, InstantiationException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, NotFoundException, IOException, CannotCompileException,EFrameworkFault
	{
		return directMapping(dbObject,getClassName());
	}
	/**
	 * Convert a memory object to a persistent image to be saved on DB
	 * @param memoryObject
	 * @return
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 */
	public Object writerAdapter(Object appObject,CClassVersionMetaObject appClassVersion) throws SecurityException, InstantiationException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		return directMapping(appObject,toVersionClassName());
	}
	/**
	 * Get the current super class
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Class<?> getSuperClass() throws ClassNotFoundException
	{
		Class<?> clzz=Class.forName(getClassCanonicalName());
		
		return clzz.getSuperclass();
	}
	/**
	 * Get the versioned super class
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Class<?> getSuperClassVersion() throws ClassNotFoundException
	{
		Class<?> clzz=((CClassLoader)ClassLoader.getSystemClassLoader()).getClass(toVersionClassName());
		
		return clzz.getSuperclass();
	}
	public String toString()
	{
		return "ClassVersion: "+classCanonicalName+"$"+classVersion;
	}
	public boolean equals(CClassVersionMetaObject another)
	{
		return (another!=null && another.getClassCanonicalName().equals(getClassCanonicalName()) && another.getClassVersion()==getClassVersion());
	}
	private static void debugMsg(String msg)
	{
		System.out.println("[CClassVersionMetaObject]::"+msg);
	}
}
