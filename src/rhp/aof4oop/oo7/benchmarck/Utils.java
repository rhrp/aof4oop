package rhp.aof4oop.oo7.benchmarck;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import rhp.aof4oop.oo7.datamodel.Assembly;
import rhp.aof4oop.oo7.datamodel.BaseAssembly;
import rhp.aof4oop.oo7.datamodel.CompositePart;
import rhp.aof4oop.oo7.datamodel.Module;

public class Utils 
{
	public static void linkCompositePartToModule(LinkingMap linkingMap,Module module,CompositePart compositePart)
	{
		/*
		 * finally insert this composite part as a child of the base assemblies
		 * that use it first the assemblies using the comp part as a
		 * usedInShared component get the first base assembly baseAssembly =
		 * Shared_cp[cpId].next();
		 */
		BaseAssembly baseAssemblyShared = null;
		ArrayList<BaseAssembly> baseAssemblieShared=new ArrayList<BaseAssembly>();
		ArrayList<BaseAssembly> baseAssembliePrivate=new ArrayList<BaseAssembly>();
		boolean opMode=true;

		if ((linkingMap.getSharedCompositePartIDs() != null) && linkingMap.getSharedCompositePartIDs().size() > 0) 
		{
//			System.out.println("shared part ids size: "+ linkingMap.getSharedCompositePartIDs().size());
//			System.out.println("shared part ids size: "+ linkingMap.getSharedCompositePartIDs().containsKey(new Long(compositePart.getId())));
			BAidList baIdList;
			baIdList = linkingMap.getSharedCompositePartIDs().get(new Long(compositePart.getId()));
			if (baIdList != null) 
			{
//				System.out.println("shared BA ID LIST NOT EMPTY size="+baIdList.size());
				Set<Long> listOfBAids = baIdList.getBaIdList();

				for (Iterator<Long> iter = listOfBAids.iterator(); iter.hasNext();)
				{
					Long baseAssemblyID = (Long) iter.next();
//					System.out.println("Find baseAssembly with ID="+baseAssemblyID.intValue());
					baseAssemblyShared = findOneBaseAssembly(module,baseAssemblyID);
					if (baseAssemblyShared != null) 
					{
//						System.out.print("Add Shared: "+baseAssemblyShared.getId()+"  ");
						if(opMode)
						{
							baseAssemblieShared.add(baseAssemblyShared);
//							System.out.print(" "+baseAssembliePrivate.size());
							baseAssemblyShared.setComponentsShared(CompositePart.add(baseAssemblyShared.getComponentsShared(),compositePart));
						}
						else
						{
							compositePart.addShared(baseAssemblyShared);
						}
//						System.out.println(" Ok.");
					} 
					else 
					{
						System.out.println("pvz: Why is the base assembly null for shared?");

					}// end if-else equals
				}// END FOR
			} 
		}
		/*
		 * 
		 * Next the assemblies using the comp part as a usedInPrivate component.
		 * Get the first base assembly
		 * 
		 */

		BaseAssembly baseAssemblyPrivate = null;
		if ((linkingMap.getPrivateCompositePartIDs() != null) && linkingMap.getPrivateCompositePartIDs().size() > 0) 
		{
//			System.out.println("private part ids size: "+ linkingMap.getPrivateCompositePartIDs().size());
//			System.out.println("private part ids size: "+ linkingMap.getSharedCompositePartIDs().containsKey(new Long(compositePart.getId())));
			BAidList baIdList;
			baIdList = linkingMap.getPrivateCompositePartIDs().get(new Long(compositePart.getId()));

			if (baIdList != null) 
			{
				// System.out.println("private BA ID LIST NOT EMPTY");
				Set<Long> listOfBAids = baIdList.getBaIdList();

				for (Iterator<Long> iter = listOfBAids.iterator(); iter.hasNext();) 
				{
					Long baseAssemblyID = (Long) iter.next();
					baseAssemblyPrivate = findOneBaseAssembly(module,baseAssemblyID.intValue());

					if (baseAssemblyPrivate != null) 
					{
//						System.out.println("Add Private: "+baseAssemblyPrivate.getId()+" ");
						if(opMode)
						{
							baseAssembliePrivate.add(baseAssemblyPrivate);
							baseAssemblyPrivate.setComponentsPrivate(CompositePart.add(baseAssemblyPrivate.getComponentsPrivate(),compositePart));
						}
						else
						{
							compositePart.addPrivate(baseAssemblyPrivate);
						}
						//System.out.print("now save...");
						//Persistence.save(baseAssemblyPrivate);
						//System.out.println("ok");
					} 
					else 
					{
						System.out.println("pvz: Why is the base assembly null for private?");
					}

				}// END FOR
			} 
		}
		if(baseAssembliePrivate.size()>0)
		{
			BaseAssembly[] tmp=baseAssembliePrivate.toArray(new BaseAssembly[baseAssembliePrivate.size()]);
			System.out.println("CompositePart "+compositePart.getId()+"::Set Private: "+tmp.length);
			compositePart.setUsedInPrivate(tmp);
		}
		if(baseAssemblieShared.size()>0)
		{
			BaseAssembly[] tmp=baseAssemblieShared.toArray(new BaseAssembly[baseAssemblieShared.size()]);
			System.out.println("CompositePart "+compositePart.getId()+"::Set Shared: "+tmp.length);
			compositePart.setUsedInShared(tmp);
		}
	}
	public static void fillLinkingMap(LinkingMap linkingMap,BaseAssembly baseAssemply)
	{
		long lowCompId = (baseAssemply.getModule().getId() - 1) * SharedArea.NumCompPerModule + 1;
		long compIdLimit = SharedArea.NumCompPerModule;

		// first select the private composite parts for this assembly
		int numCompPerAssm = SharedArea.NumCompPerAssm;

		//System.out.println("fillLinkingMap(baseAssembly ID "+baseAssemply.getId()+")");
		Set<Long> tmp=new TreeSet<Long>();
		while (tmp.size()< numCompPerAssm) 
		{
			// TODO:NOTE pvz: we could also have used
			// Random().nextInt(compIdLimit) then 0<=next <compIdLimit
			long compId = lowCompId + (RandomUtil.nextInt() % compIdLimit);
			if(!tmp.contains(compId))
			{
				tmp.add(compId);
				/*
				 * keep track of which CompositePart uses this base assembly as
				 * private Private_cp[compId].insert(self);
				 */

				if (linkingMap.getPrivateCompositePartIDs().containsKey((new Long(compId)))) 
				{

				}
				BAidList baIdList = linkingMap.getPrivateCompositePartIDs().get(new Long(compId));
				if (baIdList == null) 
				{
					//				System.out.println(" creating ba id list");
					baIdList = new BAidList();
					baIdList.setBaIdList(new TreeSet<Long>());
				}
				baIdList.getBaIdList().add(new Long(baseAssemply.getId()));
				//			System.out.println(" >>> baIdList size: "+baIdList.size());
				linkingMap.getPrivateCompositePartIDs().put(new Long(compId), baIdList);
				//			System.out.println("BaseAssembly()->Adding private composite parts with compId: "+ compId);
			}
		}



		// TODO: OZONE's implementation only sets the shared components.
		tmp=new TreeSet<Long>();
		while (tmp.size()< numCompPerAssm) 
		{
			long compId = (RandomUtil.nextInt() % SharedArea.TotalCompParts) + 1;
			/*
			 * uses this base assembly as shared
			 * Shared_cp[compositePartId].insert(self); keep track of which CP
			 * uses this base assembly as shared
			 */
			if(!tmp.contains(compId))
			{
				tmp.add(compId);
				if (linkingMap.getSharedCompositePartIDs().containsKey(new Long(compId))) 
				{
					//				System.out.println(" This id already exists!!!!!!!!!!!!!!!!!: "
					//						+ compId);
				}
				BAidList baIdList = linkingMap.getSharedCompositePartIDs().get(new Long(compId));
				if (baIdList == null) 
				{
					//				System.out.println(" creating ba id list");
					baIdList = new BAidList();
					baIdList.setBaIdList(new TreeSet<Long>());
				}
				baIdList.getBaIdList().add(new Long(baseAssemply.getId()));
				//			System.out.println(" >>> baIdList size: "+baIdList.size());
				linkingMap.getSharedCompositePartIDs().put(new Long(compId), baIdList);
				//			System.out.println("BaseAssembly()->Adding shared composite parts with compId: "+ compId);
			}
		}
	}
	private static BaseAssembly findOneBaseAssembly(Module module,long id)
	{
		for(Assembly ba:module.getAssemblies())
		{
			if(ba.getId()==id)
			{
				return (BaseAssembly)ba;
			}
		}
		System.out.println("BaseAssembly id="+id+" not exists");
		return null;
	}
}
