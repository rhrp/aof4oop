package rhp.aof4oop.framework.aspects;

import java.io.IOException;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import rhp.aof4oop.framework.core.CClassLoader;
import rhp.aof4oop.framework.core.CPersistentRoot;
import rhp.aof4oop.framework.core.datamodel.CAttributeMetaObject;
import rhp.aof4oop.framework.core.datamodel.CClassVersionMetaObject;
import rhp.aof4oop.framework.core.datamodel.CInstanceMetaObject;
import rhp.aof4oop.framework.core.exceptions.EFrameworkFault;

public aspect AFrameWorkStats 
{
	private int saveObjectLevel;
	private int updateObjectLevel;
	public AFrameWorkStats() 
	{
		super();
		saveObjectLevel=0;
		updateObjectLevel=0;
	}

	void around() : execution(void rhp.aof4oop.framework.core.CInstanceAdaptationMetadata.__weaveCode(..))
	{
		long t_ini=System.currentTimeMillis();
		proceed();
		CPersistentRoot.statsTimeWeaving+=(System.currentTimeMillis()-t_ini);
		CPersistentRoot.statsTotalWeavings++;
	}
	CAttributeMetaObject around() : execution(CAttributeMetaObject rhp.aof4oop.framework.aspects.AObjectStorageDB4O.recordLink(..))
	{
		long t_ini=System.currentTimeMillis();
		CAttributeMetaObject out=proceed();
		CPersistentRoot.statsTimeRecordingAMO+=(System.currentTimeMillis()-t_ini);
		CPersistentRoot.statsTotalRecordedAMO++;
		return out;
	}
	void around() : execution(void rhp.aof4oop.framework.aspects.AObjectStorageDB4O.__setFieldArray(..))
	{
		long tini=System.currentTimeMillis();
		proceed();
		CPersistentRoot.statsTimeSetFieldArray+=(System.currentTimeMillis()-tini);
	}
	void around() : execution(void rhp.aof4oop.framework.aspects.AObjectStorageDB4O.__setFieldObject(..))
	{
		long tini=System.currentTimeMillis();
		proceed();
		CPersistentRoot.statsTimeSetFieldObject+=(System.currentTimeMillis()-tini);
	}
	void around() : execution(void rhp.aof4oop.framework.aspects.AObjectStorageDB4O.__getFieldArray(..))
	{
		long tini=System.currentTimeMillis();
		proceed();
		CPersistentRoot.statsTimeGetFieldArray+=(System.currentTimeMillis()-tini);
	}
	void around() : execution(void rhp.aof4oop.framework.aspects.AObjectStorageDB4O.__getFieldObject(..))
	{
		long tini=System.currentTimeMillis();
		proceed();
		CPersistentRoot.statsTimeGetFieldObject+=(System.currentTimeMillis()-tini);
	}
	CInstanceMetaObject around() : execution(CInstanceMetaObject rhp.aof4oop.framework.aspects.AObjectStorageDB4O.saveObject(..))
	{
		saveObjectLevel++;
		long tini=System.currentTimeMillis();
		CInstanceMetaObject out=proceed();
		
		long t=(System.currentTimeMillis()-tini);
		if(saveObjectLevel==1)
		{
			// Upper levels will be considered ate first level of recursivity
			CPersistentRoot.statsTimeSavingObjects+=t;
		}
		CPersistentRoot.statsTotalSavedObjects++;
		//System.out.println("Level: "+saveObjectLevel+" Saved objects:"+CPersistentRoot.statsTotalSavedObjects+"  "+(out==null?"????":out.getClassName())+" Total:"+CPersistentRoot.statsTimeSavingObjects+"  consumido:"+t);		
		saveObjectLevel--;
		return out;
	}
	//updateObject()
	CInstanceMetaObject around() : execution(CInstanceMetaObject rhp.aof4oop.framework.aspects.AObjectStorageDB4O.updateObjectEmulated(..))
	{
		updateObjectLevel++;
		long tini=System.currentTimeMillis();
		CInstanceMetaObject out=proceed();
		
		long t=(System.currentTimeMillis()-tini);
		if(updateObjectLevel==1)
		{
			// Upper levels will be considered ate first level of recursivity
			CPersistentRoot.statsTimeUpdatingObjects+=t;
		}
		CPersistentRoot.statsTotalUpdatedObjects++;
		updateObjectLevel--;
		return out;
	}
	CInstanceMetaObject around() : execution(CInstanceMetaObject rhp.aof4oop.framework.aspects.AObjectStorageDB4O.updateObject(..))
	{
		updateObjectLevel++;
		long tini=System.currentTimeMillis();
		CInstanceMetaObject out=proceed();
		
		long t=(System.currentTimeMillis()-tini);
		if(updateObjectLevel==1)
		{
			// Upper levels will be considered ate first level of recursivity
			CPersistentRoot.statsTimeUpdatingObjects+=t;
		}
		CPersistentRoot.statsTotalUpdatedObjects++;
		updateObjectLevel--;
		return out;
	}
	/**
	 * writerAdapter stats 
	 * @param memoryObject
	 * @param appObjectClassVersion
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
	Object around(Object memoryObject,CClassVersionMetaObject appObjectClassVersion) throws SecurityException, InstantiationException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, NotFoundException, IOException, CannotCompileException : call(Object rhp.aof4oop.framework.core.datamodel.CClassVersionMetaObject.writerAdapter(..))  && args(memoryObject,appObjectClassVersion)
	{
		long t_ini=System.currentTimeMillis();
		Object out=proceed(memoryObject,appObjectClassVersion);
		CPersistentRoot.statsTimeWriteMapping+=(System.currentTimeMillis()-t_ini);
		return out;
	}
	/**
	 * readerAdapter stats
	 * @param dbObject
	 * @param dbParentObject
	 * @param cl
	 * @param oInst
	 * @param oParentLink
	 * @return
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 * @throws IOException
	 * @throws CannotCompileException
	 * @throws EFrameworkFault
	 */
	Object around(Object dbObject,Object dbParentObject,CClassLoader cl,CInstanceMetaObject oInst,CAttributeMetaObject oParentLink) throws SecurityException, InstantiationException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, NotFoundException, IOException, CannotCompileException,EFrameworkFault : call(Object rhp.aof4oop.framework.core.datamodel.CClassVersionMetaObject.readerAdapter(..))  && args(dbObject,dbParentObject,cl,oInst,oParentLink)
	{
		long t_ini=System.currentTimeMillis();
		Object out=proceed(dbObject,dbParentObject,cl,oInst,oParentLink);
		CPersistentRoot.statsTimeReadMapping+=(System.currentTimeMillis()-t_ini);
		return out;
	}

}
