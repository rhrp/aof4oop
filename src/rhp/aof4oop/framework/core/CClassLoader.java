package rhp.aof4oop.framework.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import rhp.aof4oop.framework.core.datamodel.CClassVersionMetaObject;
import rhp.aof4oop.framework.core.datamodel.CInstanceMetaObject;

public class CClassLoader extends ClassLoader //org.aspectj.weaver.loadtime.WeavingURLClassLoader 
{
	//Class Cache
	private Hashtable<String,Class<?>> 				classes = new Hashtable<String,Class<?>>();
	
	//Cache de todas as versões de classes já conhecidas pelo CL. Deste modo, evita-se a ida à BD.
	private Hashtable<String,CClassVersionMetaObject>	classVersions=	new Hashtable<String, CClassVersionMetaObject>();
	//Cache com todas as classes actuais
	private Hashtable<String,CClassVersionMetaObject>	classCurrent=	new Hashtable<String, CClassVersionMetaObject>();

    public CClassLoader()
    {
        super(ClassLoader.getSystemClassLoader());
    }
    public CClassLoader(ClassLoader cl)
    {
        super(cl);
    }
    public Class<?> loadClass(String className) throws ClassNotFoundException
    {
    	Class<?> clzz;
    	clzz=getClass(className);
    	if(clzz!=null)
    	{
    		return clzz;
    	}
    	try
    	{
   			clzz=super.loadClass(className);
    		if(clzz!=null)
    		{
    			putCache(clzz);
   				return clzz;
    		}
    	}
    	catch (ClassNotFoundException e) 
    	{
    		debugMsg("The class "+className+" does not exists in app's classpath.");
		}
        try
        {
        	CClassVersionMetaObject versionClass=loadVersionClass(className);
        	debugMsg("Found in DB:"+className);
        	return register(versionClass);
        }
        catch(Exception e)
        {
        	//TODO: detect when is a resource file
//        	if(!className.startsWith("com.sun.tools.javac.resources.compiler_pt")
//        	&& !className.startsWith("com.sun.swing.internal.plaf.basic.resources.basic_pt")
//        	&&!className.startsWith("com.sun.swing.internal.plaf.basic.resources.basic_pt_PT")
//        	&&!className.startsWith("com.sun.swing.internal.plaf.metal.resources.metal_pt_PT"))
        	if(!className.endsWith("_pt") && !className.endsWith("_pt_PT")) 
        	{
        		e.printStackTrace();
        	}
        	//return null;
        	throw new ClassNotFoundException("The class "+className+" does not exists in app's classpath and DB.");
        } 
        //return findClass(className);
    }
    public Class<?> findClass(String name) throws ClassNotFoundException
    {
    	//debugMsg("findClass: "+name);
		Class<?> clzz=getClass(name);
		if(clzz==null)
		{
			//debugMsg("findClass: "+name);
			clzz=super.findClass(name);
			putCache(clzz);
		}
		return clzz;
    }
    public Class<?> register(CClassVersionMetaObject versionClass) throws NotFoundException, CannotCompileException 
    {
    	return register(versionClass.toVersionClassCanonicalName(),versionClass.toVersionClassName(), versionClass.getClassByteCode());
    }
    /**
     * Register a class from a OS file .class
     * @param class_path
     * @param classCanonicalName
     * @return
     * @throws IOException
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    public Class<?> register(String class_path,String classCanonicalName,String className) throws IOException, NotFoundException, CannotCompileException
    {
    	byte[] buffer=new byte[1000000];
    	byte[] byte_code=null;
    	FileInputStream f = new FileInputStream(class_path);
    	try
    	{
    		int n=0;
    		int b=f.read();
    		while(b>=0)
    		{
    			if(n>buffer.length)
    			{
    				throw new IOException();
    			}
    			buffer[n]=(byte)b;
    			b=f.read();
    			n++;
    		}
        	byte_code=new byte[n];
        	for(int i=0;i<n;i++)
        	{
        		byte_code[i]=buffer[i];
        	}
    	}
    	finally
    	{
    		debugMsg("ByteCode ("+classCanonicalName+") "+byte_code.length);    		
    		f.close();
    	}
    	//debugMsg(classCanonicalName+" size:"+byte_code.length);
    	return register(classCanonicalName,className, byte_code);
    }
    public Class<?> register(String classCanonicalName,String className,byte[] byte_code) throws NotFoundException, CannotCompileException 
    {
    	Class<?> result;
    	
    	result=classes.get(classCanonicalName);
    	if(result!=null)
    	{
    		debugMsg("register: "+classCanonicalName+" (CACHE!!! This should not happen!!!)");
    		return result;
    	}
    	else
    	{
    		debugMsg("register: "+classCanonicalName);
    	   	result = defineClass(className,byte_code,0,byte_code.length,(ProtectionDomain)null);
    	   	putCache(result);
    	   	return result;
    	}
    }
    /**
     * If the class does not exists or is loaded returns null
     * @param className
     * @return
     */
    public Class<?> getClass(String className)
    {
    	//debugMsg("Class cache size="+classes.size());
    	return classes.get(className);
    }
    private void putCache(Class<?> clzz)
    {
    	classes.put(clzz.getName(),clzz);
//    	debugMsg("Classses' Cache size:"+classes.size());
    }
    /**
     * 
     * @param className
     * @param byte_code
     * @throws NotFoundException
     */
	public void registerJavaAssist(String className,byte[] byte_code) throws NotFoundException 
	{
		ClassPool cp=ClassPool.getDefault();
		try
		{
			CtClass cc=cp.getCtClass(className);
			debugMsg("Classe já registada: "+cc.getName());
			return;
		}
		catch (Exception e) 
		{
			debugMsg("Classe por registar no class loader: "+className);
		}
		//debugMsg("Class: "+o.getClassCanonicalName()+"  Version:"+o.getClassVersion()+"  ByteCode length:"+o.getClassByteCode().length);

		try
		{
			cp.insertClassPath(new ByteArrayClassPath(className, byte_code));
			CtClass cc=cp.getCtClass(className);
			//Loads the Version Class
			cc.toClass();
		}
		catch (CannotCompileException e) 
		{
			debugMsg("register:"+e.getMessage());
		}
	}
    
//    public Class<?> __findClass(String className)
//    {
//        byte classByte[];
//        Class<?> result;
//        
//        debugMsg("find:"+className);
//        result = (Class<?>)classes.get(className);
//        if(result != null)
//        {
//        	debugMsg("on cache");
//            return result;
//        }
//        
//        try
//        {
//            return findSystemClass(className);
//        }
//        catch(Exception e)
//        {
//        	debugMsg("Not find on System:"+className);
//        }
//        try
//        {
//        	CClassVersionMetaObject versionClass=loadVersionClass(className);
//        	debugMsg("Find on DB:"+className);
//        	classByte = versionClass.getClassByteCode();
//        	result = defineClass(className,classByte,0,classByte.length,null);
//        	classes.put(className,result);
//        	return result;
//        }
//        catch(Exception e)
//        {
//        	e.printStackTrace();
//            return null;
//        } 
//    }
 
    private CClassVersionMetaObject loadVersionClass(String classVersionName) throws ClassNotFoundException
    {
    	debugMsg("This code must be unreacheable");
    	//Implementeted as an aspect
    	return null;
    }
    private void storeVersionClass(CClassVersionMetaObject ocv)
    {
    	debugMsg("This code must be unreacheable");
    	//Implementeted as an aspect
    }
    private List<CClassVersionMetaObject> getAllClassVersions()
    {
    	debugMsg("This code must be unreacheable");
    	//Implementeted as an aspect
    	return null;
    }
	/**
	 * Find and, if necessary, creates the CClassVersionMetaObject of that object class
	 * @param object
	 * @return
	 * @throws CannotCompileException 
	 * @throws IOException 
	 * @throws NotFoundException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public CClassVersionMetaObject findObjectReferenceClassVersion(Object memoryObject) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException, InstantiationException, IllegalAccessException
	{
		String className;
		String classVersion;
		printClassDetails(memoryObject.getClass());
		
		className=memoryObject.getClass().getName();
		classVersion=CClassVersionMetaObject.calcVersion(memoryObject.getClass());
		
		//Try on the cache
		CClassVersionMetaObject ocv=classVersions.get(CClassVersionMetaObject.toVersionClassName(className, classVersion));
		if(ocv!=null)
		{
			return ocv;
		}
		//Try to Load from DB classpath
		try
		{
			return loadVersionClass(CClassVersionMetaObject.toVersionClassName(className, classVersion));
		}
		catch (ClassNotFoundException e) 
		{
			debugMsg("Fail to load "+className+" on version "+classVersion+": "+e.getMessage());
		}
		System.out.print("GO TO FACTORY: "+memoryObject.getClass().getName());
		// Register a new class version
		return factory(memoryObject.getClass());
	}
	/**
	 * Finds the CVMO of a database object
	 * @param databaseObject
	 * @return
	 */
	public CClassVersionMetaObject findDatabaseObjectClassVersion(Object databaseObject)
	{
		return classVersions.get(databaseObject.getClass().getCanonicalName());
	}
	public CClassVersionMetaObject findObjectInstanceClassVersion(CInstanceMetaObject object) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException, InstantiationException, IllegalAccessException
	{
		String objClassVersion=CClassVersionMetaObject.toVersionClassName(object.getClassName(), object.getClassVersion());
		//Try in the cache
		CClassVersionMetaObject ocv=classVersions.get(objClassVersion);
		if(ocv!=null)
		{
			return ocv;
		}
		//Load the class
		try
		{
			ocv=loadVersionClass(objClassVersion);
			classVersions.put(objClassVersion,ocv);
			return ocv;
		}
		catch (ClassNotFoundException e) 
		{
			// TODO: handle exception
		}
		Class<?> appClass=Class.forName(object.getClassName());
		//A new class version
		return factory(appClass);

	}
	/**
	 * Find and, if necessary, creates the CClassVersionMetaObject from the the class width the className
	 * @param className
	 * @return
	 * @throws CannotCompileException 
	 * @throws IOException 
	 * @throws NotFoundException 
	 * @throws ClassNotFoundException 
	 */
	public CClassVersionMetaObject findApplicationObjectClassVersion(String appClassName) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		String classVersion;
		Class<?> clazz;
		CClassVersionMetaObject ocv;

		//Try in the cache
		ocv=classCurrent.get(appClassName);
		if(ocv!=null)
		{
			return ocv;
		}
		//Load the class
		clazz=Class.forName(appClassName);
		printClassDetails(clazz);
		
		try
		{
			classVersion=CClassVersionMetaObject.calcVersion(appClassName);
			ocv=loadVersionClass(CClassVersionMetaObject.toVersionClassName(appClassName, classVersion));
			
			classCurrent.put(appClassName, ocv);
			return ocv;
		}
		catch (ClassNotFoundException e) 
		{
			// TODO: handle exception
		}
		return factory(clazz);
	}
	public static void printClassDetails(Class<?> clazz)
	{
		printClassDetails(clazz,0);
	}
	private static void printClassDetails(Class<?> clazz,int level)
	{
//		String padding="";
//		for(int i=0;i<level;i++)
//		{
//			padding+=" ";
//		}
//		debugMsg(padding+clazz.getCanonicalName());
		if(clazz.getSuperclass()!=null && !clazz.getSuperclass().getName().equals("java.lang.Object"))
		{
			printClassDetails(clazz.getSuperclass(), level+1);
		}
	}
	public void printClassCache()
	{
		Enumeration<String> ks = classes.keys();
		while(ks.hasMoreElements())
		{
			String k=ks.nextElement();
			System.out.println(k+"="+classes.get(k).getCanonicalName());
		}
	}
	/**
	 * Creates an Class Version Meta-Object for a class
	 * @param clazz
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 */
	private CClassVersionMetaObject factory(Class<?> clazz) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		ClassPool cp=new ClassPool(true);
		debugMsg("FACTORY: "+clazz.getCanonicalName());
		CClassVersionMetaObject ocv=__factory(cp,clazz.getCanonicalName(),clazz.getName(),0);
		debugMsg("Version:"+ocv.getClassVersion());
		classVersions.put(ocv.toVersionClassName(),ocv);
		debugMsg("Class cache size="+classVersions.size());
		return ocv;
	}

	/**
	 * 
	 * @param cp
	 * @param ctClass
	 * @return
	 * @throws NotFoundException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 */
	private CClassVersionMetaObject __factory(ClassPool cp,String appClassCanonicalName,String appClassName,int level) throws NotFoundException, ClassNotFoundException, IOException, CannotCompileException
	{
//		String superClassName=ClassPool.getDefault().getCtClass(appClassName).getSuperclass().getName();
//		String superClassCanonicalName=cp.getCtClass(appClassName).getSuperclass().getName();
		CtClass appClass=ClassPool.getDefault().get(appClassName);
		debugMsg("factory "+appClassName+"="+appClass.getSuperclass());		
		String superClassName=appClass.getSuperclass().getName();
		String superClassCanonicalName=appClass.getSuperclass().getName();
		level++;
		if(CPersistentRoot.isNonVersionedDataTypeObject(appClassName))
		{
			//The class being is non-versioned.
			//Thus, it just recorded as a CVMO
			String classVersion=CClassVersionMetaObject.calcVersion(appClass);
			CClassVersionMetaObject ocv=new CClassVersionMetaObject(appClassCanonicalName,appClassName,classVersion,appClass.toBytecode());
			storeVersionClass(ocv);
			return ocv;
		}
		else if(superClassName.equals("java.lang.Object"))
		{
			//The super class of the class being registered is the top-level
			//Thus, the class is recorded as CVMO and registred in JVM
			//CtClass newClass=ClassPool.getDefault().get(appClassName);
			String classVersion=CClassVersionMetaObject.calcVersion(appClass);
			String newClassName=CClassVersionMetaObject.toVersionClassName(appClassName,classVersion);
			
			//Check if is already registered
//			try
//			{
//				Class.forName(newClassName, false,this);
//				System.out.println("Level:"+level+" - "+newClassName+" is already registered (super:java.lang.Object)");
//				return loadVersionClass(newClassName);
//			}
//			catch(Exception e)
//			{
//				System.out.println("Level:"+level+" - "+newClassName+" is not registered (super:java.lang.Object)");
//			}
			
			
			CtClass cc=cp.getAndRename(appClassName, newClassName);
			debugMsg("Renamed from "+appClassName+" to "+newClassName+" result="+cc.getName());
			if(CUtilities.haveDefaultConstructor(cc))
			{
				debugMsg(""+cc.getName()+" already have a default constructor");
			}	
			else
			{
				debugMsg("Adding  to "+cc.getName()+" a default constructor");
				cc=CUtilities.addDefaultConstructor(cc);
			}
			
			
			CClassVersionMetaObject ocv=new CClassVersionMetaObject(appClassCanonicalName,appClassName,classVersion,cc.toBytecode());
			register(ocv);
			//debugMsg"NEW::"+ocv);
			storeVersionClass(ocv);
			return ocv;
		}
		else
		{
			//The super class of the class being registered is NOT the top-level
			//Thus, its superclass is registered as CVMO and registered in JVM
			// and then the class is also recorded as CVMO and registered in JVM

			CClassVersionMetaObject ocvSuperClass=__factory(cp,superClassCanonicalName,superClassName,level);
			
			
			String classVersion=CClassVersionMetaObject.calcVersion(appClass);
			String newClassName=CClassVersionMetaObject.toVersionClassName(appClassName,classVersion);
			//Check if is already registered
//			try
//			{
//				Class.forName(newClassName, false,this);
//				debugMsg"Level:"+level+" - "+newClassName+" is already registered (super:"+superClassName+")");
//				return loadVersionClass(newClassName);
//			}
//			catch(Exception e)
//			{
//				debugMsg"Level:"+level+" - "+newClassName+" is not registred (super:"+superClassName+")");
//			}
			
			debugMsg("SUPER CLASS="+ocvSuperClass.getClassCanonicalName()+"  v="+ocvSuperClass.getClassVersion());
			CtClass newClass=cp.getCtClass(appClassName);
			CtClass superClass=cp.get(ocvSuperClass.toVersionClassName());
			debugMsg("SET SUPER "+ superClass.getName()+"  FROZEN="+newClass.isFrozen());
			newClass.setSuperclass(superClass);
			
			//String newClassName=CClassVersionMetaObject.toVersionClassName(className,classVersion);
			
			newClass.replaceClassName(appClassName, newClassName);
						
			CClassVersionMetaObject ocv=new CClassVersionMetaObject(appClassCanonicalName,appClassName,classVersion,newClass.toBytecode());
			//debugMsg"NEW::"+ocv);
			register(ocv);
			storeVersionClass(ocv);
			
			return ocv;
		}
	}
	/**
	 * Exports to a class path in file system all known class versions 
	 * @param class_path
	 * @param verbose
	 * @throws IOException
	 */
	public void exportAllClassVersions(String class_path,boolean verbose) throws IOException
	{
		for(CClassVersionMetaObject cvmo:getAllClassVersions())
		{
			String filename=(class_path.endsWith("/")?class_path:class_path+"/")+cvmo.toVersionClassName().replace(".","/")+".class";
			if(verbose)
			{
				System.out.println("Exporting: "+cvmo.getClassCanonicalName()+"   Version: "+cvmo.getClassVersion()+"   size:"+cvmo.getClassByteCode().length);
				System.out.println(filename);
			}
			File f=new File(filename);
			f.getParentFile().mkdirs();
			FileOutputStream fos=new FileOutputStream(f);
			fos.write(cvmo.getClassByteCode());
			fos.close();
		}
	}
	/**
	 * Dump to screen all loaded class versions
	 */
	public void dumpClassVersions()
	{
		for(CClassVersionMetaObject cvmo:classVersions.values())
		{
			System.out.println(cvmo.getClassCanonicalName()+"   "+cvmo.getClassVersion()+"   size:"+cvmo.getClassByteCode().length);
		}
	}
//	private CClassVersionMetaObject __mais_o_menos_factory(ClassPool cp,String appClassName,int level) throws NotFoundException, ClassNotFoundException, IOException, CannotCompileException
//	{
//		String superClassName=ClassPool.getDefault().getCtClass(appClassName).getSuperclass().getName();
//		//String superClassName=cp.getCtClass(appClassName).getSuperclass().getName();
//		level++;
//		if(superClassName.equals("java.lang.Object"))
//		{
//			CtClass newClass=ClassPool.getDefault().get(appClassName);
//			String classVersion=CClassVersionMetaObject.calcVersion(newClass);
//			String newClassName=CClassVersionMetaObject.toVersionClassName(appClassName,classVersion);
//			
//			//Check if is already registered
//			try
//			{
//				Class.forName(newClassName, false,this);
//				System.out.println("Level:"+level+" - "+newClassName+" is already registered (super:java.lang.Object)");
//				return loadVersionClass(newClassName);
//			}
//			catch(Exception e)
//			{
//				System.out.println("Level:"+level+" - "+newClassName+" is not registered (super:java.lang.Object)");
//			}
//			
//			CClassVersionMetaObject ocv=new CClassVersionMetaObject(appClassName,classVersion,cp.getAndRename(appClassName, newClassName).toBytecode());
//			register(ocv);
//			//System.out.println("NEW::"+ocv);
//			storeVersionClass(ocv);
//			return ocv;
//		}
//		else
//		{
//			CClassVersionMetaObject ocvSuperClass=__factory(cp,superClassName,level);
//			
//			CtClass appClass=ClassPool.getDefault().get(appClassName);
//			String classVersion=CClassVersionMetaObject.calcVersion(appClass);
//			String newClassName=CClassVersionMetaObject.toVersionClassName(appClassName,classVersion);
//			//Check if is already registered
//			try
//			{
//				Class.forName(newClassName, false,this);
//				System.out.println("Level:"+level+" - "+newClassName+" is already registered (super:"+superClassName+")");
//				return loadVersionClass(newClassName);
//			}
//			catch(Exception e)
//			{
//				System.out.println("Level:"+level+" - "+newClassName+" is not registred (super:"+superClassName+")");
//			}
//			System.out.println("SUER CLASS="+ocvSuperClass.getClassCanonicalName()+"  v="+ocvSuperClass.getClassVersion());
//			CtClass newClass=cp.getCtClass(appClassName);
//			CtClass superClass=cp.get(ocvSuperClass.toVersionClassName());
//			System.out.println("SET SUPER "+ superClass.getName()+"  FROZEN="+newClass.isFrozen());
//			newClass.setSuperclass(superClass);
//			
//			//String newClassName=CClassVersionMetaObject.toVersionClassName(className,classVersion);
//			
//			newClass.replaceClassName(appClassName, newClassName);
//						
//			CClassVersionMetaObject ocv=new CClassVersionMetaObject(appClassName,classVersion,newClass.toBytecode());
//			//System.out.println("NEW::"+ocv);
//			register(ocv);
//			storeVersionClass(ocv);
//			
//			return ocv;
//		}
//	}
	private void debugMsg(String msg)
	{
		
	}
}