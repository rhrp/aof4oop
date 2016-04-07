package rhp.aof4oop.framework.aspects;

import java.lang.reflect.Field;

import rhp.aof4oop.framework.core.CPersistentRoot;
import rhp.aof4oop.framework.core.annotations.Aof4oopNotNull;
import rhp.aof4oop.framework.core.datamodel.CInstanceMetaObject;
import rhp.aof4oop.framework.core.exceptions.EFrameworkFault;
import rhp.aof4oop.framework.core.exceptions.EIntegrityFault;

public aspect ADomainIntegrity extends APersistence
{
	/**
	 * Besides the existence of another advice, that already checks this constraint, here we can anticipate before the object be transfered to DB.
	 */
	before(): set(* *) && !within(rhp.aof4oop.framework.core.*) && !within(rhp.aof4oop.framework.core.datamodel.*) && !within(rhp.aof4oop.framework.aspects.*)
	{
		String attributeName=thisJoinPoint.getSignature().getName();
		Object attributeValue=thisJoinPoint.getArgs()[0];
		// Since isn't possible to have a wildcard pattern in the target pointcut, something like Target("rhp.aof4oop.*")
		debugMsg("Before set property -> ObjectTarget:"+(thisJoinPoint.getTarget()!=null?thisJoinPoint.getTarget().getClass().getName()+" -> Field: "+attributeName+"  -> Value:"+attributeValue+"  size:"+thisJoinPoint.getArgs().length:"null"));
		//If target object is cached so all it properties also have to be cached
		if(attributeValue==null && thisJoinPoint.getTarget()!=null)
		{
			Aof4oopNotNull annotNotNull=null;
			try
			{
				Field f=CPersistentRoot.getField(thisJoinPoint.getTarget().getClass(),attributeName);
				annotNotNull=f.getAnnotation(Aof4oopNotNull.class);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			if(annotNotNull!=null)
			{
				throw new EIntegrityFault(annotNotNull.message());
			}
		}
	}
//	/**
//	 * Checks when an attribute is updated in AObjectSorageDB4 aspect
//   * Since method saveObject() catches all updates, this one is disabled to avoid a double check.	 
//	 * setObjectField(Field f,Object object,Object value)
//	 * @param loid
//	 * @param object saveObject(Object object,String fieldName,Object value)
//	 */
//	before(Field field,Object object,Object value): execution(Object AObjectStorageDB4O.setObjectField(..)) && args(field,object, value)
//	{
//		if(field==null || object==null)
//		{
//			System.out.println("This should not happen!!!");
//			return;
//		}
//		System.out.println("Check NotNull: "+object.getClass().getCanonicalName()+"."+field.getName()+"="+(value!=null?"object":"null"));
//		if(value==null)
//		{
//			Aof4oopNotNull annotNotNull=null;
//			try
//			{
//				field.setAccessible(true);
//				if(field.get(object)==null)
//				{
//					annotNotNull=field.getAnnotation(Aof4oopNotNull.class);
//				}
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//				throw new EFrameworkFault(e.getMessage());
//			}
//			if(annotNotNull!=null)
//			{
//				throw new EIntegrityFault(annotNotNull.message());
//			}
//		}
//	}
	/**
	 * Checks integrity constraints when an object is saved in DB
	 */
	before(CPersistentRoot container,Object memoryObject): execution(CInstanceMetaObject AObjectStorageDB4O.saveObject(..)) && args(container,memoryObject)
	{
		if(memoryObject==null)
		{
			debugMsg("This should not happen!!!");
			return;
		}
		try 
		{
			Field[] fields=CPersistentRoot.reflectFields(memoryObject.getClass());
			for(Field field:fields)
			{
				if(!field.getName().startsWith("ajc$"))
				{
					field.setAccessible(true);
					Object value=field.get(memoryObject);
					debugMsg("Check NotNull: "+memoryObject.getClass().getCanonicalName()+"."+field.getName()+"="+(value!=null?"object":"null"));
					if(value==null)
					{
						Aof4oopNotNull annotNotNull=null;
						annotNotNull=field.getAnnotation(Aof4oopNotNull.class);
						if(annotNotNull!=null)
						{
							throw new EIntegrityFault(annotNotNull.message());
						}
					}
				}
			}
		} 
		catch (SecurityException e) 
		{
			throw new EFrameworkFault(e.getMessage());
		} 
		catch (IllegalArgumentException e) 
		{
			throw new EFrameworkFault(e.getMessage());
		} 
		catch (IllegalAccessException e) 
		{
			throw new EFrameworkFault(e.getMessage());
		}
	}
}
