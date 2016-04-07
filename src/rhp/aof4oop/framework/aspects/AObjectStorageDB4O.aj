package rhp.aof4oop.framework.aspects;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.TreeMap;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;






import javassist.CannotCompileException;
import javassist.NotFoundException;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.ext.DatabaseClosedException;
import com.db4o.ext.DatabaseReadOnlyException;
import com.db4o.query.Predicate;
import com.db4o.reflect.jdk.JdkReflector;

import rhp.aof4oop.framework.core.CClassLoader;
import rhp.aof4oop.framework.core.CDefinitions;
import rhp.aof4oop.framework.core.CInstanceAdaptationMetadata;
import rhp.aof4oop.framework.core.CPersistentRoot;
import rhp.aof4oop.framework.core.CQuery;
import rhp.aof4oop.framework.core.IQuery;
import rhp.aof4oop.framework.core.datamodel.CClassVersionMetaObject;
import rhp.aof4oop.framework.core.datamodel.CInstanceMetaObject;
import rhp.aof4oop.framework.core.datamodel.CAttributeMetaObject;
import rhp.aof4oop.framework.core.datamodel.CRootMetaObject;
import rhp.aof4oop.framework.core.datamodel.CUpdateBackdateMetaObject;
import rhp.aof4oop.framework.core.datamodel.CView;
import rhp.aof4oop.framework.core.exceptions.EFrameworkFault;

/**
 * Implements the cache persistence aspect on DB4O
 * @author rhp
 *
 */
public privileged aspect AObjectStorageDB4O extends APersistence
{
	private ObjectContainer db;
	private AObjectStorageDB4O()
	{
		debugMsg("Open database");
//		Db4o.configure().setClassLoader(ClassLoader.getSystemClassLoader());
//		db=Db4o.openFile("/tmp/aof4oop.dbf");
		
		
		EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
		configuration.cache().slotCacheSize(1024);
		//configuration.file().storage(new MemoryStorage());

		configuration.common().reflectWith(new JdkReflector(ClassLoader.getSystemClassLoader()));
		db=Db4oEmbedded.openFile(configuration, CDefinitions.AOF4OOP_DATABASE);
	}
	protected void finalize() throws Throwable
	{
		db.commit();
		debugMsg("Close database");
		db.close();
		
		super.finalize();
	}
	/**
	 * Before save a root object
	 * @param rtName
	 * @param newObj
	 */
	before(String rtName,Object newObj) : execution(void  rhp.aof4oop.framework.core.CPersistentRoot.setRootObject(..)) && args(rtName,newObj)
	{
		Object obj;
		ObjectSet<CRootMetaObject> result;

		obj=((CPersistentRoot)thisJoinPoint.getTarget()).rootObjects.get(rtName);
		if(obj!=null && (obj!=newObj || newObj==null))
		{
			//Delete from DB
			debugMsg("The root object \""+rtName+"\"("+obj+") is being replaced");
			result=db.queryByExample(CRootMetaObject.class);
			for(CRootMetaObject ro:result)
			{
				if(ro.getContainerName().equals(rtName))
				{
					db.delete(ro);
				}
			}
			//Apena elimina o apontador de Root, o CG faz o resto.
			((CPersistentRoot)thisJoinPoint.getTarget()).rootObjects.remove(rtName);
		} 
	}
	
	/**
	 * After the setter of a root object
	 * @param rt
	 */
	after(String rtName,Object newRtObj) : execution(void  rhp.aof4oop.framework.core.CPersistentRoot.setRootObject(..)) && args(rtName,newRtObj)
	{
		Object memoryObject;
		
		long t_inig=System.currentTimeMillis();
		
		//obj and newRtObj are the same object instance
		memoryObject=((CPersistentRoot)thisJoinPoint.getTarget()).rootObjects.get(rtName);
		debugMsg("store obj="+memoryObject);
		if(memoryObject!=null)
		{
			try 
			{
				//Creates a new root object link
				if(memoryObject.getClass().isArray())
				{
					saveArray((CPersistentRoot)thisJoinPoint.getTarget(), memoryObject);
					recordRootArray(rtName,(Object[])memoryObject);
				}
				else
				{
					saveObject((CPersistentRoot)thisJoinPoint.getTarget(),memoryObject);
					recordRootObject(rtName, memoryObject);// memoryObject already is persistent
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		CPersistentRoot.statsTimeStorageAspectSetSaveRootObject+=(System.currentTimeMillis()-t_inig);
	}
	/**
	 * Starts the PersistenceContainer
	 */
	after() : execution(rhp.aof4oop.framework.core.CPersistentRoot.new(..))
	{
		CPersistentRoot psRoot;
		ObjectSet<rhp.aof4oop.framework.core.datamodel.CRootMetaObject> result;
	
		CClassLoader cl=(CClassLoader)ClassLoader.getSystemClassLoader();
			
		psRoot=((CPersistentRoot)thisJoinPoint.getTarget());
		debugMsg("root object size="+psRoot.getRootObjects().size());
		try
		{
			result=db.queryByExample(rhp.aof4oop.framework.core.datamodel.CRootMetaObject.class);
			debugMsg("Root="+(result!=null?""+result.size():"null"));

			for(rhp.aof4oop.framework.core.datamodel.CRootMetaObject ro: result)
			{
				if(ro.isArray())
				{
					debugMsg("get the root array LOIDs size"+ro.getLogicalArrayId().length);
					//Object[] obj_array=new Object[ro.getLogicalArrayId().length];
					Object[] obj_array=(Object[])Array.newInstance(Class.forName(ro.getObjectClassName()),ro.getLogicalArrayId().length);
					int i=0;
					for(long loid:ro.getLogicalArrayId())
					{
						CInstanceMetaObject oInst=findObjectInstance(loid);
						debugMsg("get the root object OID="+oInst.getOID()+"   class="+oInst.getClassName()+"  version="+oInst.getClassVersion());
						Object dbObject=db.ext().getByID(oInst.getOID());
						db.activate(dbObject,1);
		
						CClassVersionMetaObject ocl=cl.findApplicationObjectClassVersion(oInst.getClassName());
						Object obj=ocl.readerAdapter(dbObject,null,cl,oInst,(CAttributeMetaObject)null);
						debugMsg("root object ("+ro.getContainerName()+") version="+ocl.getClassVersion()+"  obj="+obj+"  objKey="+System.identityHashCode(obj));
						saveOnCache(ocl,oInst,obj);//ro.getLogicalObjectId()
						if(oInst.hasTypeParameters())
						{
							CPersistentRoot.saveRuntimeTypeParameters(obj,oInst.getTypeParameters());
						}
						else
						{
							debugMsg("rootObject("+ro.getContainerName()+") is not a parametrized class");
						}
						obj_array[i]=obj;
						i++;
					}
					psRoot.rootObjects.put(ro.getContainerName(),obj_array);
				}
				else
				{
					debugMsg("get the root object LOID="+ro.getLogicalObjectId());
					CInstanceMetaObject oInst=findObjectInstance(ro.getLogicalObjectId());
					debugMsg("get the root object OID="+oInst.getOID()+"   class="+oInst.getClassName()+"  version="+oInst.getClassVersion());
					Object dbObject=db.ext().getByID(oInst.getOID());
					db.activate(dbObject,1);
		
					CClassVersionMetaObject ocl=cl.findApplicationObjectClassVersion(oInst.getClassName());
					Object obj=ocl.readerAdapter(dbObject,null,cl,oInst,(CAttributeMetaObject)null);
					debugMsg("root object ("+ro.getContainerName()+") version="+ocl.getClassVersion()+"  obj="+obj+"  objKey="+System.identityHashCode(obj));
					psRoot.rootObjects.put(ro.getContainerName(),obj);
					saveOnCache(ocl,oInst,obj);//ro.getLogicalObjectId()
					
					if(oInst.hasTypeParameters())
					{
						CPersistentRoot.saveRuntimeTypeParameters(obj,oInst.getTypeParameters());
					}
					else
					{
						debugMsg("rootObject("+ro.getContainerName()+") is not a parametrized class");
					}
				}
			}
			debugMsg("RootObjects: "+((CPersistentRoot)thisJoinPoint.getTarget()).rootObjects.size());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * Loads field object from database and put on cache
	 * @param obj - parent object
	 * @param logicalObjectId - parent object id???
	 * @param fld - field name to load
	 */
	void around(Object obj,long logicalObjectId,String fld) : execution(void rhp.aof4oop.framework.core.CPersistentRoot.loadFieldObject(..)) && args(obj,logicalObjectId,fld)
	{
		Field f;
		CClassLoader cl;
		
		long tinig=System.currentTimeMillis();

		try 
		{
			cl=(CClassLoader)ClassLoader.getSystemClassLoader();
			debugMsg("loadObject: LOID{"+logicalObjectId+"} "+obj.getClass().getName()+"->"+fld);
			f=CPersistentRoot.getField(obj.getClass(),fld);
			if(CPersistentRoot.isFrameworkDataType(f.getName()))
			{
				// do nothing
				debugMsg("The field "+fld+" is a framework internal data type!!!");
			}
			else if(f.getType().isPrimitive())
			{
				// do nothing
				debugMsg("The field "+fld+" is a primitive data type");
			}
			else if(CPersistentRoot.isPrimitiveDataTypeObject(f.getType().getCanonicalName()))
			{
				//Do nothing
				debugMsg("The field "+fld+" is an object that is consired as primitive data type");
			}
			else if(f.getType().isArray())
			{
				debugMsg("The field "+fld+" is an object that is consired as an array");
				__getFieldArray(f,logicalObjectId,obj,cl);
			}
			else
			{
				debugMsg("The field "+fld+" is an object that is consired as normal object");
				__getFieldObject(f,fld,logicalObjectId,obj,cl);
			}	
		} 
		catch (NoSuchFieldException e)
		{
			// This is not an error!!!
			System.out.println("The field "+fld+"  does not exist in Logical Object "+obj.getClass().getCanonicalName()+" ("+logicalObjectId+")");
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
			throw e;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new EFrameworkFault(e);
		}
		CPersistentRoot.statsTimeStorageAspectGetLoadObject+=(System.currentTimeMillis()-tinig);
	}
	private void __getFieldArray(Field f,long logicalObjectId,Object obj,CClassLoader cl) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, NotFoundException, IOException, CannotCompileException, SecurityException, InstantiationException, NoSuchFieldException
	{
		CAttributeMetaObject oaLink;
//long t_findmo;		
//long t_classload=0;
//long t_dbload=0;
//long t_adapt=0;
//long t_wrcache=0;
//long t_ciclo;
//long t_ini,t_inii;
//boolean in_cache;

//t_inii=System.currentTimeMillis();	
		oaLink=findObjectLink(logicalObjectId, f.getName());
//t_findmo=(System.currentTimeMillis()-t_inii);		
		//debugMsg("Object array link:"+oaLink);
		if(oaLink!=null)
		{
			if(!oaLink.isTypeArray())
			{
				throw new IllegalAccessException("The pointed object "+oaLink.getObjectClassName()+"::LOID{"+oaLink.getParentObjectId()+"}."+oaLink.getMember()+" is not an array. Field expected name="+f.getName()+" type="+f.getType().getCanonicalName());
			}
			else
			{
				//System.out.println("The pointed object "+oaLink.getObjectClassName()+"::LOID{"+oaLink.getParentObjectId()+"}."+oaLink.getMember()+" is an array. Field expected name="+f.getName()+" type="+f.getType().getCanonicalName());
			}
			f.setAccessible(true);
			Object[] existingMemoryArrayObject=CPersistentRoot.findMappedArray(oaLink.getParentObjectId(),oaLink.getMember());
			if(existingMemoryArrayObject!=null)
			{
//				if("toConnections".equals(f.getName()) || "partOf".equals(f.getName()))
//				{
//					System.out.println("Loading field "+f.getName()+"   oaLink="+oaLink.getMember()+"   array="+oaLink.isTypeArray()+" size="+(oaLink.getArrayObjectId()!=null?""+oaLink.getArrayObjectId().length:"NULO"));
//					for(Object o:existingMemoryArrayObject)
//					{
//						System.out.println("     "+(o!=null?"OK("+o.getClass().getCanonicalName()+"): p="+(CPersistentRoot.isPersistent(o)+"  r="+CPersistentRoot.isReachable(o)):"NULO"));
//					}
//				}				
//				in_cache=true;
				//debugMsg("set field with cached array size="+existingMemoryArrayObject.length+" fdl="+f.getName()+"  is_array="+f.getType().isArray());
				f.set(obj,existingMemoryArrayObject);
			}
			else if(!f.getType().getComponentType().getName().equals(oaLink.getObjectClassName()))
			{
				System.out.println("Field "+f.getName()+" :: incompatible types "+f.getType().getComponentType().getName()+" <> "+oaLink.getObjectClassName());
			}
			else
			{
//				in_cache=false;
				
				CInstanceMetaObject imo=findObjectInstance(logicalObjectId);
				Object dbObjectParent=null;
				if(imo!=null)
				{
					dbObjectParent=db.ext().getByID(imo.getOID());
					db.activate(dbObjectParent,1);
				}
				//debugMsg("set field with an array. The array "+oaLink.getObjectClassName()+"::LOID{"+oaLink.getParentObjectId()+"}."+oaLink.getMember()+" must be loaded");
				//Reconstructs the memory array
				//The array must be from correct type
				Object[] memoryArray=(Object[])Array.newInstance(Class.forName(oaLink.getObjectClassName()), oaLink.getArrayObjectId().length);
				//Pre-loads all IMOs
				CInstanceMetaObject oInsts[]=findObjectInstance(oaLink.getArrayObjectId());
				for(int i=0;i<oaLink.getArrayObjectId().length;i++)
				{
					Object existingMemoryObject=CPersistentRoot.findCachedObject(oaLink.getArrayObjectId()[i]);
					if(existingMemoryObject!=null)
					{
//						debugMsg(oaLink.getMember()+"["+i+"] LOID{"+oaLink.getArrayObjectId()[i]+"} is already on cache");
						//Already in the cache
						memoryArray[i]=existingMemoryObject;
					}
					else
					{
//						debugMsg(oaLink.getMember()+"["+i+"] LOID{"+oaLink.getArrayObjectId()[i]+"} must be loaded");
						//Get from DB and put on cache
						//CInstanceMetaObject oInst=findObjectInstance(oaLink.getArrayObjectId()[i]);
						CInstanceMetaObject oInst=oInsts[i];
						//debugMsg("oInst="+oInst);
//t_ini=System.currentTimeMillis();						
						Object dbObject=db.ext().getByID(oInst.getOID());
						db.activate(dbObject,1);

//t_dbload+=(System.currentTimeMillis()-t_ini);						
						//debugMsg("dbObject="+dbObject);

//t_ini=System.currentTimeMillis();
						CClassVersionMetaObject ocl=cl.findApplicationObjectClassVersion(oInst.getClassName());
//t_classload+=(System.currentTimeMillis()-t_ini);						
						//debugMsg("ObjectClassVersion="+ocl);

//t_ini=System.currentTimeMillis();
						Object cellMemoryObject=ocl.readerAdapter(dbObject,dbObjectParent,cl,oInst,oaLink);
//t_adapt+=(System.currentTimeMillis()-t_ini);					

//t_ini=System.currentTimeMillis();
						//debugMsg("memoryObject="+memoryObject);
						saveOnCache(ocl,oInst, cellMemoryObject);
//t_wrcache+=(System.currentTimeMillis()-t_ini);

						if(oInst.hasTypeParameters())
						{
							//The loaded object has type parameters so they must be saved at run-time environment
							CPersistentRoot.saveRuntimeTypeParameters(cellMemoryObject, oInst.getTypeParameters());
						}
						memoryArray[i]=cellMemoryObject;
					}
				}
				CPersistentRoot.saveMapEntry(oaLink.getParentObjectId(),oaLink.getMember(), memoryArray);
				//debugMsg("set field with loaded array size="+memoryArray.length+" fdl="+f.getName()+"  is_array="+f.getType().isArray());
				f.set(obj,memoryArray);
			}
//t_ciclo=(System.currentTimeMillis()-t_inii);					
//System.out.println("Cached:"+in_cache+" ciclo:"+t_ciclo+"  t_findmo="+t_findmo+" t_dbload="+t_dbload+"  t_classload="+t_classload+"  t_adapt="+t_adapt+"  t_wrcache="+t_wrcache+" cache size="+CPersistentRoot.cacheSize()+" total array loading time="+CPersistentRoot.statsTimeGetLoadArray);			
		}
//		else
//		{
//			t_ciclo=(System.currentTimeMillis()-t_inii);
//			System.out.println("The pointed object through LOID{"+logicalObjectId+"}->"+f.getName()+" type="+f.getType().getCanonicalName()+" does not exists.");
//			System.out.println("Cached: n/a ciclo:"+t_ciclo+"  t_findmo="+t_findmo+" cache size="+CPersistentRoot.cacheSize()+" total array loading time="+CPersistentRoot.statsTimeGetLoadArray);
//		}
	}
//	/**
//	 * Para comparar arrays
//	 * @param existingMemoryArrayObject
//	 * @param memoryArray
//	 */
//	private static void test(Object[] existingMemoryArrayObject,Object[] memoryArray)
//	{
//		int errors=0;
//		
//			for(int i=0;i<existingMemoryArrayObject.length;i++)
//			{
//				if(!existingMemoryArrayObject[i].getClass().getCanonicalName().equals(memoryArray[i].getClass().getCanonicalName()))
//				{
//					System.out.println(existingMemoryArrayObject[i].getClass().getCanonicalName()+" != "+memoryArray[i].getClass().getCanonicalName());
//					errors++;
//				}
//				
//				if(existingMemoryArrayObject[i] instanceof BaseAssembly)
//				{
//					BaseAssembly ba1=(BaseAssembly)existingMemoryArrayObject[i];
//					BaseAssembly ba2=(BaseAssembly)memoryArray[i];
//					if(ba1.getId()!=ba2.getId())
//					{
//						System.out.println("ssssssssssssssssssssssssssssssssssss");
//					}
//					errors++;
//				}
//			}
//	}
	/**
	 * 
	 * @param f
	 * @param fld
	 * @param logicalObjectId - LOID of the object that has fld
	 * @param obj
	 * @param cl
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws NoSuchFieldException
	 */
	private void __getFieldObject(Field f,String fld,long logicalObjectId,Object obj,CClassLoader cl) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, NotFoundException, IOException, CannotCompileException, SecurityException, InstantiationException, NoSuchFieldException
	{
		CAttributeMetaObject oLink;
		
			
		oLink=findObjectLink(logicalObjectId, fld);
		//debugMsg("object link:"+oLink);
		if(oLink!=null)
		{
			f.setAccessible(true);
			
			Object existingMemoryObject=CPersistentRoot.findCachedObject(oLink.getObjectId());
			if(existingMemoryObject!=null)
			{
				//debugMsg("LOID{"+oLink.getObjectId()+"} is already on cache");
				//Already in the cache
				f.set(obj,existingMemoryObject);
			}
			else
			{
				//debugMsg("LOID{"+oLink.getObjectId()+"} must be loaded");
				//Get from DB and put on cache
				CInstanceMetaObject oInst=findObjectInstance(oLink.getObjectId());
				//debugMsg("oInst="+oInst);
				Object dbObject=db.ext().getByID(oInst.getOID());
				db.activate(dbObject,1);
				CInstanceMetaObject oInstParent=findObjectInstance(logicalObjectId);
				Object dbObjectParent=db.ext().getByID(oInstParent.getOID());
				db.activate(dbObjectParent,1);
				//debugMsg("dbObject="+dbObject);
				CClassVersionMetaObject ocl=cl.findApplicationObjectClassVersion(oInst.getClassName());
				//debugMsg("ObjectClassVersion="+ocl);
				Object memoryObject=ocl.readerAdapter(dbObject,dbObjectParent,cl,oInst,oLink);
				//debugMsg("memoryObject="+memoryObject);
				saveOnCache(ocl,oInst, memoryObject);
				if(oInst.hasTypeParameters())
				{
					//The loaded object has type parameters so they must be saved at run-time environment
					CPersistentRoot.saveRuntimeTypeParameters(memoryObject, oInst.getTypeParameters());
				}
				f.set(obj,memoryObject);
			}
		}
	}
	/**
	 * Sets an object member value.
	 * Note that if obj is already on cache, that points directly to obj. So, is not necessary to update the cache.
	 * @param obj - memory object
	 * @param fld - the field
	 * @param val - the value to be saved in the field
	 */
	void around(Object obj,String fld,Object val) : execution(void rhp.aof4oop.framework.core.CPersistentRoot.saveFieldObject(..)) && args(obj,fld,val)
	{
		Object oldVal=null;
		CClassLoader cl;
		
		long tinig=System.currentTimeMillis();
		try
		{
			cl=(CClassLoader)ClassLoader.getSystemClassLoader();
			CInstanceMetaObject oInst=findObjectInstance(CPersistentRoot.findCachedLogicalObjectID(obj));
			CClassVersionMetaObject cvmo=cl.findApplicationObjectClassVersion(obj.getClass().getCanonicalName());
			debugMsg("saveObject()::"+oInst.getClassName()+"."+fld);
			Field f=CPersistentRoot.getField(obj.getClass(), fld);
			if(cvmo.getClassVersion().equals(oInst.getClassVersion()))
			{
				if(f.getType().isPrimitive() || CPersistentRoot.isPrimitiveDataTypeObject(f.getType().getCanonicalName()))
				{
					oldVal=CPersistentRoot.setObjectField(f,obj,val); // Replaces the old value (the inboxing do all the work!!!)
					//debugMsg("I have stored an primitive type: "+obj.getClass().getName()+"."+fld+"="+val+" (old:"+oldVal+")");
					updateObject(obj,oInst,cl);
				}
				else if(f.getType().isArray())
				{
					__setFieldArray((CPersistentRoot)thisJoinPoint.getTarget(), f, oInst, fld, obj, oldVal, val);
				}
				else
				{
					__setFieldObject((CPersistentRoot)thisJoinPoint.getTarget(), f, oInst, fld, obj, oldVal, val);
				}
			}
			else
			{
				//The memory object has emulated, thus, it must be updated
				oldVal=CPersistentRoot.setObjectField(f,obj,val);
				updateObjectEmulated((CPersistentRoot)thisJoinPoint.getTarget(),obj,oInst,cl);
			}
		} 
		catch(NullPointerException e)
		{
			e.printStackTrace();
			throw new EFrameworkFault(e);
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
			throw new EFrameworkFault(e);
		}
		catch(NotFoundException e)
		{
			e.printStackTrace();
			throw new EFrameworkFault(e);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			throw new EFrameworkFault(e);
		}
		catch(CannotCompileException e)
		{
			e.printStackTrace();
			throw new EFrameworkFault(e);
		}
		catch(NoSuchFieldException e)
		{
			e.printStackTrace();
			throw new EFrameworkFault(e);
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
			throw new EFrameworkFault(e);
		} 
		catch (InstantiationException e) 
		{
			e.printStackTrace();
			throw new EFrameworkFault(e);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new EFrameworkFault(e);
			
		}
		debugMsg(obj+"."+fld+"="+oldVal);

		CPersistentRoot.statsTimeStorageAspectSetSaveObject+=(System.currentTimeMillis()-tinig);
	}
	void around(Object memoryObject) : execution(void rhp.aof4oop.framework.core.CPersistentRoot.saveObject(..)) && args(memoryObject)
	{
		long tinig=System.currentTimeMillis();
		try 
		{
			saveObject((CPersistentRoot)thisJoinPoint.getTarget(), memoryObject);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new EFrameworkFault(e);
		}
		CPersistentRoot.statsTimeStorageAspectSetSaveObject+=(System.currentTimeMillis()-tinig);
	}
	
	private void __setFieldObject(CPersistentRoot psRoot,Field f,CInstanceMetaObject oInst,String fld,Object obj,Object oldVal,Object val) throws IllegalAccessException,ClassNotFoundException, NotFoundException, IOException, CannotCompileException,InstantiationException,NoSuchFieldException
	{
		oldVal=CPersistentRoot.setObjectField(f,obj,val); // Replaces the old value

		debugMsg("I have stored an object: "+obj.getClass().getName()+"."+fld+"="+val+" primitive:"+f.getType().isPrimitive());
		
		CAttributeMetaObject oLink=findObjectLink(oInst.getId(), f.getName());
		if(val==null)
		{
			if(oLink!=null)
			{
				//Just removes the link.
				//The GC, removes the meta-objects and objects
				//The cache will detect if it is necessary to remove the entry to old value
				deleteLink(oLink);
				debugMsg("Old value:"+oldVal);
			}
			else
			{
				// Do nothing!!!
				debugMsg("Old value already was null");
			}
		}
		else
		{
			if(oLink!=null)
			{
				//Updates the link by updating the meta-object
				if(CPersistentRoot.isPersistent(val))
				{
					//Updates the link with the existing persistent object
					CInstanceMetaObject oInstVal=findObjectInstance(CPersistentRoot.findCachedLogicalObjectID(val));
					updateLink(oLink,oInstVal);
					debugMsg("Updates the link with the existing persistent object: "+oLink.getObjectClassName()+"."+oLink.getMember());
				}
				else
				{
					//Save the value and Updates the link
					CInstanceMetaObject oInstVal=saveObject(psRoot,val);
					updateLink(oLink,oInstVal);
					debugMsg("Save the value and Updates the link: "+oLink.getObjectClassName()+"."+oLink.getMember());
				}
				CPersistentRoot.saveMapEntry(oLink.getParentObjectId(),fld, val);
			}
			else
			{
				CAttributeMetaObject nLink;
				//Creates a new Object Link
				if(CPersistentRoot.isPersistent(val))
				{
					//Creates a link to an existing Object Instance
					CInstanceMetaObject oInstVal=findObjectInstance(val);
					nLink=recordLink(oInst, f.getName(), oInstVal);
					debugMsg("Creates a link to an existing Object Instance");
				}
				else
				{
					//Creates a new Object Instance and Link
					CInstanceMetaObject oInstVal=saveObject(psRoot,val);
					nLink=recordLink(oInst, f.getName(), oInstVal);		
					debugMsg("Creates a new Object Instance and Link");
				}
				CPersistentRoot.saveMapEntry(nLink.getParentObjectId(),fld, val);
			}
		}
	}
	/**
	 * 
	 * @param psRoot
	 * @param f
	 * @param oInst
	 * @param fld
	 * @param obj
	 * @param oldVal
	 * @param val
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 * @throws InstantiationException
	 * @throws NoSuchFieldException
	 */
	private void __setFieldArray(CPersistentRoot psRoot,Field f,CInstanceMetaObject oInst,String fld,Object obj,Object oldVal,Object val) throws IllegalAccessException,ClassNotFoundException, NotFoundException, IOException, CannotCompileException,InstantiationException,NoSuchFieldException
	{
		CClassVersionMetaObject fiedlClassVersion;

		oldVal=CPersistentRoot.setObjectField(f,obj,val); // Replaces the old value 
		//debugMsg("I have stored an array: "+obj.getClass().getName()+"."+fld+"="+val+" primitive:"+f.getType().isPrimitive());
	
		//Check what type of array
		fiedlClassVersion=findClassVersion(f.getType().getComponentType().getCanonicalName());
		//debugMsg("Now lets save an array of "+fiedlClassVersion.toVersionClassName());
		
		CAttributeMetaObject oLink=findObjectLink(oInst.getId(), f.getName());
		if(val==null)
		{
			if(oLink!=null)
			{
				//Just removes the link.
				//The GC, removes the meta-objects and objects
				//The cache will detect if it is necessary to remove the entry to old value
				deleteLink(oLink);
				//debugMsg("Old array:"+oldVal);
			}
			else
			{
				// Do nothing!!!
				//debugMsg("Old array already was null");
			}
		}
		else
		{
			CInstanceMetaObject[] oInstArrayVal=new CInstanceMetaObject[((Object[])val).length];
			//Check all cells who already is persistent. If not store.
			for (int i=0;i<oInstArrayVal.length;i++)
			{
				Object cell=((Object[])val)[i];
				CInstanceMetaObject oInstCell;
				if(CPersistentRoot.isPersistent(cell))
				{
					oInstCell=findObjectInstance(CPersistentRoot.findCachedLogicalObjectID(cell));
				}
				else
				{
					oInstCell=saveObject(psRoot,cell);
				}
				oInstArrayVal[i]=oInstCell;
			}
			if(oLink!=null)
			{
				//Updates the link by updating the metaobject
				updateLink(oLink,oInstArrayVal);
				//debugMsg("Updates the link with the existing persistent object array");
				CPersistentRoot.saveMapEntry(oLink.getParentObjectId(),fld, val);
			}
			else
			{
				//Creates a new Object Array Link
				CAttributeMetaObject nLink=recordLink(oInst, f.getName(),fiedlClassVersion,oInstArrayVal);
				//debugMsg("Creates a link to an existing Array Object Instance");
				CPersistentRoot.saveMapEntry(nLink.getParentObjectId(),fld, val);
			}
		}

	}
	void around(Object loadedMemoryObject,CInstanceMetaObject imo) : execution(void rhp.aof4oop.framework.core.CPersistentRoot.activateAllObjectAttributes(..)) && args(loadedMemoryObject,imo)
	{
		ArrayList<CAttributeMetaObject> amos=findObjectLinks(imo.getId());
		for(CAttributeMetaObject amo:amos)
		{
			try
			{
				//System.out.println("Loading IMO "+amo.getParentObjectId()+" -> "+amo.getMember()+" ...");
				CPersistentRoot.loadFieldObject(loadedMemoryObject,amo.getParentObjectId(),amo.getMember());
			}
			catch(Exception e)
			{
				throw new EFrameworkFault(e.getMessage());
			}
		}
	}	
	void around(Object dbObject,CInstanceMetaObject imo,int depth,ArrayList<String> excludeMembers) : execution(void rhp.aof4oop.framework.core.CPersistentRoot.activateAllDBObjectAttributes(..)) && args(dbObject,imo,depth,excludeMembers)
	{
		//TODO::034:
		//Currently the objects at database have the current app signature. 
		//However, they may have a different structure
		ArrayList<CAttributeMetaObject> amos=findObjectLinks(imo.getId());
 
		for(CAttributeMetaObject amo:amos)
		{
			try
			{
				//Loads the array and all its cells recursively
				if(excludeMembers!=null && excludeMembers.contains(amo.getMember()))
				{
					//System.out.println("["+depth+"] not load "+amo.getParentObjectId()+" -> "+amo.getMember()+"[] ...");
				}
				else if(amo.isTypeArray())
				{
					System.out.println("["+depth+"] Loading DB array of IMO "+amo.getParentObjectId()+" -> "+amo.getMember()+"[] ...");
					CClassLoader cl=(CClassLoader)ClassLoader.getSystemClassLoader();
					//TODO::034:at DB the fields are not renamed to its version
					//Class<?> clzz=cl.loadClass(amo.toObjectClassVersion());
					Class<?> clzz=cl.loadClass(amo.getObjectClassName());// Uses the current one
					if(clzz==null)
					{
						throw new EFrameworkFault("Class "+amo.toObjectClassVersion()+" not found");
					}
					Object[] arry=(Object[])Array.newInstance(clzz,amo.getArrayObjectId().length);
					for(int i=0;i<amo.getArrayObjectId().length;i++)
					{
						long loid=amo.getArrayObjectId()[i];
						if(loid>0)
						{
							CInstanceMetaObject att_imo=findObjectInstance(loid);
							Object cell=db.ext().getByID(att_imo.getOID());
							if(cell!=null)
							{
								db.activate(cell,1);
								if(depth>1)
								{
									CPersistentRoot.activateAllDBObjectAttributes(cell,att_imo,depth-1,excludeMembers);
								}
							}
							CClassVersionMetaObject cvmo=findClassVersion(att_imo);
							arry[i]=cvmo.readerAdapter(cell, null, cl,att_imo,null);
						}
					}
				
					Field f=CPersistentRoot.getField(dbObject.getClass(),amo.getMember());
					f.setAccessible(true);
					f.set(dbObject,arry);
				}
				else
				{
					System.out.println("["+depth+"] Loading DB Object of IMO "+amo.getParentObjectId()+" -> "+amo.getMember()+" ("+amo.getObjectClassName()+") ...");
					//Loads the object
					if(amo.getObjectId()>0)
					{
						CInstanceMetaObject att_imo=findObjectInstance(amo.getObjectId());
						Object o=db.ext().getByID(att_imo.getOID());
						if(o!=null)
						{
							db.activate(o,1);
							if(depth>1)
							{
								CPersistentRoot.activateAllDBObjectAttributes(0,att_imo,depth-1,(ArrayList<String>)null);
							}
						}
						Field f=CPersistentRoot.getField(dbObject.getClass(),amo.getMember());
						f.setAccessible(true);
						f.set(dbObject,o);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new EFrameworkFault(e);
			}
		}
	}
	/**
	 * Saves the object on the cache
	 * @param container
	 * @param object
	 */
	private void saveOnCache(CClassVersionMetaObject cvmo,CInstanceMetaObject imo,Object memoryObject)
	{
		long logicalObjectId=imo.getId();
		
		boolean emulated=!(cvmo.getClassVersion().equals(imo.getClassVersion()));
		CPersistentRoot.saveCacheEntry(logicalObjectId,memoryObject,emulated);
//		System.out.print("Object LOID="+imo.getId()+"  of class "+imo.getClassCanonicalName()+" Version "+imo.getClassVersion()+"  was emulated to version "+cvmo.getClassVersion()+"  ");
//		System.out.println("Persistent="+CPersistentRoot.isPersistent(memoryObject)+" Cached="+CPersistentRoot.isCached(memoryObject)+"  Emulated="+CPersistentRoot.isEmulated(memoryObject)+" LOID="+CPersistentRoot.findCachedLogicalObjectID(memoryObject)+" SISId="+System.identityHashCode(memoryObject));
	}
	/**
	 * Delete the object from chace
	 * @param memoryObject
	 */
	private void deleteFromCache(Object memoryObject)
	{
		CPersistentRoot.deleteCacheEntry(memoryObject);
	}
	public void loadFields(Object obj) throws IllegalArgumentException, IllegalAccessException
	{
		System.out.println("class's fields: "+obj.getClass().getName());
		
	    Field[] fdls=obj.getClass().getDeclaredFields();
	    for(int i=0;i<fdls.length;i++)
	    {
	    	fdls[i].setAccessible(true);
	    	System.out.println("Field["+i+"]::"+fdls[i].getName()+"="+fdls[i].get(obj));
	    	db.activate(fdls[i].get(obj),1);
	    }
	}
	/**
	 * Updates an existing object
	 * @param obj
	 * @param oInst
	 * @param cl
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	private CInstanceMetaObject updateObject(Object obj,CInstanceMetaObject oInst,CClassLoader cl) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException, SecurityException, InstantiationException, IllegalAccessException, NoSuchFieldException
	{
		CClassVersionMetaObject ocv=cl.findApplicationObjectClassVersion(oInst.getClassName());
		Object dbObject=ocv.writerAdapter(obj,cl.findObjectReferenceClassVersion(obj));//Lazy conversion
		db.store(dbObject);
		updateObjectInstance(oInst,getOID(dbObject));
		
		return oInst;
	}
	/**
	 * Updates an existing Emulated object
	 * @param container
	 * @param memoryObject
	 * @param imo
	 * @param cl
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	private CInstanceMetaObject updateObjectEmulated(CPersistentRoot container,Object memoryObject,CInstanceMetaObject imo,CClassLoader cl) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException 
	{
		Object dbObject;
		//CClassLoader cl;
		
		if(memoryObject==null)
		{
			return null;
		}
		if(memoryObject.getClass().isArray())
		{
			System.out.println("ALERT: An Array here!!!");
		}
		if(memoryObject.getClass().isPrimitive())
		{
			return null;
		}
		if(CPersistentRoot.isPrimitiveDataTypeObject(memoryObject.getClass().getName()))
		{
			return null;
		}

		if(!CPersistentRoot.isPersistent(memoryObject))
		{
			throw new EFrameworkFault("The object must exist");
		}
		else
		{
			//cl=(CClassLoader)ClassLoader.getSystemClassLoader();
			//Save the new object
			CClassVersionMetaObject ocv=cl.findObjectReferenceClassVersion(memoryObject);
			dbObject=ocv.writerAdapter(memoryObject,cl.findObjectReferenceClassVersion(memoryObject));
			//TODO: String[] typeParametes=CPersistentRoot.findRuntimeTypeParameters(memoryObject);
			db.store(dbObject);
			long persistentObjectId=getOID(dbObject);
			updateObjectInstance(imo,persistentObjectId);

//			deleteFromCache(memoryObject);
//System.out.println("YYYYY "+CPersistentRoot.isCached(memoryObject));			
//			saveOnCache(imo.getId(),memoryObject);
//System.out.println("ZZZZZ "+CPersistentRoot.isCached(memoryObject));			
			__saveTree(container, memoryObject,imo);
			
			return imo;
		}
	}
	/**
	 * Saves the object and then, recursively, all their properties
	 * @param object
	 * @throws CannotCompileException 
	 * @throws IOException 
	 * @throws NotFoundException 
	 * @throws ClassNotFoundException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 */
	private CInstanceMetaObject saveObject(CPersistentRoot container,Object memoryObject) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException 
	{
		Object dbObject;
		CClassLoader cl;
		long persistentObjectId;

		
		if(memoryObject==null)
		{
			return null;
		}
		if(memoryObject.getClass().isArray())
		{
			throw new EFrameworkFault("An array was not expected!!!");
		}
		if(memoryObject.getClass().isPrimitive())
		{
			return null;
		}
		if(CPersistentRoot.isPrimitiveDataTypeObject(memoryObject.getClass().getName()))
		{
			return null;
		}

		if(CPersistentRoot.isPersistent(memoryObject))
		{
			//If the object is already persistent returns its CInstanceMetaObject to be linked 
			return findObjectInstance(memoryObject);
		}
		else
		{
			cl=(CClassLoader)ClassLoader.getSystemClassLoader();
			//Save the new object
			CClassVersionMetaObject ocv=cl.findObjectReferenceClassVersion(memoryObject);
			dbObject=ocv.writerAdapter(memoryObject,cl.findObjectReferenceClassVersion(memoryObject));
			String[] typeParametes=CPersistentRoot.findRuntimeTypeParameters(memoryObject);

			db.store(dbObject);
			persistentObjectId=getOID(dbObject);
			CInstanceMetaObject oInst=recordObjectInstance(ocv,typeParametes,persistentObjectId);
			saveOnCache(ocv,oInst,memoryObject);
			
			__saveTree(container, memoryObject,oInst);
			
			return oInst;
		}
	}
	/**
	 * Saves the array and returns an array of IMO
	 * @param psRoot
	 * @param memoryObject
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 */
	private CInstanceMetaObject[] saveArray(CPersistentRoot psRoot,Object memoryObject) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException, NotFoundException, IOException, CannotCompileException
	{
		if(!memoryObject.getClass().isArray())
		{
			throw new EFrameworkFault("An array was expected!!!");
		}
		Object[] memoryArray=(Object[])memoryObject;
		CInstanceMetaObject[] oInstArrayVal=new CInstanceMetaObject[memoryArray.length];
		//Check all cells who already is persistent. If not store.
		for (int i=0;i<oInstArrayVal.length;i++)
		{
			Object cell=memoryArray[i];
			CInstanceMetaObject oInstCell;
			if(CPersistentRoot.isPersistent(cell))
			{
				oInstCell=findObjectInstance(CPersistentRoot.findCachedLogicalObjectID(cell));
				//TODO::010:
			}
			else
			{
				oInstCell=saveObject(psRoot,cell);
			}
			oInstArrayVal[i]=oInstCell;
		}
		return oInstArrayVal;
	}
	/**
	 * This is a convenience method to recursively save all the tree 
	 * 
	 * @param container
	 * @param alreadySavedMemoryObject
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	private void __saveTree(CPersistentRoot container,Object alreadySavedMemoryObject,CInstanceMetaObject objectInstance) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException, InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException 
	{
		CClassLoader cl=(CClassLoader)ClassLoader.getSystemClassLoader();
		
		//Save all object fields
		Field[] flds=CPersistentRoot.reflectFields(alreadySavedMemoryObject.getClass());
//		for(Field f:flds)
//		{
//			System.out.println("    "+f.getName());
//		}

		if(flds!=null && flds.length>0)
		{
			for(int i=0;i<flds.length;i++)
			{
				if(flds[i].getName()!=null && !flds[i].getName().startsWith("ajc$"))
				{
					//System.out.println("__saveTree("+i+"/"+flds.length+"):"+alreadySavedMemoryObject.getClass().getCanonicalName()+"."+flds[i].getName());
					flds[i].setAccessible(true);
					Object val=flds[i].get(alreadySavedMemoryObject);
					if(val!=null)
					{
						Class<?> ft=flds[i].getType();
						if(CPersistentRoot.isCached(val))
						{
							//debugMsg(flds[i].getName()+" Already persistente: "+val.getClass().getCanonicalName());
							CInstanceMetaObject oInstVal=findObjectInstance(CPersistentRoot.findCachedLogicalObjectID(val));
							recordLink(objectInstance,flds[i].getName(),oInstVal);
						}
						else if(ft.isPrimitive() || CPersistentRoot.isPrimitiveDataTypeObject(ft.getCanonicalName()))
						{
							//debugMsg("Primitive: "+val.getClass().getCanonicalName());
						}
						else if(ft.isArray())
						{
							//Check what type of array
							if(CPersistentRoot.isPrimitiveDataTypeObject(ft.getComponentType().getCanonicalName()))
							{
								//TODO: test
								//debugMsg("Array of primitives "+ft.getCanonicalName());
								//System.out.println("Array of primitives "+ft.getCanonicalName());	
							}
							else if("java.lang.Object".equals(ft.getComponentType().getCanonicalName()))
							{
								//TODO: test
								//System.out.println("Array Of Objects "+ft.getCanonicalName());
								//debugMsg("Array Of Objects "+ft.getCanonicalName());
							}
							else
							{
								CClassVersionMetaObject fiedlClassVersion=findClassVersion(flds[i].getType().getComponentType().getCanonicalName());
								//debugMsg("Now lets save an array \""+flds[i].getName()+"\" of "+fiedlClassVersion.toVersionClassName());
								CInstanceMetaObject[] oInstArrayVal=new CInstanceMetaObject[((Object[])val).length];
								//Check all cells who already is persistent. If not store.
								for (int a=0;a<oInstArrayVal.length;a++)
								{
									Object cell=((Object[])val)[a];
									CInstanceMetaObject oInstCell;
									long cell_loid=CPersistentRoot.findCachedLogicalObjectID(cell);
									if(cell_loid>0)// If there is a loid, then, the object it is persistent
									{
										//debugMsg("Already persistent "+flds[i].getName()+"["+a+"]");
										oInstCell=findObjectInstance(cell_loid);
									}
									else
									{
										//debugMsg("Start save "+flds[i].getName()+"["+a+"]");
										oInstCell=saveObject(container,cell);
									}
									oInstArrayVal[a]=oInstCell;
								}
								//Creates a new Object Array Link
								recordLink(objectInstance,flds[i].getName(),fiedlClassVersion,oInstArrayVal);
								//debugMsg("Creates a link to a new Array Object Instance");
								//TODO: Recursively should save all cell trees
							}
						}
						else
						{
							Object dbVal;
							//debugMsg("Save (Class:"+val.getClass().getName()+" primitive="+CPersistentRoot.isPrimitiveDataTypeObject(val.getClass().getName())+"):("+ft.getCanonicalName()+") "+flds[i].getName()+"="+val);
							CClassVersionMetaObject ocvf=cl.findObjectReferenceClassVersion(val);
							dbVal=ocvf.writerAdapter(val,cl.findObjectReferenceClassVersion(val));
							db.store(dbVal);

							CClassLoader.printClassDetails(dbVal.getClass());

							String[] typeParametes=CPersistentRoot.findRuntimeTypeParameters(val);

							CInstanceMetaObject oInstVal=recordObjectInstance(ocvf,typeParametes,getOID(dbVal));

							saveOnCache(ocvf,oInstVal,val);
							recordLink(objectInstance,flds[i].getName(),oInstVal);// val already is persistent

							__saveTree(container,val,oInstVal);
						}
					}
				}
			}
		}
	}


	/**
	 * Deletes an object and it reachable tree of objects
	 * @param container
	 * @param memoryObject
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 */
//	private void deleteObjectTree(CPersistentRoot container,Object memoryObject) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException 
//	{
//		long logicalObjectId;
//		logicalObjectId=CPersistentRoot.findCachedLogicalObjectID(memoryObject);
//
//		System.out.println("Remove tree LOID{"+logicalObjectId+"}::"+memoryObject.getClass());
//		//Deletes the Object Links Tree. CG do the rest.
//		deleteLinkTree(container,logicalObjectId);
//	}

	/**
	 * Delete the Object Link Tree
	 * @param container
	 * @param dbObjectId
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 */
//	private void deleteLinkTree(CPersistentRoot container,long logicalObjectId) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException
//	{
//		List<CAttributeMetaObject> oLinks;
//		
//		
//		oLinks=db.ext().query(CAttributeMetaObject.class);
//		for(CAttributeMetaObject oLink:oLinks)
//		{
//			//Delete the sub-tree
//			if(oLink.getParentObjectId()==logicalObjectId)
//			{
//				System.out.println("   * LOID{"+oLink.getParentObjectId()+"}."+oLink.getMember());
//				deleteLinkTree(container, oLink.getObjectId());
//				deleteLink(oLink);
//			}
//		}
//		//If don have any link and already in the cache remove from it
//		//CPersistentRoot.deleteCacheEntry(logicalObjectId);
//		//the GC  removes the object instance //db.ext().delete(dbObj)
//	}
	/**
	 * Loads an object and puts on cache
	 * @param oInst - Object to be loaded
	 * @param parentLink - The parent's attribute that points to oInst
	 * @throws CannotCompileException 
	 * @throws IOException 
	 * @throws NotFoundException 
	 * @throws ClassNotFoundException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 */
	private Object loadObject(CInstanceMetaObject oInst,CAttributeMetaObject parentLink) throws SecurityException, InstantiationException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		CClassLoader cl=(CClassLoader)ClassLoader.getSystemClassLoader();
		Object dbObject=db.ext().getByID(oInst.getOID());
		db.activate(dbObject,1);
		Object dbObjectParent=null;
		if(parentLink!=null)
		{
			dbObjectParent=db.ext().getByID(parentLink.getParentObjectId());
			db.activate(dbObjectParent,1);
		}
		
		CClassVersionMetaObject ocl=cl.findApplicationObjectClassVersion(oInst.getClassName());
		Object obj=ocl.readerAdapter(dbObject,dbObjectParent,cl,oInst,parentLink);
		//debugMsg("obj="+obj);
		saveOnCache(ocl,oInst,obj);
		
		return obj;
	}
	/**
	 * Record the link between two objects
	 * @param parent
	 * @param member
	 * @param object
	 * @throws CannotCompileException 
	 * @throws IOException 
	 * @throws NotFoundException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private CAttributeMetaObject recordLink(CInstanceMetaObject parent,String member,CInstanceMetaObject object) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException, InstantiationException, IllegalAccessException
	{
		if(object==null)
		{
			throw new IllegalArgumentException("The link to a null object is not possible");
		}
		CClassLoader cl=(CClassLoader)ClassLoader.getSystemClassLoader();
		CClassVersionMetaObject mo=cl.findObjectInstanceClassVersion(object);
		CAttributeMetaObject oLink=new CAttributeMetaObject(parent.getId(),member,object.getId(),object.getClassName(),object.getClassVersion());
		db.store(oLink);
		CPersistentRoot.saveAMOCacheEntry(oLink);
		debugMsg("Store Link to: "+oLink.getObjectId()+"  MetaObject: "+mo.getClassCanonicalName()+"$"+mo.getClassVersion());
		return oLink;
	}

	/**
	 * Record a link from one array object member and his objects
	 * @param parent - Parent Object
	 * @param member - Member parent object
	 * @param array - array class - The array be zero length
	 * @param objects - array objects 
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private CAttributeMetaObject recordLink(CInstanceMetaObject parent,String member,CClassVersionMetaObject array,CInstanceMetaObject[] objects) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException, InstantiationException, IllegalAccessException
	{
		if(array==null || objects==null)
		{
			throw new IllegalArgumentException("The link to a null object is not possible");
		}
		long[] loids=new long[objects.length];
		for(int i=0;i<objects.length;i++)
		{
			loids[i]=objects[i].getId();
		}
		//CClassLoader cl=(CClassLoader)ClassLoader.getSystemClassLoader();
		CAttributeMetaObject oLink=new CAttributeMetaObject(parent.getId(),member,array.getClassCanonicalName(),array.getClassVersion(),loids);
		db.store(oLink);
		CPersistentRoot.saveAMOCacheEntry(oLink);
		debugMsg("Store Link to array: "+oLink.getObjectId()+"  MetaObject: "+array.getClassCanonicalName()+"$"+array.getClassVersion());
		return oLink;
	}
	
	/**
	 * Update a link between two objects
	 * @param oLink - Existing Link object on DB
	 * @param oInstObject
	 */
	private void updateLink(CAttributeMetaObject oLink,CInstanceMetaObject linkedObject)
	{
		if(getOID(oLink)<=0)
		{
			throw new IllegalArgumentException();
		}
		oLink.setObjectId(linkedObject.getId());
		oLink.setArrayObjectId(null);
		db.store(oLink);
		CPersistentRoot.saveAMOCacheEntry(oLink);
	}
	/**
	 * Update a link from one array object member and its objects
	 * @param oLink
	 * @param linkedArray
	 * @param objects
	 */
	private void updateLink(CAttributeMetaObject oLink,CInstanceMetaObject[] objects)
	{
		if(getOID(oLink)<=0)
		{
			throw new IllegalArgumentException();
		}
		oLink.setObjectId(0);
		long[] loids=new long[objects.length];
		for(int i=0;i<objects.length;i++)
		{
			loids[i]=objects[i].getId();
		}
		oLink.setArrayObjectId(loids);
		db.store(oLink);
		CPersistentRoot.saveAMOCacheEntry(oLink);
	}
	private void deleteLink(CAttributeMetaObject objectLink)
	{
		List<CAttributeMetaObject> result;
		result=db.queryByExample(CAttributeMetaObject.class);
		for(CAttributeMetaObject oLink:result)
		{
			if(oLink.getParentObjectId()==objectLink.getParentObjectId() && oLink.getMember().equals(objectLink.getMember()))
			{
				db.delete(oLink);
				CPersistentRoot.deleteAMOCacheEntry(oLink);
				debugMsg("Deleted the Link to: "+oLink.getObjectId());
			}
		}
	}	
	private void recordRootObject(String rtName,Object memoryObject) throws DatabaseClosedException, DatabaseReadOnlyException, ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		CClassVersionMetaObject objectClassVersion=findClassVersion(memoryObject.getClass().getCanonicalName());
		
		db.store(new CRootMetaObject(rtName,objectClassVersion.getClassCanonicalName(),objectClassVersion.getClassVersion(),CPersistentRoot.findCachedLogicalObjectID(memoryObject)));
	}
	private void recordRootArray(String rtName,Object[] memoryObject) throws DatabaseClosedException, DatabaseReadOnlyException, ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		CClassVersionMetaObject arrayClassVersion=findClassVersion(memoryObject.getClass().getComponentType().getCanonicalName());
		
		db.store(new CRootMetaObject(rtName,arrayClassVersion.getClassCanonicalName(),arrayClassVersion.getClassVersion(),CPersistentRoot.findCachedLogicalArrayID(memoryObject)));
	}
	/**
	 * Creates a new instance meta-object
	 * @param ocv
	 * @param dbOID
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 */
	private CInstanceMetaObject recordObjectInstance(CClassVersionMetaObject ocv,String[] typeParameters,long dbOID) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		CInstanceMetaObject oInst=CInstanceMetaObject.factory(ocv,typeParameters,dbOID);
		db.store(oInst);
		CPersistentRoot.saveIMOCacheEntry(oInst);
		return oInst;
	}
	/**
	 * Updates the new physical Object Id of the data versioned object in their meta-object
	 * @param oInst - Instance Meta-Object 
	 * @param dbOID - OID of the versioned object
	 * @throws CannotCompileException 
	 * @throws IOException 
	 * @throws NotFoundException 
	 * @throws ClassNotFoundException 
	 */
	private void updateObjectInstance(CInstanceMetaObject oInst,long dbOID) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		CClassLoader cl;
		if(getOID(oInst)<=0)
		{
			throw new IllegalArgumentException();
		}
		cl=(CClassLoader)ClassLoader.getSystemClassLoader();
		CClassVersionMetaObject  appclassversion=cl.findApplicationObjectClassVersion(oInst.getClassName());
		oInst.setOID(dbOID);
		oInst.setClassVersion(appclassversion.getClassVersion());
		db.store(oInst);
		CPersistentRoot.saveIMOCacheEntry(oInst);
	}

	/**
	 * Find a link to an object from it parent and member
	 * @param parent
	 * @param member
	 * @return
	 */
	private CAttributeMetaObject findObjectLink(long parentId,String member)
	{
		CAttributeMetaObject mo=CPersistentRoot.findCachedMetaObjectLink(parentId, member);
		if(mo!=null)
		{
			//System.out.println("T="+((double)System.currentTimeMillis()-tini)/1000+" Cache!!!");
			return mo;
		}
//		//Loading one to one (low performance)
//		PredicateObjectLink<CAttributeMetaObject> p=new PredicateObjectLink<CAttributeMetaObject>(parentId, member) 
//		{
//			private static final long serialVersionUID = 1L;
//
//			public boolean match(CAttributeMetaObject olink)
//			{
//				return (olink.getParentObjectId()==getParentLoid() && olink.getMember()!=null && getMember()!=null && olink.getMember().equals(getMember()));
//			}
//		};
//		List<CAttributeMetaObject> links=db.query(p);
//		System.out.println("LOID("+parentId+")->"+member+"="+links.size());
//		for(CAttributeMetaObject oLink:links)
//		{
//			CPersistentRoot.saveCacheEntry(oLink);//Save all loaded mo
//			if(oLink.getParentObjectId()==parentId && oLink.getMember().equals(member))
//			{
//				CPersistentRoot.saveCacheEntry(oLink);
//				return oLink;
//			}
//		}
//		return null;
			
//		//Loading all MO's not cached (low performance)
		//TODO: implement same mark that allow the detection of outdated cache 
		if(CPersistentRoot.cacheObjectLinkSize()==0)
		{
			List<CAttributeMetaObject> links=db.query(new PredicateObjectLinkCached(CPersistentRoot.getCacheObjectLink()));
			System.out.println("Loaded CAttributeMetaObject="+links.size());
			for(CAttributeMetaObject oLink:links)
			{
				CPersistentRoot.saveAMOCacheEntry(oLink);//Save all loaded mo
			}
			return CPersistentRoot.findCachedMetaObjectLink(parentId, member);// Try again
		}
		else
		{
			return null;
		}
		
		
		//Load all db!!! (load unecessary objects)
//		if(CPersistentRoot.cacheObjectLinkSize()==0)
//		{
//			List<CAttributeMetaObject> links=db.query(CAttributeMetaObject.class);
//			for(CAttributeMetaObject oLink:links)
//			{
//				CPersistentRoot.saveCacheEntry(oLink);
//			}
//			return CPersistentRoot.findCachedMetaObjectLink(parentId, member);
//		}
//		else
//		{
//			return null;
//		}
	}
	/**
	 * Load a returns all AMO belonging to a object
	 * @param parentId
	 * @return
	 */
	private ArrayList<CAttributeMetaObject> findObjectLinks(long parentId)
	{
		//TODO: implement same mark that allow the detection of outdated cache 
		if(CPersistentRoot.cacheObjectLinkSize()==0)
		{
			loadCacheAMO();
		}
		return CPersistentRoot.findCachedMetaObjectLinks(parentId);
	}
	private CInstanceMetaObject findObjectInstance(Object alreadySavedMemoryObject)
	{
		return findObjectInstance(CPersistentRoot.findCachedLogicalObjectID(alreadySavedMemoryObject));
	}
	public abstract class PredicateObjectInstance<T> extends Predicate<T>
	{
		private static final long serialVersionUID = 1L;
		public long logicalObjectId;
		public PredicateObjectInstance(long logicalObjectId)
		{
			super();
			this.logicalObjectId=logicalObjectId;
		}
	}
	/**
	 * Gets the IMO by its logical id
	 * @param logicalObjectId
	 * @return
	 */
	private CInstanceMetaObject findObjectInstance(long logicalObjectId)
	{
		//double tini=System.currentTimeMillis();
		CInstanceMetaObject mo=CPersistentRoot.findCachedMetaObjectInstance(logicalObjectId);
		if(mo!=null)
		{
			//System.out.println("T="+((double)System.currentTimeMillis()-tini)/1000+" Cache!!!");
			return mo;
		}
		if(CPersistentRoot.cacheInstanceObjectSize()==0)
		{
			//This technique have a better performance to arrays!!!
			loadCacheIMO();
			return CPersistentRoot.findCachedMetaObjectInstance(logicalObjectId);
		}
		else
		{
			return null;
		}
	}
	private void loadCacheIMO()
	{
		List<CInstanceMetaObject> oInsts=db.query(CInstanceMetaObject.class);
		System.out.println("Loaded IMO MetaObjects="+oInsts.size());
		for(CInstanceMetaObject oInst:oInsts)
		{
			CPersistentRoot.saveIMOCacheEntry(oInst);
		}
	}
	private void loadCacheAMO()
	{
		List<CAttributeMetaObject> links=db.query(new PredicateObjectLinkCached(CPersistentRoot.getCacheObjectLink()));
		System.out.println("Loaded AMO MetaObjects="+links.size());
		for(CAttributeMetaObject amo:links)
		{
			CPersistentRoot.saveAMOCacheEntry(amo);//Save all loaded mo
		}
	}
	private CInstanceMetaObject[] findObjectInstance(long[] logicalObjectIds)
	{
		//double tini=System.currentTimeMillis();
		Hashtable<Long,CInstanceMetaObject> oInsts=null;
		
		CInstanceMetaObject[] mos=new CInstanceMetaObject[logicalObjectIds.length];
		for(int i=0;i<logicalObjectIds.length;i++)
		{
			CInstanceMetaObject mo=CPersistentRoot.findCachedMetaObjectInstance(logicalObjectIds[i]);
			if(mo!=null)
			{
				//System.out.println("T="+((double)System.currentTimeMillis()-tini)/1000+" Cache!!!");
				mos[i]=mo;
			}
			else
			{
				if(oInsts==null)
				{
					List<CInstanceMetaObject> tmp=db.query(CInstanceMetaObject.class);
					oInsts=new Hashtable<Long,CInstanceMetaObject>();
					for(CInstanceMetaObject oInst:tmp)
					{
						oInsts.put(oInst.getId(),oInst);
					}
				}
				mo=oInsts.get(logicalObjectIds[i]);
				CPersistentRoot.saveIMOCacheEntry(mo);
				//System.out.println("T="+((double)System.currentTimeMillis()-tini)/1000+" objs="+oInsts.size());
				mos[i]=mo;
				oInsts.remove(mo);
			}
		}
		//System.out.println("T="+((double)System.currentTimeMillis()-tini)/1000);
		return mos;
	}
	private CClassVersionMetaObject findClassVersion(CInstanceMetaObject oInst) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException, InstantiationException, IllegalAccessException
	{
		CClassLoader cl=(CClassLoader)ClassLoader.getSystemClassLoader();
		return cl.findObjectInstanceClassVersion(oInst);
	}
	private CClassVersionMetaObject findClassVersion(String appClassName) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		CClassLoader cl=(CClassLoader)ClassLoader.getSystemClassLoader();
		return cl.findApplicationObjectClassVersion(appClassName);
	}
	/**
	 * Gets the OID
	 * @param dbObject
	 * @return
	 */
	private long getOID(Object dbObject)
	{
		return db.ext().getID(dbObject);
	}
	long around(Object memoryObject) : execution(long rhp.aof4oop.framework.core.CPersistentRoot.getOID(..)) && args(memoryObject)
	{
		long loid=CPersistentRoot.findCachedLogicalObjectID(memoryObject);
		CInstanceMetaObject oInst=findObjectInstance(loid);
		return oInst.getOID();
	}
	void around(Object obj,int depth) : execution(void rhp.aof4oop.framework.core.CPersistentRoot.activate(..)) && args(obj,depth)
	{
		db.activate(obj,depth);
	}
	/**
	 * GC
	 */
	void around(int verbose) : execution(void rhp.aof4oop.framework.core.CPersistentRoot.gc(..))  && args(verbose)
	{
		List<CAttributeMetaObject> oLinks;
		List<CRootMetaObject> oRoots;
		List<CInstanceMetaObject> mobjects;
		List<Object> objects;
		int nObj=0,nMObj=0,nLink=0;
		boolean ok;

		oLinks=db.ext().query(CAttributeMetaObject.class);
		oRoots=db.ext().query(CRootMetaObject.class);
		mobjects=db.ext().query(CInstanceMetaObject.class);

		//Removes all broken links.
		for(CAttributeMetaObject oLink:oLinks)
		{
			ok=false;
			for(CInstanceMetaObject oInst:mobjects)
			{
				if(oInst.getId()==oLink.getParentObjectId())
				{
					ok=true;
					break;
				}
			}
			if(!ok)
			{
				if(verbose>1)
				{
					System.out.println("CG::Free Broken Link::"+oLink);
				}
				db.delete(oLink);
				nLink++;
			}
		}
		//Removes all MetaObjects that have any Link or Root refering to him
		for(CInstanceMetaObject oInst:mobjects)
		{
			ok=false;
			for(CAttributeMetaObject oLink:oLinks)
			{
				if(oLink.isTypeArray())
				{
					for(long cellLoid: oLink.getArrayObjectId())
					{
						if(cellLoid==oInst.getId())
						{
							ok=true;
							if(verbose>1)
							{
								System.out.println("GC::PRESERVE CInstanceMetaObject Meta Object:LOID{"+oInst.getId()+"}::Link="+oLink);
							}
							break;
						}
					}
				}
				else
				{
					if(oLink.getObjectId()==oInst.getId())
					{
						ok=true;
						if(verbose>1)
						{
							System.out.println("GC::PRESERVE CInstanceMetaObject Meta Object:LOID{"+oInst.getId()+"}::Link="+oLink);
						}
						break;
					}
				}
			}
			for(CRootMetaObject oRoot:oRoots)
			{
				if(oRoot.getLogicalObjectId()==oInst.getId())
				{
					ok=true;
					if(verbose>1)
					{
						System.out.println("GC::PRESERVE CRootMetaObject Meta Object:LOID{"+oInst.getId()+"}::Root="+oRoot);
					}
					break;
				}
			}
			if(!ok)
			{
				if(verbose>1)
				{
					System.out.println("GC::FREE:LOID{"+oInst+"}::"+oInst);
				}
				db.delete(oInst);
				nMObj++;
			}
		}

		
		//Removes all data objects that have any MetaObject (CInstanceMetaObject) refer to him
		mobjects=db.ext().query(CInstanceMetaObject.class);//   Refresh the objectInstance and load data objects
		objects=db.ext().query(Object.class);
		for(Object obj:objects)
		{
			if(obj instanceof CAttributeMetaObject || obj instanceof CRootMetaObject || obj instanceof CClassVersionMetaObject || obj instanceof CInstanceMetaObject || obj instanceof CView || obj instanceof IQuery)
			{
				//Is meta object
				debugMsg("GC::MetaObject::"+obj);
			}
			else
			{
				long oid=getOID(obj);
				ok=false;
				for(CInstanceMetaObject oInst:mobjects)
				{
					if(oInst.getOID()==oid)
					{
						ok=true;
//						if(verbose)
//						{
//							System.out.println("CG::PRESERVE:OID{"+oid+"}::"+obj+"  LOID="+oInst.getId());
//						}
						break;
					}
				}
				if(!ok)
				{
					if(verbose>1)
					{
						System.out.println("GC::FREE:OID{"+oid+"}::"+obj);
					}
					db.delete(obj);
					nObj++;
				}
			}
		}
		if(verbose>0)
		{
			System.out.println("GC removes "+nLink+" broken links");
			System.out.println("GC removes "+nMObj+" metaobjects");
			System.out.println("GC removes "+nObj+" objects");
		}
	}
	void around(boolean verbose) : execution(void rhp.aof4oop.framework.core.CPersistentRoot.showAllDB(..)) && args(verbose)
	{
		showAllDB(verbose);
	}
	private void showAllDB(boolean verbose)
	{
		System.out.println("*-------------------------------------------------*");
		System.out.println("|                  S h o w  DB                    |");
		System.out.println("*-------------------------------------------------*");
		TreeMap<String,Integer>  imo_totals;
		
		System.out.println("OID     Class                             Object");
		int n;
		n=0;
		try
		{
			CInstanceAdaptationMetadata md=CInstanceAdaptationMetadata.loadSchemaEvolution(verbose);
			for(CUpdateBackdateMetaObject mo:md.getUbmo())
			{
				if(verbose || true)
				{
					System.out.println("-- \tUBMO\t aplyDefault: "+mo.isApplyDefault()+"\t outputClassName: "+mo.getOutputClassName()+" Code: "+(mo.getConversionSourceCode()!=null?""+mo.getConversionSourceCode().length()+" chrs":"NULL")+" \t"+mo.getName());
				}
				n++;
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
		System.out.println("Total of UBMO: "+n);
		List <Object> objects = db.query(Object.class);
		n=0;
		for(Object o:objects)
		{
			if(o instanceof CClassVersionMetaObject)
			{
				if(verbose)
				{
					System.out.println(db.ext().getID(o)+"\tClass Version \t"+o);
				}
				n++;
			}
		}	
		System.out.println("Total of CVMO: "+n);

		n=0;
		for(Object o:objects)
		{
			if(o instanceof CRootMetaObject)
			{
				if(verbose)
				{
					CRootMetaObject ro=(CRootMetaObject)o;
					System.out.println(db.ext().getID(o)+"\tRoot Object   \t"+(ro.isArray()?" Array of "+ro.getObjectClassName()+" length "+ro.getLogicalArrayId().length:" "+ro.getObjectClassName())+"  class version "+ro.getObjectClassVersion());
				}
				n++;
			}
		}
		System.out.println("Total of RMO: "+n);
		n=0;
		for(Object o:objects)
		{
			if(o instanceof CAttributeMetaObject)
			{
				if(verbose)
				{
					System.out.println(db.ext().getID(o)+"\tLink Object   \t"+o);
				}
				n++;
			}
		}
		System.out.println("Total of AMO: "+n);
		n=0;
		imo_totals=new TreeMap<String,Integer>();
		for(Object o:objects)
		{
			if(o instanceof CInstanceMetaObject)
			{
				if(verbose)
				{
					System.out.println(db.ext().getID(o)+"\tInstance Object\t"+o);
				}
				n++;
				CInstanceMetaObject imo=(CInstanceMetaObject)o;
				String key=CClassVersionMetaObject.toVersionClassName(imo.getClassCanonicalName(),imo.getClassVersion());
				Integer tmp=imo_totals.get(key);
				if(tmp==null)
				{
					imo_totals.put(key,new Integer(1));
				}
				else
				{
					imo_totals.put(key,new Integer(tmp+1));
				}
			}
		}
		System.out.println("List of IMO(s)");
		for(String key:imo_totals.keySet())
		{
			System.out.println("    "+key+"\t :: "+imo_totals.get(key)+" instances");
		}
		System.out.println("Total of IMO(s): "+n);
		n=0;
		for(Object o:objects)
		{
			if(!(o instanceof CRootMetaObject || o instanceof CAttributeMetaObject || o instanceof CInstanceMetaObject || o instanceof CClassVersionMetaObject))
			{
				if(verbose)
				{
					System.out.println(db.ext().getID(o)+"\tData Object   \t"+o.getClass().getCanonicalName());
				}
				n++;
			}
		}
		System.out.println("Total of Data Object: "+n);
		System.out.println("-------------------------------");
	}
	/**
	 * Implements the ClassLoader method loadVersionClass
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	CClassVersionMetaObject around(String classVersionName) throws ClassNotFoundException : call(CClassVersionMetaObject rhp.aof4oop.framework.core.CClassLoader.loadVersionClass(..))  && args(classVersionName)
	{
		//Load from DB
    	List<CClassVersionMetaObject>clazzs=db.query(CClassVersionMetaObject.class);
    	for(CClassVersionMetaObject clazz:clazzs)
    	{
    		if(clazz.toVersionClassName().equals(classVersionName) || clazz.toVersionClassCanonicalName().equals(classVersionName))
    		{
    			return clazz;
    		}
    	}
    	throw new ClassNotFoundException("Class not found on DB ("+clazzs.size()+" entries): "+classVersionName);
	}
	/**
	 *  Implements the ClassLoader method getAllClassVersions
	 * @return
	 */
	List<CClassVersionMetaObject> around(): execution(* rhp.aof4oop.framework.core.CClassLoader.getAllClassVersions())
	{
		//Load from DB
    	List<CClassVersionMetaObject>clazzs=db.query(CClassVersionMetaObject.class);
    	return clazzs;
	}
	void around(CClassVersionMetaObject ocv) : execution(void rhp.aof4oop.framework.core.CClassLoader.storeVersionClass(..)) && args(ocv)
	{
		db.store(ocv);
	}
	boolean around(String viewName,IQuery query) : execution(boolean rhp.aof4oop.framework.core.CPersistentRoot.createView(..)) && args(viewName,query)
	{
		ObjectSet <CView>views=db.queryByExample(new CView(viewName,null));
		if(views.size()==0)
		{
			db.store(new CView(viewName,query));
			return true;
		}
		else
		{
			return false;
		}
	}
	CView around(String viewName) : execution(CView rhp.aof4oop.framework.core.CPersistentRoot.getView(..)) && args(viewName)
	{
		ObjectSet <CView>views=db.queryByExample(new CView(viewName,null));
		if(views.size()==0)
		{
			return null;
		}
		else if(views.size()>1)
		{
			return null;
		}
		else
		{
			return views.get(0);
		}
	}
	boolean around(String viewName) : execution(boolean rhp.aof4oop.framework.core.CPersistentRoot.dropView(..)) && args(viewName)
	{
		ObjectSet <CView>views=db.queryByExample(new CView(viewName,null));
		int n=0;
		for(CView v:views)
		{
			db.delete(v);
			n++;
		}
		return n>0;
	}
	long around() : execution(long rhp.aof4oop.framework.core.CPersistentRoot.beginTransactionDB())
	{
		//Commits any pending db update
		db.commit();
		return 0;
	}
	boolean around() : execution(boolean rhp.aof4oop.framework.core.CPersistentRoot.commitTransactionDB())
	{
		db.commit();
		return true;
	}
	boolean around() : execution(boolean rhp.aof4oop.framework.core.CPersistentRoot.rollBackTransactionDB())
	{
		db.rollback();
		return false;
	}
	/**
	 * Implements the run method of the CQuery
	 * @return
	 */
	List<?> around() : execution(* rhp.aof4oop.framework.core.CQuery.executeQuery()) 
	{
		CQuery query=(CQuery)thisJoinPoint.getTarget();
		List<Object> result;

		try
		{
			result=new ArrayList<Object>();
			ObjectSet <CInstanceMetaObject>oInsts=db.query(CInstanceMetaObject.class);
			for(CInstanceMetaObject imo:oInsts)
			{
				if(imo.getClassName().equals(query.getClassName()))
				{
					Object obj=CPersistentRoot.findCachedObject(imo.getId());
					if(obj==null)
					{
						obj=loadObject(imo,(CAttributeMetaObject)null);
					}
					result.add(obj);
					CPersistentRoot.saveIMOCacheEntry(imo);
				}
			}
		}
		catch (Exception e) 
		{
			throw new EFrameworkFault(e);
		}
		return result;
	}
	CInstanceMetaObject around(long logicalObjectId) : execution(* rhp.aof4oop.framework.core.CPersistentRoot.findMetaObjectInstance(..)) && args(logicalObjectId)
	{
		return findObjectInstance(logicalObjectId);
	}
	void around() : execution(void rhp.aof4oop.framework.core.CPersistentRoot.reloadCacheIMO())
	{
		proceed();
		loadCacheIMO();
	}
	void around() : execution(void rhp.aof4oop.framework.core.CPersistentRoot.reloadCacheAMO())
	{
		proceed();
		loadCacheAMO();
	}
	/**
	 * This predicate specifies the loading of an CAttributeMetaObject by its loid and member
	 * @author rhp
	 *
	 * @param <T>
	 */
	public abstract class PredicateObjectLink<T> extends Predicate<T> 
	{
		private static final long serialVersionUID = 1L;
		private long parentLoid;
		private String member;

		
		public PredicateObjectLink(long parentLoid, String member) 
		{
			super();
			this.parentLoid = parentLoid;
			this.member = member;
		}
		public long getParentLoid() 
		{
			return parentLoid;
		}
		public void setParentLoid(long parentLoid) 
		{
			this.parentLoid = parentLoid;
		}
		public String getMember() 
		{
			return member;
		}
		public void setMember(String member) 
		{
			this.member = member;
		}
		@Override
		public boolean match(T arg0) 
		{
				return false;
		}
	};
	/**
	 *  This predicate specifies the loading of all CAttributeMetaObject that are not in the cache collection
	 * @author rhp
	 *
	 * @param <T>
	 */
	public class PredicateObjectLinkCached extends Predicate<CAttributeMetaObject> 
	{
		private static final long serialVersionUID = 1L;
		private Hashtable<String,CAttributeMetaObject> cache;
		
		public PredicateObjectLinkCached(Hashtable<String,CAttributeMetaObject> cache) 
		{
			super();
			this.cache=cache;
		}

		public Hashtable<String, CAttributeMetaObject> getCache() {
			return cache;
		}

		public void setCache(Hashtable<String, CAttributeMetaObject> cache) {
			this.cache = cache;
		}
		@Override
		public boolean match(CAttributeMetaObject olink)
		{
			return !getCache().containsKey(olink.calcKey());
		}
	};
}
