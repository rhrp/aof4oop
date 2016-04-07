package rhp.aof4oop.framework.aspects;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import rhp.aof4oop.framework.core.CClassLoader;
import rhp.aof4oop.framework.core.CInstanceAdaptationMetadata;
import rhp.aof4oop.framework.core.CPersistentRoot;
import rhp.aof4oop.framework.core.datamodel.CClassVersionMetaObject;
import rhp.aof4oop.framework.core.datamodel.CInstanceMetaObject;
import rhp.aof4oop.framework.core.datamodel.CAttributeMetaObject;
import rhp.aof4oop.framework.core.datamodel.CUpdateBackdateMetaObject;
import rhp.aof4oop.framework.core.exceptions.EFrameworkFault;
import javassist.CannotCompileException;
import javassist.NotFoundException;

public aspect AObjectAdaptation extends APersistence
{
	private CInstanceAdaptationMetadata instanceAdaptationMetadata;	// Instance adaptation meta data 
	
	private AObjectAdaptation() throws JAXBException, ParserConfigurationException, IOException, SAXException
	{
		debugMsg("AObjectAdaptation init");
		instanceAdaptationMetadata=CInstanceAdaptationMetadata.loadSchemaEvolution(true);
		//instanceAdaptationMetadata.dump();
		debugMsg("AObjectAdaptation "+instanceAdaptationMetadata.getUbmo().length+" UBMO meta-objects loaded");
	}
	
	/**
	 * WiterAdapter
	 * 
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
	Object around(Object memoryObject,CClassVersionMetaObject appObjectClassVersion) throws SecurityException, InstantiationException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, NotFoundException, IOException, CannotCompileException : execution(Object rhp.aof4oop.framework.core.datamodel.CClassVersionMetaObject.writerAdapter(..))  && args(memoryObject,appObjectClassVersion)
	{
		long t_ini=System.currentTimeMillis();
		//Turn off Transaction Log
		boolean old_trx_state=CPersistentRoot.setTransactionLogActive(false);
		
		CClassVersionMetaObject target=(CClassVersionMetaObject)thisJoinPoint.getTarget();
		debugMsg("writerAdapter: "+memoryObject.getClass().getCanonicalName()+" --> "+target.toVersionClassName());
		Object out=proceed(memoryObject,appObjectClassVersion);
		CPersistentRoot.statsTimeWriteMapping+=(System.currentTimeMillis()-t_ini);
		
		//Restore transaction log state
		CPersistentRoot.setTransactionLogActive(old_trx_state);

		return out;
	}
	/**
	 * Read Adapter
	 * @param dbObject
	 * @param cl
	 * @param oInst - 
	 * @param oParentLink - Link from parent
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
	Object around(Object dbObject,Object dbParentObject,CClassLoader cl,CInstanceMetaObject oInst,CAttributeMetaObject oParentLink) throws SecurityException, InstantiationException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, NotFoundException, IOException, CannotCompileException,EFrameworkFault : execution(Object rhp.aof4oop.framework.core.datamodel.CClassVersionMetaObject.readerAdapter(..)) && args(dbObject,dbParentObject,cl,oInst,oParentLink)
	{
		Object newObject=null;
		long tini=0;
		//Turn off Transaction Log
		boolean old_trx_state=CPersistentRoot.setTransactionLogActive(false);
		try 
		{		
			CClassVersionMetaObject dbObjectClassVersion=cl.findObjectInstanceClassVersion(oInst);
			CClassVersionMetaObject targetClassVersion=(CClassVersionMetaObject)thisJoinPoint.getTarget();
			debugMsg("readerAdapter: "+dbObjectClassVersion.toVersionClassName()+" --> "+targetClassVersion.getClassCanonicalName());
			if(dbObjectClassVersion.equals(targetClassVersion))
			{
				//No conversion is need
				newObject=proceed(dbObject,dbParentObject,cl,oInst,oParentLink);
			}
			else if(!dbObjectClassVersion.getClassCanonicalName().equals(targetClassVersion.getClassCanonicalName()))
			{
				//upsss!!
				debugMsg("upssss!!!! "+dbObjectClassVersion.getClassCanonicalName()+" not equals to "+targetClassVersion.getClassCanonicalName());
				throw new EFrameworkFault("The source and target classes are not compatible");
			}
			else 
			{
				CClassVersionMetaObject dbObjectParentClassVersion=null;
				if(dbParentObject!=null)
				{
					dbObjectParentClassVersion=cl.findDatabaseObjectClassVersion(dbParentObject);
				}
				CUpdateBackdateMetaObject ubmo=instanceAdaptationMetadata.match(cl,dbObjectClassVersion,dbObjectParentClassVersion,oParentLink);
				if(ubmo!=null)
				{
					tini=System.currentTimeMillis();
//				addDebug("There is an UBMO!!!! ");
//				addDebug("  Class:"+dbObjectClassVersion.getClassCanonicalName());
//				addDebug("  Current class SuperClass:"+dbObjectClassVersion.getSuperClass().getCanonicalName());
//				addDebug("  DB Class SupperClass: "+dbObjectClassVersion.getSuperClassVersion().getCanonicalName());
					Object newObjectParent=null;
					if(oParentLink!=null)
					{
						//CInstanceMetaObject parentmo=CPersistentRoot.findMetaObjectInstance(oParentLink.getParentObjectId());
						//addDebug("  Parent object: "+(parentmo.getClassName())+"->"+oParentLink.getMember());
						newObjectParent=CPersistentRoot.findCachedObject(oParentLink.getParentObjectId());
					}
					if(ubmo.isApplyDefault())
					{
						debugMsg("Apply default adaptation");
						//Initializes the object as is at current version 
						newObject=proceed(dbObject,dbParentObject,cl,oInst,oParentLink);
						//Loads all oldObj's objects at first level, which may be necessary for conversion
						//TODO::037:
						//optimizations for the Geographical app
						//ArrayList<String> tmp=new ArrayList<String>(); tmp.add("relations"); tmp.add("nodesRefs"); tmp.add("member"); tmp.add("tag"); tmp.add("tags"); tmp.add("nodes"); tmp.add("ways");	//TODO::035: implement directives in UBMO to control depth loading and excluded members: "bounds", "data"
						//int tmp_depth=2;
						//optimizations for the oo7 benchmark
						ArrayList<String> tmp=new ArrayList<String>(); tmp.add("toConnections"); tmp.add("fromConnections"); tmp.add("partOf");
						int tmp_depth=2;
						CPersistentRoot.activateAllDBObjectAttributes(dbObject,oInst,tmp_depth,tmp);// Depth is 2  
						//Loads all newObj's objects at first level, which may be necessary for conversion
						CPersistentRoot.activateAllObjectAttributes(newObject,oInst);
					}
					else
					{
						//addDebug("No default adaptation for "+dbObjectClassVersion.getClassCanonicalName());
						newObject=Class.forName(dbObjectClassVersion.getClassCanonicalName()).newInstance();
					}
					CClassVersionMetaObject new_cvmo=cl.findApplicationObjectClassVersion(ubmo.getOutputClassName());
					debugMsg("Output Class:"+(new_cvmo!=null?new_cvmo.toVersionClassCanonicalName():" Not Found!!!"));
					instanceAdaptationMetadata.weaveCode(cl,dbObjectClassVersion,ubmo);
					newObject=instanceAdaptationMetadata.convert(dbObject,dbParentObject,dbObjectClassVersion,newObject,newObjectParent,ubmo);
					CPersistentRoot.registerReachableObjectTree(newObject,oInst.getId());
				}
				else
				{
					debugMsg("upssss!!!! Try the default conversion");
					newObject=proceed(dbObject,dbParentObject,cl,oInst,oParentLink);
				}
			}
			return newObject;
		}
		catch (Exception e) 
		{
			debugException(e);
			newObject=null;
			throw new EFrameworkFault(e.getMessage());
		} 
		finally
		{
			if(tini>0)
			{
				// Case of an user-defined mapping
				long tend=System.currentTimeMillis();
				CPersistentRoot.statsTimeUserDefinedMapping+=(tend-tini);
			}
			CPersistentRoot.setTransactionLogActive(old_trx_state);
		}
	}
}
