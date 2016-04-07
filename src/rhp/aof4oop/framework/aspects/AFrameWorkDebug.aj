package rhp.aof4oop.framework.aspects;


public aspect AFrameWorkDebug 
{
	/**
	 * debug CPersistentRoot
	 * @param msg
	 */
	void around(String msg) : execution(void rhp.aof4oop.framework.core.CPersistentRoot.debugMsg(..)) && args(msg)
	{
//		proceed(msg);
	}
	void around(String msg) : execution(void rhp.aof4oop.framework.core.CCache.debugMsg(..)) && args(msg)
	{
		//proceed(msg);
	}
	/**
	 * Debug CObjectClassVersion
	 * @param msg
	 */
	void around(String msg) : execution(void rhp.aof4oop.framework.core.datamodel.CClassVersionMetaObject.debugMsg(..)) && args(msg)
	{
		//proceed(msg);
	}
	void around(String msg) : execution(void rhp.aof4oop.framework.aspects.APersistence.debugMsg(..)) && args(msg)
	{
//		String debugedObject;
//		
//		debugedObject=thisJoinPoint.getTarget().getClass().getName();
//		if(rhp.aof4oop.framework.aspects.AObjectPersistence.class.getName().equals(debugedObject))
//		{
//			proceed(msg);
//		}
//		else if(rhp.aof4oop.framework.aspects.AObjectStorageDB4O.class.getName().equals(debugedObject))
//		{
//			proceed(msg);
//		}
//		else if(rhp.aof4oop.framework.aspects.AObjectAdaptation.class.getName().equals(debugedObject))
//		{
//			proceed(msg);
//		}
//		else
//		{
//			proceed(msg);
//		}
	}
	void around(Exception ex) : execution(void rhp.aof4oop.framework.aspects.APersistence.debugException(..)) && args(ex)
	{
//		String debugedObject;
//		
//		debugedObject=thisJoinPoint.getTarget().getClass().getName();
//		if(rhp.aof4oop.framework.aspects.AObjectPersistence.class.getName().equals(debugedObject))
//		{
//			proceed(msg);
//		}
//		else if(rhp.aof4oop.framework.aspects.AObjectStorageDB4O.class.getName().equals(debugedObject))
//		{
//			proceed(msg);
//		}
//		else if(rhp.aof4oop.framework.aspects.AObjectAdaptation.class.getName().equals(debugedObject))
//		{
//			proceed(msg);
//		}
//		else
//		{
//			proceed(msg);
//		}
	}
}
