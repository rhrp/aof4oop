package rhp.aof4oop.framework.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import rhp.aof4oop.framework.core.datamodel.CInstanceMetaObject;
import rhp.aof4oop.framework.core.datamodel.CAttributeMetaObject;
import rhp.aof4oop.framework.core.datamodel.CTransactionLogRecord;
import rhp.aof4oop.framework.core.datamodel.CView;
import rhp.aof4oop.framework.core.exceptions.EFrameworkFault;



/**
 * Implements the Object persistence functionalities
 * 
 * Cache strategy
 * CCache relates an object memory instance with his image at storage through his loid.
 * Since array do not have a CObjectIntece meta-object, as happens wit normal object, a second type of cache was implemented.
 * This one, relates a memory object instance (the array) through the pair parentLoid/member that refers the object.
 * Of course that an object can have many entries through different key pairs.
 *   
 *
 * @author rhp
 *
 */
public class CPersistentRoot 
{
	private static String DEFAULT_ROOT_NAME	=	"root";
	private Hashtable<String,Object>	rootObjects;// A Hashtable is thread safe - TODO::030: Root object data structure instead of the object, take its LOID
	private int gcVerbose=0;
	/*Shared among all containers*/
	private	static CCache cachePersistentObjects=new CCache();															// Cache of persistent objects
	private static Hashtable<String,Object> objectMap=new Hashtable<String,Object>();									// key(parentLOID+member) :: Value(object reference) Objects relationships
	private static Hashtable<Long,CInstanceMetaObject> cacheInstanceObject=new Hashtable<Long,CInstanceMetaObject>();	// IMO cache
	private static Hashtable<String,CAttributeMetaObject> cacheObjectLink=new Hashtable<String,CAttributeMetaObject>();	// AMO cache
	private static int objectMapHits=0;
	private static int objectMapAccess=0;
	private static int objectMapHitsPart=0;
	private static int objectMapAccessPart=0;
	private static int metaObjectCacheAccess=0;
	private static int metaObjectCacheAccessPart=0;
	private static int metaObjectCacheHits=0;
	private static int metaObjectCacheHitsPart=0;
	private static Hashtable<String,String[]> typeParameters=new Hashtable<String,String[]>();
	
	private static long	transactionId=0;				// Id of the last transaction
	private static int	transactionLevel=0;			// Level of the current transaction
	private static ArrayList<CTransactionLogRecord>transactionLog=new ArrayList<CTransactionLogRecord>();		// Saves the current transaction's Undo data
	//private static Hashtable<Integer,Boolean>	transactionLogActive=new Hashtable<Integer,Boolean>();
	private static Boolean	transactionLogActive=new Boolean(true);
	
	public static long statsTimeDirectMapping=0;	// Time required to map db classes on app classes
	public static long statsTimeUserDefinedMapping=0;	// Time required to map db classes on app classes
	public static long statsTimeReadMapping=0;	// Time required to map db classes on app classes
	public static long statsTimeWriteMapping=0;	// Time required to map db classes on app classes
	public static long statsTimeCacheFindLOID=0;		// Time required to find  object LOID on cache
	public static long statsTimeCacheFindObject=0;	// Time required to find an object on cache
	public static long statsTimeSavingObjects=0;	// Time used saving objects
	public static long statsTotalSavedObjects=0;	// Number of saved objects
	public static long statsTimeUpdatingObjects=0;	// Time used updating objects
	public static long statsTotalUpdatedObjects=0;	// Number of updated objects
	public static long statsTimeGetFieldObject=0;	// On get aspect, the required time to load object through its LOID
	public static long statsTimeGetFieldArray=0;		// On get aspect, the required time to load array through its LOID
	public static long statsTimeSetFieldObject=0;	// On get aspect, the required time to save object through its LOID
	public static long statsTimeSetFieldArray=0;		// On get aspect, the required time to save array through its LOID
	public static long statsTimePersistAspectGetLoadObject=0;	// Persistence Aspect get
	public static long statsTimeStorageAspectGetLoadObject=0;	// Storage Aspect get
	public static long statsTimePersistAspectSetSaveObject=0;	// Persistence Aspect set
	public static long statsTimeStorageAspectSetSaveObject=0;	// Storage Aspect set
	public static long statsTimeStorageAspectSetSaveRootObject=0;	// Storage Aspect set root
	
	public static long statsTimeRecordingAMO	=	0;	// Time used while recording links between objects
	public static long statsTotalRecordedAMO	=	0;	// Number of recorded AMO meta-objects
	
	public static long statsTotalWeavings		=	0;	// Number of weaving processes
	public static long statsTimeWeaving			=	0;	// Time used while weaving processes
	
	public CPersistentRoot()
	{
		rootObjects=new Hashtable<String,Object>();
		//loadUBMO();
		try
		{
			CClassLoader cl=((CClassLoader)ClassLoader.getSystemClassLoader());
			cl.exportAllClassVersions(CDefinitions.AOF4OOP_CLASSLOADER_CLASSPATH,true);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	public void finalize() throws Throwable
	{
		close();
		super.finalize();
	}
	public synchronized void setRootObject(Object rootObject) 
	{
		setRootObject(DEFAULT_ROOT_NAME,rootObject);
	}
	public synchronized void setRootObject(String rootName,Object rootObject) 
	{
		if(rootObject!=null)
		{
			this.rootObjects.put(rootName,rootObject);
		}
		else
		{
			this.rootObjects.remove(rootName);
		}
	}
	
	public int getGcVerbose() {
		return gcVerbose;
	}
	public void setGcVerbose(int gcVerbose) {
		this.gcVerbose = gcVerbose;
	}
	public<T> T getRootObject() 
	{
		return getRootObject(DEFAULT_ROOT_NAME);
	}
	@SuppressWarnings("unchecked")
	public <T> T getRootObject(String rootName) 
	{
		return (T)(rootObjects!=null && rootName!=null?rootObjects.get(rootName):null);
	}
	public Hashtable<String,Object> getRootObjects()
	{
		return rootObjects;
	}
	/**
	 * Give a precreated view over the entities in database
	 * @param viewName
	 * @return
	 */
	public <T> List<T> view(String viewName)
	{
		CView view;
		view=getView(viewName);
		if(view==null)
		{
			throw new IllegalArgumentException("The view \""+viewName+"\" does not exists!!");
		}
		return view.getQuery().execute();
	}
	/**
	 * Creates a Query
	 * @param viewName
	 * @param query
	 * @return
	 */
	public boolean createView(String viewName,IQuery query)
	{
		//This must be executed inside the Storage Aspect
		return false;
	}
	public boolean dropView(String viewName)
	{
		//This must be executed inside the Storage Aspect
		return false;
	}
	/**
	 * Gets a saved view
	 * @param viewName
	 * @return
	 */
	public CView getView(String viewName)
	{
		//This must be executed inside the Storage Aspect
		return null;
	}
	public <T> List<T> query(IQuery query)
	{
		return query.execute();
	}
	public static int cacheSize()
	{
		synchronized(cachePersistentObjects)
		{
			return cachePersistentObjects.size();
		}
	}
	/**
	 * To be called by persistence aspect
	 * @param oid
	 * @param object
	 */
	@SuppressWarnings("unused")
	private static void saveCacheEntry(long loid,Object object,boolean emulated)
	{
		if(object==null)
		{
			return;
		}
		synchronized(cachePersistentObjects)
		{
			if(!cachePersistentObjects.isCachedByLOID(loid))
			{
				cachePersistentObjects.put(object,loid,emulated);
				//debugMsg("put:cache size:"+cache.size()+"  new object: "+object.getClass()+"  key="+CCache.cacheKey(loid));
			}
//			else
//			{
//				debugMsg("put:cache size:"+cache.size()+"  existing reference object: "+object.getClass()+"  key="+CCache.cacheKey(loid));
//			}
		}
	}
	@SuppressWarnings("unused")
	private static void deleteCacheEntry(Object memoryObject)
	{
		if(memoryObject==null)
		{
			return;
		}
		synchronized(cachePersistentObjects)
		{
			cachePersistentObjects.removeByLOID(findCachedLogicalObjectID(memoryObject));
		}
	}
	public static long findCachedLogicalObjectID(Object memoryCachedObject)
	{
		return cachePersistentObjects.findCachedLogicalObjectID(memoryCachedObject);
	}
	public static long[] findCachedLogicalArrayID(Object[] memoryCachedArray)
	{
		return cachePersistentObjects.findCachedLogicalArrayID(memoryCachedArray);
	}
	public static Object findCachedObject(long logicalObjectId)
	{
		return cachePersistentObjects.findCachedObjectByLOID(logicalObjectId);
	}
	/**
	 * To be called by persistence aspect
	 * @param loid
	 * @param mo
	 */
	public static void saveIMOCacheEntry(CInstanceMetaObject mo)
	{
		if(mo==null)
		{
			throw new IllegalArgumentException("Invalid meta-object");
		}
		cacheInstanceObject.put(mo.getId(),mo);
	}
	/**
	 * Search only in the cache
	 * @param loid
	 * @return
	 */
	public static CInstanceMetaObject findCachedMetaObjectInstance(long loid)
	{
		metaObjectCacheAccess++;
		metaObjectCacheAccessPart++;
		CInstanceMetaObject mo=cacheInstanceObject.get(loid);
		if(mo!=null)
		{
			metaObjectCacheHits++;
			metaObjectCacheHitsPart++;		
		}
		return mo;
	}
	/**
	 * Clear the IMO cache
	 */
	private static void reloadCacheIMO()
	{
		cacheInstanceObject=new Hashtable<Long,CInstanceMetaObject>();	// IMO cache
		//The loading process takes place at Storage aspect
	}
	/**
	 * Clear the AMO cache
	 */
	private static void reloadCacheAMO()
	{
		cacheObjectLink=new Hashtable<String,CAttributeMetaObject>();	// AMO cache
		//The loading process takes place at Storage aspect
	}	
	public static CInstanceMetaObject findMetaObjectInstance(long logicalObjectId)
	{
		//Implemented as an aspect
		return null;
	}
	public static int cacheObjectLinkSize()
	{
		return cacheObjectLink.size();
	}
	public static int cacheInstanceObjectSize()
	{
		return cacheInstanceObject.size();
	}
	public static void saveAMOCacheEntry(CAttributeMetaObject amo)
	{
		if(amo==null)
		{
			throw new IllegalArgumentException("Invalid AMO");
		}
		//cacheObjectLink.put("loid("+amo.getParentObjectId()+")."+amo.getMember(),amo);
		cacheObjectLink.put(amo.calcKey(),amo);
	}
	public static void deleteAMOCacheEntry(CAttributeMetaObject amo)
	{
		if(amo==null)
		{
			throw new IllegalArgumentException("Invalid AMO");
		}
		cacheObjectLink.remove(amo.calcKey());
	}
	public static Hashtable<String, CAttributeMetaObject> getCacheObjectLink()
	{
		return cacheObjectLink;
	}
	public static CAttributeMetaObject findCachedMetaObjectLink(long parentLoid,String member)
	{
		metaObjectCacheAccess++;
		metaObjectCacheAccessPart++;
		CAttributeMetaObject mo=cacheObjectLink.get(CAttributeMetaObject.calcKey(parentLoid,member));
		if(mo!=null)
		{
			metaObjectCacheHits++;
			metaObjectCacheHitsPart++;		
		}
		return mo;
	}
	public static boolean _isLocked(Object memoryObject,String memberName)
	{
		return (findCurrentTransactionLog(memoryObject,memberName)!=null);
	}
	/**
	 * Finds the current (the last one) transaction log for a pair object/member
	 * @param memoryObject
	 * @param memberName
	 * @return
	 */
	public static CTransactionLogRecord findCurrentTransactionLog(Object memoryObject,String memberName)
	{
		if(memoryObject==null)
		{
			throw new IllegalArgumentException("Invalid arguments");
		}
		synchronized (transactionLog) 
		{
			CTransactionLogRecord out=null;
			if(transactionLog.size()>0)
			{
				for(CTransactionLogRecord trx:transactionLog)
				{
					if(trx.getObject()==memoryObject && trx.getMemberName().equals(memberName))
					{
						// take the last one
						out=trx;
					}
				}
			}
			return out;
		}
	}

	/**
	 * Checks if the pair object/member is under a transaction
	 * If exists, gets data from transaction log
	 * @param memoryObject
	 * @param memberName
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static boolean isolateCurrentTransactionLog(Object memoryObject,String memberName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException
	{
		synchronized (transactionLog) 
		{
			CTransactionLogRecord trx=findCurrentTransactionLog(memoryObject, memberName);
			if(trx!=null)
			{
				setObjectField(memberName, memoryObject,trx.getValue());
				//System.out.println("TRX ISOLATION["+trx.getTrxName()+"]["+transactionLog.size()+"]: "+memoryObject.getClass().getName()+"."+memberName+"="+trx.getValue());
				return true;
			}
			else
			{
				//System.out.println("TRX ISOLATION none");
				return false;
			}
		}
	}
	/**
	 * Saves the current transaction into the Log
	 * @param memoryObject
	 * @param memberName
	 * @param memberValue
	 */
	private static void __saveTransactionLog(Object memoryObject,String memberName,Object memberValue)
	{
		if(memoryObject==null || memberName==null || memberName.isEmpty())
		{
			throw new IllegalArgumentException("Invalid arguments");
		}
		synchronized (transactionLog) 
		{
//			String target_class_name=(memoryObject!=null?memoryObject.getClass().getCanonicalName():"NULL");
//			debugMsg("SAVELOG :: "+target_class_name+"."+memberName+"="+(memberValue!=null && isPrimitiveDataTypeObject(memberValue.getClass().getCanonicalName())?memberValue:"????"));
			transactionLog.add(new CTransactionLogRecord("trx#"+transactionId+"L"+transactionLevel,transactionId,transactionLevel,memoryObject, memberName,memberValue));
		}
	}
	/**
	 * 
	 * @param new_state
	 * @return - old state
	 */
	public static boolean setTransactionLogActive(boolean new_state)
	{
		boolean old_state;
		synchronized (transactionLog) 
		{
			old_state=isTransactionLogActive();
			//transactionLogActive.put(transactionLevel,new Boolean(new_state));
			transactionLogActive=new Boolean(new_state);
		}
		return old_state;
	}
	private static boolean isTransactionLogActive()
	{
		Boolean b;
		synchronized (transactionLog) 
		{
//			b=transactionLogActive.get(transactionLevel);
//			if(b==null)
//			{
//				//Init flags
//				transactionLogActive.put(transactionLevel,new Boolean(true));
//				b=true;
//			}
			b=transactionLogActive.booleanValue();
		}
		return b;
	}
	/**
	 * 
	 * @param memoryObject
	 * @param memberName
	 * @param prop
	 */
	private static void __doTransaction(Object memoryObject,String memberName,Object memberValue)
	{
		//String target_class_name=(memoryObject!=null?memoryObject.getClass().getCanonicalName():"NULL");
		//debugMsg("DOTRANSACTION :: "+target_class_name+"."+memberName+"="+(memberValue!=null && isPrimitiveDataTypeObject(memberValue.getClass().getCanonicalName())?memberValue:"????"));		
		//If target object is cached so all it properties also have to be cached
		if(CPersistentRoot.isPersistent(memoryObject))
		{
			//System.out.println("Target is persistent: "+target_class_name+" LOID="+CPersistentRoot.findCachedLogicalObjectID(memoryObject)+"   Emulated="+CPersistentRoot.isEmulated(memoryObject)+" set "+target_class_name+"."+memberName+"="+(memberValue!=null?"<value>":"<NULL>"));
			CPersistentRoot.saveFieldObject(memoryObject,memberName,memberValue);		// Save object changes
		}
		else
		{
			if(CPersistentRoot.isReachable(memoryObject))
			{ 
				//startDebug
				//System.out.println("Target is reachable: "+target_class_name+"  set "+target_class_name+"."+memberName+"="+(memberValue!=null?"<value>":"<NULL>"));
//				ArrayList<Long> loids=CPersistentRoot.findRechability(memoryObject);
//				for(Long loid:loids)
//				{
//					CInstanceMetaObject rimo=CPersistentRoot.findCachedMetaObjectInstance(loid);
//					debugMsg("The Target "+target_class_name+" is not persistent, but is reachable from "+rimo.getId()+" ("+rimo.getClassName()+")");
//					//TODO: check the object is recursively saved even if it is persistent. Is our case. 
//				}
				//endDebug
				CPersistentRoot.saveObject(memoryObject);
			}
			else
			{
//				System.out.println("Target is transient: "+target_class_name+"("+System.identityHashCode(memoryObject)+")  Member "+memberName+" will not be saved");
			}
		}
	}
	@SuppressWarnings("unused")
	private static void doTransaction(Object memoryObject,String memberName,Object memberValue) throws EFrameworkFault
	{
		synchronized (transactionLog) 
		{
			//System.out.println("doTrx trx #"+transactionId+" L"+transactionLevel);
			if(transactionLevel>1)
			{
				//Saves transaction into Log 
				__saveTransactionLog(memoryObject, memberName, memberValue);
			}
			else
			{
				//Saves transaction into database
				__doTransaction(memoryObject, memberName, memberValue);
			}
		}		
	}
	/**
	 * Turns off autocommit mode.
	 * While autocommit mode is turned off, changes made to the database are not committed until you end the transaction by calling commitTransaction().
	 * Calling rollBackTransaction() will roll back all changes to the database and return the connection to autocommit mode. 
	 * @return
	 */
	public static long beginTransaction()
	{
		synchronized (transactionLog) 
		{
			if(transactionLevel==0)
			{
				transactionId+=1;
			}
			transactionLevel+=1;
		}
//System.out.println("Begin: "+transactionId+"  level "+transactionLevel);		
		return transactionId;
	}
	public static boolean commitTransaction()
	{
		synchronized (transactionLog) 
		{
			if(transactionLevel==1)
			{
				if(transactionLog.size()>0)
				{
					try
					{
						beginTransactionDB();
						System.out.println("Commiting trx #"+transactionId);
						for(CTransactionLogRecord trx:transactionLog)
						{
							System.out.println("  "+trx);
							__doTransaction(trx.getObject(),trx.getMemberName(),trx.getValue());
						}
						//Clear Log for the next transaction
						transactionLog=new ArrayList<CTransactionLogRecord>();
						transactionLevel=0;
						commitTransactionDB();
//System.out.println("Commit: "+transactionId+"  level "+transactionLevel+"   log size="+transactionLog.size());						
						return true;
					}
					catch(Exception e)
					{
						rollBackTransactionDB();
						throw new EFrameworkFault(e);
					}
				}
				else
				{
//System.out.println("End: "+transactionId+"  level "+transactionLevel+"   log size="+transactionLog.size());
					transactionLevel=0;
					return false;
				}
			}
			else
			{
//System.out.println("End nested: "+transactionId+"  level "+transactionLevel+"   log size="+transactionLog.size());							
				//There are nested transactions
				transactionLevel-=1;
				return false;
			}
		}
	}
	public static boolean rollBackTransaction()
	{
		synchronized (transactionLog) 
		{
			if(transactionLevel==1)
			{
				System.out.println("RollBack trx #"+transactionId);
				for(int i=transactionLog.size();i>0;i--)
				{
					CTransactionLogRecord trx = transactionLog.get(i-1);
					System.out.println("  "+trx);
					trx.rollback();
				}
				
				//TODO: rollback Meta objects
				reloadCacheAMO();
				reloadCacheIMO();
				
				//Clear Log for the next transaction
				transactionLog=new ArrayList<CTransactionLogRecord>();
				transactionLevel=0;
				return true;
			}
			else
			{
				//There are nested transactions
				transactionLevel-=1;
				return false;
			}
		}
	}
	private static long beginTransactionDB()
	{
		//This must be executed inside the Storage Aspect
		return 0;		
	}
	private static boolean commitTransactionDB()
	{
		//This must be executed inside the Storage Aspect
		return false;
	}
	private static boolean rollBackTransactionDB()
	{
		//This must be executed inside the Storage Aspect
		return false;
	}
	/**
	 * Find all cached AMO belonging to a object
	 * @param parentLoid
	 * @return
	 */
	public static ArrayList<CAttributeMetaObject> findCachedMetaObjectLinks(long parentLoid)
	{
		metaObjectCacheAccess++;
		metaObjectCacheAccessPart++;

		ArrayList<CAttributeMetaObject> out=new ArrayList<CAttributeMetaObject>();
		for(CAttributeMetaObject amo:cacheObjectLink.values())
		{
			if(amo.getParentObjectId()==parentLoid)
			{
				metaObjectCacheHits++;
				metaObjectCacheHitsPart++;
				out.add(amo);
			}
		}
		return out;
	}
	/**
	 * Counts the number of AMO (attributes) of a Logical Object  
	 * @param parentLoid
	 * @return
	 */
	public static int countCachedMetaObjectLinkByLOID(long parentLoid)
	{
		int n=0;
		
		for(CAttributeMetaObject amo:cacheObjectLink.values())
		{
			if(amo.getParentObjectId()==parentLoid)
			{
				n++;
			}
		}
		return n;
	}
	public static void saveMapEntry(long parentLogicalObjectId,String member,Object object)
	{
		if(object!=null)
		{
			//objectMap.put("loid("+parentLogicalObjectId+")->"+member,object);
			objectMap.put(CAttributeMetaObject.calcKey(parentLogicalObjectId,member),object);// Uses the same structure of key
			//System.out.println("loid("+parentLogicalObjectId+")->"+member+"  size="+objectMap.size());
		}
	}
	public static Object[] findMappedArray(long parentLogicalObjectId,String member)
	{
		//Object[] array=(Object[])objectMap.get("loid("+parentLogicalObjectId+")->"+member);
		Object[] array=(Object[])objectMap.get(CAttributeMetaObject.calcKey(parentLogicalObjectId,member));
		if(array!=null)
		{
			objectMapHits++;
			objectMapHitsPart++;
			//System.out.println("loid("+parentLogicalObjectId+")->"+member+"  size="+objectMap.size()+" Total hits:"+objectMapHits);
		}
		objectMapAccess++;
		objectMapAccessPart++;
		return array;
	}

	/**
	 * To be called by persistence aspect 
	 * Loads an object field from DB
	 * @param object of the object to load
	 * @param logicalObjectId
	 * @param field
	 * @return
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 */
	@SuppressWarnings("unused")
	private static void loadFieldObject(Object object,long logicalObjectId,String fieldName) throws SecurityException, NoSuchFieldException
	{
		//This is never executed!
		System.out.println("I need: LOID{"+logicalObjectId+"}."+fieldName);
	}
	/**
	 * To be called by persistence aspect
	 * @param object - parent object
	 * @param fieldName - object 
	 */
	private static void saveFieldObject(Object object,String fieldName,Object value)
	{
		//This is never executed!
		System.out.println("I need save: "+object.getClass().getName()+"."+fieldName+"="+value);
	}
	private static void saveObject(Object object)
	{
		//This is never executed!
		System.out.println("I need save: "+object.getClass().getName());
	}
	/**
	 * Activates all non-primitive attributes  
	 * @param memoryObject
	 */
	public static void activateAllObjectAttributes(Object loadedMemoryObject,CInstanceMetaObject imo)
	{
		//This is never executed!
		System.out.println("I need to activate LOID:"+imo.getId());
	}
	public static synchronized void dumpCache()
	{
		dumpCache(false);
	}
	public static synchronized void dumpCache(boolean fillWithTrash)
	{
		cachePersistentObjects.dump();
		if(fillWithTrash)
		{
			//Fill the cache with trash to force the out of some unnecessary entries 
			cachePersistentObjects.fillWidthTrash();
		}
		cachePersistentObjects.dump();
	}
	public synchronized int countCache()
	{
		return cachePersistentObjects.size();
	}
	public static void resetLapObjectMap()
	{
		objectMapAccessPart=0;
		objectMapHitsPart=0;
		metaObjectCacheAccessPart=0;
		metaObjectCacheHitsPart=0;
		cachePersistentObjects.resetObjectMapLap();
	}
	public static void resetStats()
	{
		statsTimeDirectMapping=0;
		statsTimeUserDefinedMapping=0;
		statsTimeReadMapping=0;
		statsTimeWriteMapping=0;
		statsTimeCacheFindLOID=0;
		statsTimeCacheFindObject=0;
		statsTimeSavingObjects=0;
		statsTotalSavedObjects=0;
		statsTimeUpdatingObjects=0;
		statsTotalUpdatedObjects=0;
		statsTimeGetFieldObject=0;
		statsTimeGetFieldArray=0;
		statsTimeSetFieldObject=0;
		statsTimeSetFieldArray=0;
		statsTimePersistAspectGetLoadObject=0;
		statsTimeStorageAspectGetLoadObject=0;
		statsTimePersistAspectSetSaveObject=0;
		statsTimeStorageAspectSetSaveObject=0;
		statsTimeStorageAspectSetSaveRootObject=0;
		
		statsTimeRecordingAMO	=	0;
		statsTotalRecordedAMO	=	0;
		
		statsTotalWeavings		=	0;
		statsTimeWeaving			=	0;
	}
	public static void showObjectMapStatus()
	{
		System.out.println("Object Map Status:");
		System.out.println("Since start: objectMapAccess="+objectMapAccess+"   objectMapHits="+objectMapHits+"   hits="+(((double)objectMapHits/(double)objectMapAccess)*100)+"%");
		System.out.println("This lap: objectMapAccessPart="+objectMapAccessPart+"   objectMapHitsPart="+objectMapHitsPart+"   hits="+(((double)objectMapHitsPart/(double)objectMapAccessPart)*100)+"%");
		
		System.out.println("Meta Object Cache Status:");
		System.out.println("Since start: metaObjectCacheAccess="+metaObjectCacheAccess+"   metaObjectCacheHits="+metaObjectCacheHits+"   hits="+(((double)metaObjectCacheHits/(double)metaObjectCacheAccess)*100)+"%");
		System.out.println("This lap: metaObjectCacheAccessPart="+metaObjectCacheAccessPart+"   objectMapHitsPart="+metaObjectCacheHitsPart+"   hits="+(((double)metaObjectCacheHitsPart/(double)metaObjectCacheAccessPart)*100)+"%");
		
		cachePersistentObjects.showStatus();
		
		//Dump class cache
		//((CClassLoader)ClassLoader.getSystemClassLoader()).printClassCache();
	}
	/**
	 * Checks if the object is emulated from persistent image
	 * @param memory_object
	 * @return
	 */
	public static boolean isEmulated(Object memory_object)
	{
		//return cache._isCached(object);
		//If tit has a LOID, thus is persistent
		return (cachePersistentObjects.findCachedFlagEmulated(memory_object));
	}
	/**
	 * 
	 * @param object
	 * @return
	 */
	public static boolean isCached(Object memory_object)
	{
		return cachePersistentObjects.isCached(memory_object);
	}
	/**
	 * An object may be persistent, but not cached.
	 * @param object
	 * @return
	 */
	public static boolean isPersistent(Object memory_object)
	{
		//return cache._isCached(object);
		//If tit has a LOID, thus is persistent
		return (cachePersistentObjects.findCachedLogicalObjectID(memory_object)>0);
	}
	/**
	 * Check if a object is reachable
	 * That is, the object is not persistent but it is reachable from another that is persistent
	 * @param object
	 * @return
	 */
	public static boolean isReachable(Object memory_object)
	{
		return findRechability(memory_object).size()>0;
	}
	public static ArrayList<Long> findRechability(Object memory_object)
	{
		return cachePersistentObjects.findRechability(memory_object);
	}
	public static int registerReachableObjectTree(Object memory_object,long loid)
	{
		return cachePersistentObjects.registerReachableObjectTree(memory_object,loid);
	}
	/**
	 * TODO:020
	 * @param objClassName
	 * @return
	 */
	public static boolean isPrimitiveDataTypeObject(String objClassName)
	{
		return (objClassName!=null 
		&& (objClassName.equals("java.lang.Integer")
			|| objClassName.equals("java.lang.Long")
			|| objClassName.equals("java.lang.Float")
			|| objClassName.equals("java.lang.Double")
			|| objClassName.equals("java.lang.String")
			|| objClassName.equals("java.lang.Boolean")
			|| objClassName.equals("java.util.Date")
			|| objClassName.equals("sun.util.calendar.Gregorian")
			));
	}
	/**
	 * TODO:020
	 * @param objClassName
	 * @return
	 */
	public static boolean isNonVersionedDataTypeObject(String objClassName)
	{
		return (objClassName!=null
			&& (objClassName.equals("java.awt.geom.Point2D")
			|| objClassName.equals("java.awt.geom.Point2D.Float")
			|| objClassName.equals("java.awt.geom.Point2D.Double")
			|| objClassName.equals("java.awt.geom.Point2D$Float")
			|| objClassName.equals("java.awt.geom.Point2D$Double")
			|| objClassName.startsWith("java.")
			));
	}	
	protected static boolean isFrameworkDataType(String objClassName)
	{
		return (objClassName!=null && objClassName.startsWith("rhp.aof4oop.framework"));
	}
	/**
	 * For debug proposes, this method dump an object tree of relationships
	 * @param object
	 * @param level
	 */
	public static void dumpObjectTree(Object object,int level) 
	{
		Object oField;
		Object[] aField;
		String cField;
		String padding;

		if(level>2)
		{
			return;
		}
		padding="";
		for(int i=0;i<level;i++)
		{
			padding+="  ";
		}
		debugMsg(padding+"Level: "+level+" --------------------------------------------------------------------------");
		Field[] flds=object.getClass().getDeclaredFields();
		debugMsg(padding+"Fields of: "+object.getClass().getName()+" ("+(flds!=null?""+flds.length:"null")+")");
		if(flds!=null && flds.length>0)
		{
			for(int i=0;i<flds.length;i++)
			{
				if(flds[i].getName()!=null && !flds[i].getName().startsWith("ajc$"))
				{
					try 
					{
						cField=flds[i].getType().getName();
						//System.out.print(padding+"Field: "+flds[i].getName()+"    type: "+cField+"  Comp.Type: "+flds[i].getType().getComponentType()+"   Modifiers: "+flds[i].getModifiers());
						System.out.format("%s  \t\tField: %s  \tType: %s  \tComponent Type: %s",
	                               flds[i],flds[i].getName(),flds[i].getType(),flds[i].getType().getComponentType());
						flds[i].setAccessible(true);
						if(flds[i].getType().isArray())
						{
							//Array
							aField=(Object[])flds[i].get(object);
							if(aField!=null)
							{
								System.out.println(" value = "+aField.toString()+"  length="+aField.length);
								if(cField.startsWith("Lrhp."))
								{
									dumpObjectTree(aField,level+1);
								}
							}
							else
							{
								System.out.println(" value = null");
							}
						}
						else
						{
							//Object
							oField=flds[i].get(object);
							if(oField!=null)
							{
								System.out.println(" value = "+oField.toString());
								if(cField.startsWith("rhp."))
								{
									//dumpTree(oField,level+1);
								}
							}
							else
							{
								System.out.println(" value = null");
							}
						}
					} catch (SecurityException e) 
					{
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					catch (Exception e) 
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	public void gc()
	{
		gc(gcVerbose);
	}
	public void gc(int verbose)
	{
		// Call GC
		System.out.println("null GC");
	}
	public void close()
	{
		gc();
	}
	public long getLOID(Object obj)
	{
		return findCachedLogicalObjectID(obj);
	}
	public long getOID(Object obj)
	{
		return 0;
	}
	/**
	 * Activates a database object
	 * @param obj
	 * @param depth
	 */
	public static void activate(Object dbObject,int depth)
	{
		System.out.println("null object activator");
	}
	/**
	 * 
	 * @param dbObject
	 * @param imo
	 * @param depth - Depth of loading
	 * @param excludeMembers - Members to be excluded in this level of deph
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void activateAllDBObjectAttributes(Object dbObject,CInstanceMetaObject imo,int depth,ArrayList<String> excludeMembers) throws IllegalArgumentException, IllegalAccessException
	{
		System.out.println("null object activator");
	}
	public void showAllDB(boolean verbose)
	{
		// Show All Database
		System.out.println("null show all db");
	}
	private static void debugMsg(String msg)
	{
		System.out.println("[CPersistenRoot]::"+msg);
	}
	/**
	 * This method is used as a wrapper in application source code to intercept construction of parametric classes 
	 * @param <T>
	 * @param in
	 * @param paramTypes
	 * @return
	 */
	public static <T> T wrapper(T in,String[] paramTypes)
	{
		saveRuntimeTypeParameters(in, paramTypes);
		return in;
	}
	/**
	 * Saves runtime information of that object
	 * @param in
	 * @param paramTypes
	 */
	public static void saveRuntimeTypeParameters(Object obj,String[] paramTypes)
	{
		
		if(paramTypes!=null && paramTypes.length>0)
		{
			//Save parametric information
			typeParameters.put(memoryObjectKey(obj),paramTypes);
			String tmp=""+obj+"<";
			int i=0;
			for(String p: paramTypes)
			{
				tmp+=(i++>0?",":"");
				tmp+=p;
			}
			tmp+=">";
			debugMsg(tmp);
		}
		else
		{
			debugMsg("<none>");
		}
	}
	/**
	 * Check if there is runtime information for that object
	 * @param obj
	 * @return
	 */
	public static String[] findRuntimeTypeParameters(Object obj)
	{
		debugMsg("size="+typeParameters.size());
		return typeParameters.get(memoryObjectKey(obj));
	}
	/**
	 * Recursively, gets all field names
	 * @param clazz
	 * @param flds
	 * @param level
	 * @return
	 */
	public static Field[] reflectFields(Class<?> clazz)
	{
		return __reflectFields(clazz, null,1);
	}
	private static Field[] __reflectFields(Class<?> clazz,Field[] flds,int level) 
	{
		Field[] fldsClass=clazz.getDeclaredFields();
//		System.out.println("__reflectFields::level["+level+"]="+fldsClass.length+"  "+clazz.getName()+"  super:"+clazz.getSuperclass().getName());
//		for(Field f:fldsClass)
//		{
//			System.out.println("   "+f.getName()+"::"+f.getType().getCanonicalName());
//		}
		if(!clazz.getSuperclass().getCanonicalName().startsWith("java."))
		{
			Field[] fldsSuper=__reflectFields(clazz.getSuperclass(), fldsClass,level+1);
			if(fldsSuper!=null && fldsSuper.length>0)
			{
				flds=__add(fldsClass,fldsSuper);
			}
			return flds;
		}
		else
		{
			return fldsClass;
		}
	}
	private static Field[] __add(Field[] flds1,Field[] flds2)
	{
		Field[] out=new Field[flds1.length+flds2.length];
		for(int i=0;i<flds1.length;i++)
		{
			out[i]=flds1[i];
		}
		for(int i=0;i<flds2.length;i++)
		{
			out[i+flds1.length]=flds2[i];
		}
		return out;
	}
	/**
	 * Find the field in a class
	 * @param clazz
	 * @param name
	 * @return
	 * @throws NoSuchFieldException
	 */
	private static Field __getField(Class<?> clazz,String name,int level) throws NoSuchFieldException
	{
		try
		{
//			System.out.println("Level: "+level+"::"+clazz.getCanonicalName());
			Field f=clazz.getDeclaredField(name);
			return f;
		}
		catch (NoSuchFieldException e) 
		{
			if(!clazz.getSuperclass().equals(Object.class))
			{
				level++;
				return __getField(clazz.getSuperclass(), name,level);
			}
			else
			{
				throw e;
			}
		}
	}
	public static Field getField(Class<?> clazz,String name) throws NoSuchFieldException
	{
		return __getField(clazz, name,0);
	}
	public static Object getFieldValue(Object obj,String fieldName) throws InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException, ClassNotFoundException
	{
		try
		{
			Field f=CPersistentRoot.getField(obj.getClass(),fieldName);
			f.setAccessible(true);
			return f.get(obj);
		}
		catch (NoSuchFieldException e) 
		{
			debugMsg("   Field: name ignored ("+fieldName+")");
			e.printStackTrace();
		}
		return null;
	}
	private static Object setObjectField(String fld,Object object,Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException
	{
		Field f=CPersistentRoot.getField(object.getClass(), fld);
		return setObjectField(f, object, value);
	}
	/**
	 * This method works like a wrapper to help ADomainintegrity aspect
	 * @param f
	 * @param object
	 * @param value
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private static Object setObjectField(Field f,Object object,Object value) throws IllegalArgumentException, IllegalAccessException
	{
		f.setAccessible(true);
		Object oldVal=f.get(object);
		f.set(object,value);
		return oldVal;
	}
	/**
	 * Generates a unique string key for a memory loaded object
	 * @param object
	 * @return
	 */
	private static String memoryObjectKey(Object object)
	{
		return (object!=null?Integer.toHexString(System.identityHashCode(object)):null);
	}
	public static void printStats()
	{
		System.out.println("System stats:");
		System.out.println(" * Time used on persistence advice Get: "+CPersistentRoot.statsTimePersistAspectGetLoadObject+" ms");
		System.out.println(" * Time used on storage advice Get: "+CPersistentRoot.statsTimeStorageAspectGetLoadObject+" ms");
		System.out.println("   -Time used on field object geters: "+CPersistentRoot.statsTimeGetFieldObject+" ms");
		System.out.println("   -Time used on field array geters: "+CPersistentRoot.statsTimeGetFieldArray+" ms");
		System.out.println("   -Time used on ReadAdapter: "+CPersistentRoot.statsTimeReadMapping+" ms");
		System.out.println(" * Time used on persistence advice Set: "+CPersistentRoot.statsTimePersistAspectSetSaveObject+" ms");
		System.out.println(" * Time used on storage advice Set Object: "+CPersistentRoot.statsTimeStorageAspectSetSaveObject+" ms");
		System.out.println(" * Time used on storage advice Set Root: "+CPersistentRoot.statsTimeStorageAspectSetSaveRootObject+" ms");
		System.out.println("   -Time used saving objects: "+CPersistentRoot.statsTimeSavingObjects+" ms");
		System.out.println("   -Total of saved objects: "+statsTotalSavedObjects);
		System.out.println("   -Time used updating objects: "+CPersistentRoot.statsTimeUpdatingObjects+" ms");
		System.out.println("   -Total of updated objects: "+statsTotalUpdatedObjects);
		System.out.println("   -Time used on field object setters: "+CPersistentRoot.statsTimeSetFieldObject+" ms");
		System.out.println("   -Time used on field array setters: "+CPersistentRoot.statsTimeSetFieldArray+" ms");
		System.out.println("   -Time used on WriteAdapter: "+CPersistentRoot.statsTimeWriteMapping+" ms");
		System.out.println(" * Time used on direct class mapping: "+CPersistentRoot.statsTimeDirectMapping+" ms");
		System.out.println(" * Time used on user-defined class mapping: "+CPersistentRoot.statsTimeUserDefinedMapping+" ms");
		System.out.println(" * Time used on searching LOIDs in cache: "+CPersistentRoot.statsTimeCacheFindLOID+" ms");
		System.out.println(" * Time used on searching objects in cache: "+CPersistentRoot.statsTimeCacheFindObject+" ms");
		System.out.println(" * Time used on recording AMO meta-objects: "+statsTimeRecordingAMO+" ms");
		System.out.println(" * Number of new AMO: "+statsTotalRecordedAMO);
		System.out.println(" * Number of cached AMO: "+cacheObjectLinkSize());
		System.out.println(" * Number of cached IMO: "+cacheInstanceObjectSize());
		System.out.println(" * Number of cached Objects: "+cacheSize());
		System.out.println(" * Number of Weavings: "+statsTotalWeavings);
		System.out.println(" * Total time of Weavings: "+statsTimeWeaving+" ms");
		cachePersistentObjects.showStatus();
	}
}
