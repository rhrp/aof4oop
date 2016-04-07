package rhp.aof4oop.framework.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import rhp.aof4oop.framework.core.exceptions.EFrameworkFault;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtNewConstructor;
import javassist.NotFoundException;

public class CUtilities 
{
	public static Class<?> addDefaultConstructor(String className)
	{
		try
		{
			ClassPool cp=ClassPool.getDefault();
			CtClass ctcls=cp.makeClass(className);
			ctcls.defrost();
			ctcls.addConstructor(CtNewConstructor.defaultConstructor(ctcls));
			//System.out.println("Byte Code after the constructor:"+ctcls.toBytecode().length+" bytes");
			return ctcls.toClass();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new EFrameworkFault(e);			
		}
	}
	public static CtClass addDefaultConstructor(CtClass ctcls) throws CannotCompileException
	{
		//System.out.println("Byte Code before the constructor:"+ctcls.toBytecode().length+" bytes");
		//ctcls.defrost();
		ctcls.addConstructor(CtNewConstructor.defaultConstructor(ctcls));
		//System.out.println("Byte Code after the constructor:"+ctcls.toBytecode().length+" bytes");
		return ctcls;
	}
	public static Class<?> addDefaultConstructor(Class<?> clzz) 
	{
		try
		{
			ClassPool cp=ClassPool.getDefault();
			CtClass ctcls=cp.get(clzz.getCanonicalName());
			//System.out.println("Byte Code before the constructor:"+ctcls.toBytecode().length+" bytes");
			ctcls.defrost();
			ctcls.addConstructor(CtNewConstructor.defaultConstructor(ctcls));
			//System.out.println("Byte Code after the constructor:"+ctcls.toBytecode().length+" bytes");
			//Class<?> out=ctcls.toClass();
			Class<?> out=ctcls.toClass();
			
			System.out.println("The class "+out.getCanonicalName()+" now already have a default constructer");
			return out;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new EFrameworkFault(e);
		}
	}
	public static boolean haveDefaultConstructor(CtClass ctcls) throws NotFoundException
	{
		CtConstructor[] cs = ctcls.getConstructors();
		for(CtConstructor c:cs)
		{
			if(c.getParameterTypes().length==0)
			{
				return true;
			}
		}
		return false;
	}
	public static boolean  haveDefaultConstructor(Class<?> clzz) throws IllegalArgumentException, IllegalAccessException
	{
		Constructor<?>[] cs = clzz.getConstructors();
		for(Constructor<?> c:cs)
		{
			if(c.getParameterTypes().length==0)
			{
				return true;
			}
		}
		return false;
	}
	public static void showFields(Object obj) throws IllegalArgumentException, IllegalAccessException
	{
		System.out.println("class's fields: "+obj.getClass().getName());
		
	    Field[] fdls=obj.getClass().getDeclaredFields();
	    for(int i=0;i<fdls.length;i++)
	    {
	    	fdls[i].setAccessible(true);
	    	System.out.println("Field["+i+"]::"+fdls[i].getName()+"="+fdls[i].get(obj));
	    }
	}
	public static void showConstructors(Object obj) throws IllegalArgumentException, IllegalAccessException
	{
		System.out.println("class: "+obj.getClass().getName());
		Constructor<?>[] cs = obj.getClass().getConstructors();
		for(Constructor<?> c:cs)
		{
			System.out.println(""+c.getName()+"("+c.getParameterTypes().length+")");
		}
	}
}
