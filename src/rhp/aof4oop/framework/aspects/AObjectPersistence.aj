package rhp.aof4oop.framework.aspects;


import org.aspectj.lang.JoinPoint;

import rhp.aof4oop.framework.core.CPersistentRoot;
import rhp.aof4oop.framework.core.exceptions.EFrameworkFault;

/**
 * Implements the object persistence on the Container
 * @author rhp
 *
 */
public privileged aspect AObjectPersistence extends APersistence
{
	//declare warning : call(* *.*())  && !within(rhp.aof4oop.framework.core.*) && !within(rhp.aof4oop.framework.aspects.*) : "Tudo Ok";

	private AObjectPersistence()
	{
		debugMsg("Init Aspect AObjectPersistence");
	}
	protected void finalize() throws Throwable
	{
		debugMsg("Finalize Aspect AObjectPersistence");
		super.finalize();
	}
	
	/**
	 * Intercepts the moment before of all set of any object property
	 * Note: Integrity aspects should be considered before this one 
	 */
	before(): set(* *) && !within(rhp.aof4oop.framework.core.*) 
	                   && !within(rhp.aof4oop.framework.core.datamodel.*) 
	                   && !within(rhp.aof4oop.framework.aspects.*)
	{
		long tini=System.currentTimeMillis();
		// Since isn't possible to have a wildcard pattern in the target pointcut, something like Target("rhp.aof4oop.*")
		// Thinks like, System.out.println("string"); are advised
//		debugMsg(getJointPointInfo(thisJoinPoint));
//		String target_class_name=""+(thisJoinPoint.getTarget()!=null?thisJoinPoint.getTarget().getClass().getName():"???");
//		debugMsg("Before set property -> ObjectTarget:"+(thisJoinPoint.getTarget()!=null?thisJoinPoint.getTarget().getClass().getName()+" -> Field: "+thisJoinPoint.getSignature().getName()+"  -> Value:"+thisJoinPoint.getArgs()[0]+"  size:"+thisJoinPoint.getArgs().length:"null"));
		try
		{
			if(CPersistentRoot.isTransactionLogActive())
			{
				//TODO: Does AspectJ allow the exclude pointcut within a stack of call?
				CPersistentRoot.beginTransaction();
				if(thisJoinPoint.getTarget()!=null)
				{
					//System.out.println("File :"+thisJoinPoint.getSourceLocation().getFileName()+"  Line: "+thisJoinPoint.getSourceLocation().getLine());				
					CPersistentRoot.doTransaction(thisJoinPoint.getTarget(), thisJoinPoint.getSignature().getName(),thisJoinPoint.getArgs()[0]);	
				}
				CPersistentRoot.commitTransaction();
			}
//			else
//			{
//				String memberName=thisJoinPoint.getSignature().getName();
//				Object tmp=thisJoinPoint.getArgs()[0];
//				String memberValue;
//				if(tmp==null)
//				{
//					memberValue="<NULL>";
//				}
//				else if(CPersistentRoot.isPrimitiveDataTypeObject(tmp.getClass().getCanonicalName()))
//				{
//					memberValue=""+tmp;
//				}
//				else
//				{
//					memberValue="<"+tmp.getClass().getCanonicalName()+">";
//				}
//				System.out.println("IGNORE LOG :: "+target_class_name+"."+memberName+"="+memberValue);
//			}
		}
		catch(Exception e)
		{
			CPersistentRoot.rollBackTransaction();
			throw new EFrameworkFault(e);
		}
		long tend=System.currentTimeMillis();
		CPersistentRoot.statsTimePersistAspectSetSaveObject+=(tend-tini);
	}
	

	
	/**
	 * Intercepts the before moment of all gets of any object property
	 */
	before(): get(* *) && !within(rhp.aof4oop.framework.core.*) && !within(rhp.aof4oop.framework.core.datamodel.*) && !within(rhp.aof4oop.framework.aspects.*)
	{
		long tini=System.currentTimeMillis();
		if(thisJoinPoint.getTarget()!=null)
		{
			
			//debugMsg(getJointPointInfo(thisJoinPoint));
//			String target_class=""+(thisJoinPoint.getTarget()!=null?thisJoinPoint.getTarget().getClass().getName():"????");
			//debugMsg("Before get property -> ObjectTarget:"+(thisJoinPoint.getTarget()!=null?thisJoinPoint.getTarget().getClass().getName()+" -> Field: "+thisJoinPoint.getSignature().getName():"["+thisJoinPoint.getTarget()+"]"));

			try
			{
				if(CPersistentRoot.isolateCurrentTransactionLog(thisJoinPoint.getTarget(),thisJoinPoint.getSignature().getName()))//If target object is involved in transaction, the current thread 
				{
//					TODO: testar desempenho
//					TODO: isolamento ao nivel do thread
					//The current thread see the current application's value 
					//Don't continue 
					//Checks if the pair object/member is under a transaction
					// If exists, gets its data from transaction log
//					debugMsg("ObjectTarget("+target_class+") is part of a transaction");
					return;
				}
				else if(CPersistentRoot.isPersistent(thisJoinPoint.getTarget())) 				//If target object is cached so all it properties may exist
				{
//					debugMsg("ObjectTarget("+target_class+") is persistent");
					//If the field is an object, lets start loading if needed
					long loid=CPersistentRoot.findCachedLogicalObjectID(thisJoinPoint.getTarget());
					CPersistentRoot.loadFieldObject(thisJoinPoint.getTarget(),loid,thisJoinPoint.getSignature().getName());
				}
				else
				{
//					debugMsg("ObjectTarget("+target_class+") is not persistent");
				}
			}
			catch(EFrameworkFault e)
			{
				throw e;
			}
			catch(Exception e)
			{
				throw new EFrameworkFault(e);
			}

		}
		long tend=System.currentTimeMillis();
		CPersistentRoot.statsTimePersistAspectGetLoadObject+=(tend-tini);
	}
//	after(): get(@rhp.aof4oop.framework.core.annotations.Aof4oopPersistent * *) 
//	          && !within(rhp.aof4oop.framework.core.*) 
//	          && !within(rhp.aof4oop.framework.core.datamodel.*) 
//	          && !within(rhp.aof4oop.framework.aspects.*)
//	
//	{
//		TODO::008:
//		System.out.println("Getter");
//	}
	/**
	 * Intercepts all method executions
	 *
	void around():execution (void *.*(..)) && !within(rhp.aof4oop.framework.core.*) && !within(rhp.aof4oop.framework.aspects.*)
	{
		String tmp=getJointPointInfo(thisJoinPoint);

		debugMsg("Before any method execution with any args(..): "+tmp);
	    proceed();	
	    debugMsg("After any method execution with any args(..): "+tmp);
	}
	*/
	//before(): execution(* rhp.aof4oop.framework.core.wrapper(..)) && !within(rhp.aof4oop.framework.core.*) && !within(rhp.aof4oop.framework.core.datamodel.*) && !within(rhp.aof4oop.framework.aspects.*)
	Object around(Object in,String[] params) : execution(Object rhp.aof4oop.framework.core.CPersistentRoot.wrapper(..)) && args(in,params)
	{
		return proceed(in,params);
	}
	/**
	 * TODO: move to aspect super-class
	 * Print debug info about the JoinPoint
	 * @param jp
	 * @return
	 */
	private String getJointPointInfo(JoinPoint jp)
	{
		if(jp!=null && jp.getTarget()!=null && jp.getTarget().getClass()!=null)
			return jp.toString()+"  On cache["+jp.getTarget().getClass()+"  key="+jp.getTarget().hashCode()+"]:"+CPersistentRoot.isPersistent(jp.getTarget());
		else if(jp!=null)
			return jp.toString();
		else
			return "???";
	}


}
