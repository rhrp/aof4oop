package rhp.aof4oop.oo7.benchmarck;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rhp.aof4oop.oo7.datamodel.Assembly;
import rhp.aof4oop.oo7.datamodel.AtomicPart;
import rhp.aof4oop.oo7.datamodel.BaseAssembly;
import rhp.aof4oop.oo7.datamodel.ComplexAssembly;
import rhp.aof4oop.oo7.datamodel.CompositePart;
import rhp.aof4oop.oo7.datamodel.Connection;
import rhp.aof4oop.oo7.datamodel.Module;

import com.db4o.Db4o;
import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.config.EmbeddedConfiguration;

public class DB4O 
{
	
	public static void main(String[] args) 
	{
		
		String op="trav";
		String typeOfSearch=SharedArea.Trav1;
		ObjectContainer db = createConnDB();
		

		if("createdb".equals(op))
		{
			double t=createDB(db);
			System.out.println("C"+SharedArea.NumCompPerModule+"L"+SharedArea.NumAssmLevels+" DB creation: "+t+" seconds");
		}
		else if("trav".equals(op))
		{
			double tini=System.currentTimeMillis();
			int count=traverse(db,typeOfSearch);
			System.out.println("count="+count);
			System.out.println("Cold DB "+typeOfSearch+": "+((double)System.currentTimeMillis()-tini)/((double)1000)+" seconds");

			tini=System.currentTimeMillis();
			count=traverse(db,typeOfSearch);
			System.out.println("count="+count);
			System.out.println("Hot DB "+typeOfSearch+": "+((double)System.currentTimeMillis()-tini)/((double)1000)+" seconds");
		}
		else if("listdb".equals(op))
		{
			double tini=System.currentTimeMillis();
			listDB(db);
			System.out.println("DB list: "+((double)System.currentTimeMillis()-tini)/((double)1000)+" seconds");
		}
		else
		{
			System.out.println("Unkwon operation op="+op);
		}
	}
	private static ObjectContainer createConnDB()
	{
		System.out.println("Create a DB conn");
//		//EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
//		//configuration.common().reflectWith(new JdkReflector(ClassLoader.getSystemClassLoader()));
//		//ObjectContainer db = Db4oEmbedded.openFile(configuration, "oo7db4o.dbf");
//		ObjectContainer db = Db4oEmbedded.openFile("oo7db4o.dbf");
//		Db4o.configure().objectClass(BaseAssembly.class).cascadeOnUpdate(true);
//		Db4o.configure().objectClass(ComplexAssembly.class).cascadeOnUpdate(true);
//		Db4o.configure().objectClass(Module.class).cascadeOnUpdate(true);
//		System.out.println("Db4o version used: " + Db4o.version());
		
		
		
		EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
		configuration.cache().slotCacheSize(1024);
		//configuration.file().storage(new MemoryStorage());

		ObjectContainer db=Db4oEmbedded.openFile(configuration, "oo7db4o.dbf");
		System.out.println("Db4o version used: " + Db4o.version());
		return db;
	}
	private static long genBuildDate()
	{
		return (new java.util.Date()).getTime();
	}

	private static double createDB(ObjectContainer db)
	{
		long tini=System.currentTimeMillis();
		Module[] modules;
		System.out.println("Init db4o connection");
		
		LinkingMap linkingMap=new LinkingMap();
		
		// Create the modules, BaseAssemblies and ComplexAssemblies
		int startCompId = 1;
		int endCompId = SharedArea.numberOfCompositeObjects;
		int totalModules = SharedArea.TotalModules;
		modules=new Module[totalModules];
		System.out.println("generating composite parts from " + startCompId	+ " to " + endCompId);
		/*
		 * First generate the desired number of modules
		 */
		if (startCompId == 1) 
		{
			/*
			 * first run, generate modules
			 */
			while (SharedArea.nextModuleId <= totalModules) 
			{
				Module module;

				int id = SharedArea.nextModuleId++;
				System.out.println("about to generate module with id:" + id);

				module=new Module(id,System.currentTimeMillis());
				modules[SharedArea.nextModuleId-2]=module;
				
				System.out.println(module.getType());
				System.out.println(module.getDesignRoot().getType());
				System.out.println(module.getType()+" subAssemblies="+module.getDesignRoot().getSubAssemblies().length);
				System.out.println(module.getType()+" Assemblies="+module.getAssemblies().length);
				
				for(Assembly a:module.getAssemblies())
				{
					System.out.println("Id:"+a.getId()+"  type="+a.getType());
					if(a instanceof BaseAssembly)
					{
						Utils.fillLinkingMap(linkingMap,(BaseAssembly)a);
					}
				}
				linkingMap.printBAidList();
				System.out.println("Next BA Id="+SharedArea.nextBaseAssemblyId);
				System.out.println("Next CA Id="+SharedArea.nextComplexAssemblyId);
				db.store(module);
				System.out.println("Saving module "+module.getId()+"...");
			}// end while
		}// end if startCompId == 1


		System.out.println("Made modules, now on to composite parts.");
		
		/*
		 * now create a batch of composite parts.
		 */
		SharedArea.nextCompositeId = startCompId;
		int numAtomicPerComp = SharedArea.NumAtomicPerComp;
		SharedArea.nextAtomicId = (startCompId - 1) * numAtomicPerComp + 1;
		
		//checkBAidList(settingsUtil);
		while (SharedArea.nextCompositeId <= endCompId) 
		{
			CompositePart compositePart;

			int id = SharedArea.nextCompositeId++;
			compositePart = new CompositePart(id);
			Utils.linkCompositePartToModule(linkingMap,modules[0],compositePart);
			db.store(compositePart);
			
			if(compositePart.getUsedInPrivate()!=null)
			{
				for(int i=0;i<compositePart.getUsedInPrivate().length;i++)
				{
					db.store(compositePart.getUsedInPrivate()[i]);
				}
			}
			if(compositePart.getUsedInShared()!=null)
			{
				for(int i=0;i<compositePart.getUsedInShared().length;i++)
				{
					db.store(compositePart.getUsedInShared()[i]);
				}
			}

			if ((SharedArea.nextCompositeId % 10) == 0) 
			{
				System.out.println("Made composite part with id: "	+ SharedArea.nextCompositeId+"  of "+endCompId);
			}// end if
		}// end while
		System.out.println("made composite parts from id:" + startCompId+ " to id: " + endCompId);
		//psRoot.setRootObject(modules[0]);
		System.out.println("ok");
		
		return ((double)System.currentTimeMillis()-(double)tini)/(double)1000;
	}
	private static double listDB(ObjectContainer db)
	{
		long tini=System.currentTimeMillis();
		
		List<Module> mds=db.query(Module.class);
		System.out.println("Module Total: "+mds.size());
		
		List<ComplexAssembly> cas=db.queryByExample(ComplexAssembly.class);
		System.out.println("ComplexAssembly Total: "+cas.size());
		
		
		List<BaseAssembly> bas=db.queryByExample(BaseAssembly.class);
		System.out.println("BaseAssembly Total: "+bas.size());
	
		List<CompositePart> cps=db.queryByExample(CompositePart.class);
		System.out.println("CompositPart Total: "+cps.size());

		
		List<AtomicPart> aps=db.queryByExample(AtomicPart.class);
		System.out.println("AtomicPart Total: "+aps.size());

		List<Connection> cs=db.queryByExample(Connection.class);
		System.out.println("Connection Total: "+cs.size());
		
		List<Object> objs=db.queryByExample(Object.class);
		System.out.println("Objects Total: "+objs.size());
		return ((double)System.currentTimeMillis()-(double)tini)/(double)1000;
	}
	/**
	 * The psRoot is passed to avoid gc calls at the end
	 * @param psRoo
	 * @param typeOfSearch
	 * @return
	 */
	private static int traverse(ObjectContainer db,String typeOfSearch)
	{
		int count=0;
		List<Module> modules=db.query(Module.class);
		
		Module module;
		module=modules.get(0);
		
		
		
		System.out.println(module.getType());
		System.out.println(module.getDesignRoot().getType());
		Assembly[] ass=module.getDesignRoot().getSubAssemblies();
		System.out.println(module.getType()+" ass="+ass.length);
		for(Assembly a:ass)
		{
			if(a instanceof ComplexAssembly)
			{
				count+=traverse(db,(ComplexAssembly)a,typeOfSearch,0);
			}
			else
			{
				count+=traverse(db,(BaseAssembly)a,typeOfSearch,0);
			}
		}
		return count;
	}
	private static int traverse(ObjectContainer db,ComplexAssembly ca,String typeOfSearch,int level)
	{
		int count=0;
		level++;
		db.activate(ca,1);
		System.out.println("traverse(ComplexAssemply Level="+level+"  CA:"+ca.getType()+" id="+ca.getId()+" super="+(ca.getSuperAssembly()!=null?""+ca.getSuperAssembly().getId():"NONE"));
		if(ca.getSubAssemblies()!=null)
		{
			for(Assembly a:ca.getSubAssemblies())
			{
				if(a instanceof ComplexAssembly)
				{
					count+=traverse(db,(ComplexAssembly)a,typeOfSearch,level);
				}
				else
				{
					count+=traverse(db,(BaseAssembly)a,typeOfSearch,level);
				}
			}
		}
		return count;
	}
	private static int traverse(ObjectContainer db,BaseAssembly baseAssembly,String typeOfSearch,int level)
	{
		db.activate(baseAssembly,1);
		
		
		//System.out.println("Level="+level+"   BA:"+ba.getType()+"  id="+ba.getId()+" super="+(ba.getSuperAssembly()!=null?""+ba.getSuperAssembly().getId():"NONE"));
		int count = 0;
		//System.out.print("traverse(ba id="+baseAssembly.getId()+" Module Id="+baseAssembly.getModule().getId());
		if(baseAssembly.getComponentsPrivate()!=null)
		{
			db.activate(baseAssembly.getComponentsPrivate(),1);
			//System.out.print("  privates="+baseAssembly.getComponentsPrivate().length);
		}
		else
		{
			//System.out.print("  privates=null!!");
		}
		if(baseAssembly.getComponentsShared()!=null)
		{
			db.activate(baseAssembly.getComponentsShared(),1);
			//System.out.print("  shared="+baseAssembly.getComponentsShared().length+")");
		}
		else
		{
			//System.out.print("  shared=null!!");
		}
		//System.out.println();
		
		// establish iterator of private composite parts
		// TODO:NOTE: Ozone uses the shared components! why?
		if (baseAssembly.getComponentsPrivate() != null) 
		{
			for (CompositePart compositePart: baseAssembly.getComponentsPrivate()) 
			{
				count += traverse(db,compositePart, typeOfSearch);
			}// end for
		}// end ifcomplexAssembly.getComponentsPrivate()
		return count;
	}
	/**
	 * 
	 * OZONE, does not have the extra if's
	 * 
	 * Db4oTraversal + dfs method in OZONE
	 * 
	 * @param compositePart
	 * @param typeOfSearch
	 * @return 18-Apr-2006
	 */
	public static int traverse(ObjectContainer db,CompositePart compositePart, String typeOfSearch) 
	{
		if (typeOfSearch.equals(SharedArea.Trav1) || typeOfSearch.equals(SharedArea.Trav1WW)
				|| typeOfSearch.equals(SharedArea.Trav2a) || typeOfSearch.equals(SharedArea.Trav2b)
				|| typeOfSearch.equals(SharedArea.Trav2c) || typeOfSearch.equals(SharedArea.Trav3a)
				|| typeOfSearch.equals(SharedArea.Trav3b) || typeOfSearch.equals(SharedArea.Trav3c)) 
		{

			/*
			 * do a DFS(DEPTH FIRST SEARCH) of the composite part's atomic part
			 * graph OZONE, does not have the extra if's
			 */
			Set<Integer> visitedIDs = new HashSet<Integer>();
			System.out.println("traverse(CompositePart Id="+compositePart.getId()+")");
			db.activate(compositePart,1);
			return traverse(db,compositePart.getRootPart(), typeOfSearch,visitedIDs);

		} 
//		else if (typeOfSearch.equals(Db4oLazyTraversal.Trav4)) 
//		{
//			// search document text for a certain character
//			return compositePart.getDocumentation().searchText("I");
//		} 
//		else if (typeOfSearch.equals(Db4oLazyTraversal.Trav5do)) 
//		{
//			// conditionally change initial part of document text
//			// TODO:NOTE pvz: not sure if I need to save this change. Where do
//			// thy do it in the Versant version?
//			int success = compositePart.getDocumentation().replaceText("I am",
//					"This is");
//			Persistence.saveOrUpdate(compositePart.getDocumentation());
//			return success;
//		} 
//		else if (typeOfSearch.equals(Db4oLazyTraversal.Trav5undo)) 
//		{
//			// conditionally change back initial part of document text
//			int success = compositePart.getDocumentation().replaceText(
//					"This is", "I am");
//			Persistence.saveOrUpdate(compositePart.getDocumentation());
//			return success;
//		} 
//		else if (typeOfSearch.equals(Db4oLazyTraversal.Trav6)) 
//		{
//			// visit the root part only (it knows how to handle this)
//			Set visitedIDs = new HashSet();
//
//			return traverse(compositePart.getRootPart(), typeOfSearch,
//					visitedIDs);
//		} 
		else 
		{
			// composite parts don't respond to other Db4oTraversals
			System.out.println("*** CompositePart::PANIC -- illegal Db4oTraversal!!!");
			return 0;
		}
	}
	public static int traverse(ObjectContainer db,AtomicPart atomicPart, String typeOfSearch,	Set<Integer> visitedIDs) 
	{

		int count = 0; // was 1 in Version 1. Why???

		if (SharedArea.Trav1.equals(typeOfSearch)) 
		{
			db.activate(atomicPart,1);
			
			// just examine the part
			//System.out.println("    AtomicPart Id="+atomicPart.getId()+"  partOf="+atomicPart.getPartOf().getId());
			count += 1;
			// DoNothing();
		} 
		else if (SharedArea.Trav2a.equals(typeOfSearch)) 
		{
			// swap x and y if first part
			if (visitedIDs.isEmpty()) 
			{
				atomicPart.swapXY();
				count += 1;
			}
		} 
		else if (SharedArea.Trav2b.equals(typeOfSearch)) 
		{
			// swap X and Y
			atomicPart.swapXY();
			count += 1;
		} 
//		else if (Db4oLazyTraversal.Trav2c.equals(typeOfSearch)) 
//		{
//
//			// swap X and Y repeatedly
//			for (int i = 0; i < SettingsUtil.UpdateRepeatCnt; i++) {
//				atomicPart.swapXY();
//				Persistence.saveOrUpdate(atomicPart);
//				count += 1;
//			}// end for
//		} 
//		else if (Db4oLazyTraversal.Trav3a.equals(typeOfSearch)) 
//		{
//
//			// toggle date if first part
//			if (visitedIDs.isEmpty()) {
//				atomicPart.toggleDate();
//				Persistence.saveOrUpdate(atomicPart);
//				count += 1;
//			}
//		} 
//		else if (Db4oLazyTraversal.Trav3b.equals(typeOfSearch)) 
//		{
//
//			// toggle date
//			atomicPart.toggleDate();
//			Persistence.saveOrUpdate(atomicPart);
//			count += 1;
//		} 
//		else if (Db4oLazyTraversal.Trav3c.equals(typeOfSearch)) 
//		{
//
//			// toggle date repeatedly
//			for (int i = 0; i < SettingsUtil.UpdateRepeatCnt; i++) {
//				atomicPart.toggleDate();
//				Persistence.saveOrUpdate(atomicPart);
//				count += 1;
//			}
//		} 
//		else if (Db4oLazyTraversal.Trav6.equals(typeOfSearch)) 
//		{
//
//			// examine only the root part
//			count += 1;
//			// DoNothing();
//			return count;
//
//		} 
		else 
		{
			// atomic parts don't respond to other Db4oTraversals
			System.out.println("*** AtomicPart()->PANIC -- illegal Db4oTraversal!!!");

		}
		// now, record the fact that we've visited this part
		visitedIDs.add(new Integer(atomicPart.getId()));
		// finally, continue with a DFS of the atomic parts graph
		// establish iterator over connected parts

		if (atomicPart.getToConnections() != null) 
		{
			db.activate(atomicPart.getToConnections(),1);
			int n=0;
			for (Connection connection: atomicPart.getToConnections()) 
			{
				db.activate(connection,1);
				n++;
				AtomicPart nextAtomicPart = connection.getTo();
				db.activate(nextAtomicPart,1);
				if (!visitedIDs.contains(new Integer(nextAtomicPart.getId()))) 
				{
					count += traverse(db,nextAtomicPart, typeOfSearch, visitedIDs);
					//System.out.println("Visit AtomicPart Id "+atomicPart.getId()+"  connection "+n+" of "+atomicPart.getToConnections().length+"  cont="+count+"   atomicPart visited="+visitedIDs.size());
				}
			}// end for
		}// end if

		return count;
	}// end traverse atomic part
}
