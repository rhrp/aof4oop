package rhp.aof4oop.oo7.benchmarck;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import rhp.aof4oop.framework.core.CPersistentRoot;
import rhp.aof4oop.framework.core.CQuery;
import rhp.aof4oop.oo7.datamodel.Assembly;
import rhp.aof4oop.oo7.datamodel.AtomicPart;
import rhp.aof4oop.oo7.datamodel.BaseAssembly;
import rhp.aof4oop.oo7.datamodel.ComplexAssembly;
import rhp.aof4oop.oo7.datamodel.CompositePart;
import rhp.aof4oop.oo7.datamodel.Connection;
import rhp.aof4oop.oo7.datamodel.Module;


public class AOF4OOP 
{

	public static void main(String[] args) throws Exception
	{
		String schemaID=args[0];	// S1, S2...
		String op=args[1];			// "CreateDB", "Trav" or ""listdb"
		String typeOfSearch=("1".equals(args[2])?SharedArea.Trav1:("2".equals(args[2])?SharedArea.Trav2a:"none"));

		System.out.println("SchemaID:  "+schemaID);
		System.out.println("Operation: "+op+"#"+typeOfSearch);

/*		
		schemaID="S2";
//		String op="CreateDB";
		op="Trav";
//		op="listdb";
		typeOfSearch=SharedArea.Trav1;
*/
		

		if("CreateDB".equals(op))
		{
			long t=createDB();
			System.out.println(dbType()+" DB creation: "+t+" ms");
			CPersistentRoot.showObjectMapStatus();
			logTestResult(schemaID, op,"Cold",t);
		}
		else if("Trav".equals(op))
		{
			long tini=System.currentTimeMillis();
			CPersistentRoot psRoot=new CPersistentRoot();
			int count=traverse(psRoot,typeOfSearch);
			System.out.println("count="+count);
			long t=(System.currentTimeMillis()-tini);
			System.out.println("Cold DB "+typeOfSearch+": "+t+" ms");
			logTestResult(schemaID,op+"#"+typeOfSearch,"Cold",t);
			CPersistentRoot.showObjectMapStatus();
			CPersistentRoot.resetLapObjectMap();
			CPersistentRoot.printStats();
		
			CPersistentRoot.resetStats();
			tini=System.currentTimeMillis();
			count=traverse(psRoot,typeOfSearch);
			t=(System.currentTimeMillis()-tini);
			System.out.println("count="+count);
			System.out.println("Hot DB "+typeOfSearch+": "+t+" ms");
			logTestResult(schemaID, op+"#"+typeOfSearch,"Hot",t);
			CPersistentRoot.showObjectMapStatus();
			CPersistentRoot.printStats();
		}
		else if("listdb".equals(op))
		{
			long tini=System.currentTimeMillis();
			describeComplexAssemblies();
			describeBaseAssemblies();
			describeCompositeParts();
			describeAtomicParts();
			describeConnections();
			System.out.println("DB list: "+((double)System.currentTimeMillis()-tini)/((double)1000)+" seconds");
		}
		else if("forName".equals(op))
		{
			try
			{
				System.out.println("new operator="+testNew()+" ms");//18ms
				System.out.println("forName cache="+testForNameCache()+" ms");// 71 ms
				System.out.println("forName normal="+testForNameNormal()+" ms");//271 ms
				System.out.println("forName optimized="+testForNameOptimized()+" ms");//44ms
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
//			
//			CPersistentRoot psRoot=new CPersistentRoot();
//			psRoot.gc();

//			listDB();
			

			System.out.println("Unkwon operation op="+op);
		}
		
	}

	private static long createDB()
	{
		CPersistentRoot psRoot;
		Module[] modules;
		long tini=System.currentTimeMillis();
		
		System.out.println("Init psRoot");
		psRoot=new CPersistentRoot();
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
				psRoot.setRootObject(module);
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

			if ((SharedArea.nextCompositeId % 10) == 0) 
			{
				System.out.println("Made composite part with id: "	+ SharedArea.nextCompositeId+"  of "+endCompId);
			}// end if
		}// end while
		System.out.println("made composite parts from id:" + startCompId+ " to id: " + endCompId);
		//psRoot.setRootObject(modules[0]);
		System.out.println("ok");
		
		return System.currentTimeMillis()-tini;
	}

	/**
	 * The psRoot is passed to avoid gc calls at the end
	 * @param psRoo
	 * @param typeOfSearch
	 * @return
	 */
	private static int traverse(CPersistentRoot psRoot,String typeOfSearch)
	{
		int count=0;
		
		
		Module module;
		module=psRoot.getRootObject();
		
		
		
		System.out.println(module.getType());
		System.out.println(module.getDesignRoot().getType());
		Assembly[] ass=module.getDesignRoot().getSubAssemblies();
		System.out.println(module.getType()+" ass="+ass.length);
		for(Assembly a:ass)
		{
			if(a instanceof ComplexAssembly)
			{
				count+=traverse((ComplexAssembly)a,typeOfSearch,0);
			}
			else
			{
				count+=traverse((BaseAssembly)a,typeOfSearch,0);
			}
		}
		return count;
	}
	private static int traverse(ComplexAssembly ca,String typeOfSearch,int level)
	{
		int count=0;
		level++;
		System.out.println("traverse(ComplexAssemply Level="+level+"  CA:"+ca.getType()+" id="+ca.getId()+" super="+(ca.getSuperAssembly()!=null?""+ca.getSuperAssembly().getId():"NONE"));
		if(ca.getSubAssemblies()!=null)
		{
			for(Assembly a:ca.getSubAssemblies())
			{
				if(a instanceof ComplexAssembly)
				{
					count+=traverse((ComplexAssembly)a,typeOfSearch,level);
				}
				else
				{
					count+=traverse((BaseAssembly)a,typeOfSearch,level);
				}
			}
		}
		return count;
	}
	private static int traverse(BaseAssembly baseAssembly,String typeOfSearch,int level)
	{
		//System.out.println("Level="+level+"   BA:"+ba.getType()+"  id="+ba.getId()+" super="+(ba.getSuperAssembly()!=null?""+ba.getSuperAssembly().getId():"NONE"));
		int count = 0;
		System.out.print("traverse(ba id="+baseAssembly.getId()+" Module Id="+baseAssembly.getModule().getId());
		System.out.print("  privates="+baseAssembly.getComponentsPrivate().length);
		System.out.println("  shared="+baseAssembly.getComponentsShared().length+")");
		

		// establish iterator of private composite parts
		// TODO:NOTE: Ozone uses the shared components! why?
		if (baseAssembly.getComponentsPrivate() != null) 
		{
			for (CompositePart compositePart: baseAssembly.getComponentsPrivate()) 
			{
				count += traverse(compositePart, typeOfSearch);
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
	public static int traverse(CompositePart compositePart, String typeOfSearch) 
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
			//System.out.println("traverse(CompositePart Id="+compositePart.getId()+")");
			return traverse(compositePart.getRootPart(), typeOfSearch,visitedIDs);
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
	public static int traverse(AtomicPart atomicPart, String typeOfSearch,	Set<Integer> visitedIDs) 
	{

		int count = 0; // was 1 in Version 1. Why???

		if (SharedArea.Trav1.equals(typeOfSearch)) 
		{
			// just examine the part
			//System.out.println("    AtomicPart Id="+atomicPart.getId()+"  partOf compositePart="+atomicPart.getPartOf().getId());
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
			int n=0;
			for (Connection connection: atomicPart.getToConnections()) 
			{
				n++;
				AtomicPart nextAtomicPart = connection.getTo();
				if (!visitedIDs.contains(new Integer(nextAtomicPart.getId()))) 
				{
					count += traverse(nextAtomicPart, typeOfSearch, visitedIDs);
					//System.out.println("Visit AtomicPart Id "+atomicPart.getId()+"  connection "+n+" of "+atomicPart.getToConnections().length+"  cont="+count+"   atomicPart visited="+visitedIDs.size());
				}
			}// end for
		}
		else
		{
			System.out.println("Visit AtomicPart Id "+atomicPart.getId()+"  without conections");
		}	// end if
		

		return count;
	}// end traverse atomic part
	
	/**
	 * Resume all database content
	 */
	private static void listDB()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		System.out.println("Cache:"+psRoot.countCache());
		
		List<Module> mds=psRoot.query(new CQuery(Module.class));
		System.out.println("Module Total: "+mds.size());
		
		System.out.println("Cache:"+psRoot.countCache());
		
		List<ComplexAssembly> cas=psRoot.query(new CQuery(ComplexAssembly.class));
		System.out.println("ComplexAssembly Total: "+cas.size());
		
		System.out.println("Cache:"+psRoot.countCache());
		
		List<BaseAssembly> bas=psRoot.query(new CQuery(BaseAssembly.class));
		System.out.println("BaseAssembly Total: "+bas.size());
		
		System.out.println("Cache:"+psRoot.countCache());
		
		List<CompositePart> cps=psRoot.query(new CQuery(CompositePart.class));
		System.out.println("CompositPart Total: "+cps.size());
		
		System.out.println("Cache:"+psRoot.countCache());
		
		List<AtomicPart> aps=psRoot.query(new CQuery(AtomicPart.class));
		System.out.println("AtomicPart Total: "+aps.size());
		
		System.out.println("Cache:"+psRoot.countCache());
		
		List<Connection> cs=psRoot.query(new CQuery(Connection.class));
		System.out.println("Connection Total: "+cs.size());

		System.out.println("Cache:"+psRoot.countCache());
	}
	public static void describeComplexAssemblies()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		List<ComplexAssembly> cas=psRoot.query(new CQuery(ComplexAssembly.class));
		System.out.println("ComplexAssembly Total: "+cas.size());
		
		for(ComplexAssembly ca:cas)
		{
			System.out.println("CA "+ca.getId()+"  subAssemblies="+(ca.getSubAssemblies()!=null?""+ca.getSubAssemblies().length:"null!!")+" super=CA(id:"+(ca.getSuperAssembly()!=null?""+ca.getSuperAssembly().getId():"null!!!")+")");
		}
	}
	public static void describeBaseAssemblies()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		List<BaseAssembly> bas=psRoot.query(new CQuery(BaseAssembly.class));
		System.out.println("BaseAssembly Total: "+bas.size());
		
		for(BaseAssembly ba:bas)
		{
			System.out.println("BA "+ba.getId()+"  private="+ba.getComponentsPrivate().length+"  shared="+ba.getComponentsShared().length);
		}
	}
	public static void describeCompositeParts()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		List<CompositePart> cps=psRoot.query(new CQuery(CompositePart.class));
		System.out.println("CompositPart Total: "+cps.size());
		int n=0;
		for(CompositePart cp:cps)
		{
			if(n%100==0)
			{
			 System.out.println("CP "+cp.getId()+"  parts="+(cp.getParts()!=null?""+cp.getParts().length:"Null!!!")+"  private BAs="+(cp.getUsedInPrivate()!=null?""+cp.getUsedInPrivate().length:"null!!!")+"  shared BAs="+(cp.getUsedInShared()!=null?""+cp.getUsedInShared().length:"null!!!"));
			}
			n++;
		}
	}
	public static void describeAtomicParts()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		List<AtomicPart> aps=psRoot.query(new CQuery(AtomicPart.class));
		System.out.println("AtomicPart Total: "+aps.size());
		int n=0;
		for(AtomicPart ap:aps)
		{
			if(n%200==0)
			{
			System.out.println("AP "+ap.getId()+" partOf=CP(id:"+ap.getPartOf().getId()+") from="+ap.getFromConnections().length+"  to="+ap.getFromConnections().length);
			}
			n++;
		}
	}
	public static void describeConnections()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		List<Connection> conns=psRoot.query(new CQuery(Connection.class));
		System.out.println("Connection Total: "+conns.size());
		int n=0;
		for(Connection conn:conns)
		{
			if(n%1000==0)
			{
				System.out.println("Conn From=AT(id:"+conn.getFrom().getId()+")  to=AT(id:"+conn.getTo().getId()+")");
			}
			n++;
		}
	}
	public static double testNew()
	{
		double tini=System.currentTimeMillis();
		for(int i=0;i<100000;i++)
		{
			rhp.aof4oop.oo7.datamodel.ComplexAssembly a=new rhp.aof4oop.oo7.datamodel.ComplexAssembly();
		}
		double tend=System.currentTimeMillis();
		return tend-tini;
	}
	public static double testForNameOptimized() throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		Class<?> clzz=Class.forName("rhp.aof4oop.oo7.datamodel.ComplexAssembly");
		double tini=System.currentTimeMillis();
		for(int i=0;i<100000;i++)
		{
			ComplexAssembly a=(rhp.aof4oop.oo7.datamodel.ComplexAssembly)clzz.newInstance();
		}
		double tend=System.currentTimeMillis();
		return tend-tini;
	}
	public static double testForNameNormal() throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		double tini=System.currentTimeMillis();
		for(int i=0;i<100000;i++)
		{
			Class<?> clzz=Class.forName("rhp.aof4oop.oo7.datamodel.ComplexAssembly");
			ComplexAssembly a=(rhp.aof4oop.oo7.datamodel.ComplexAssembly)clzz.newInstance();
		}
		double tend=System.currentTimeMillis();
		return tend-tini;
	}
	public static double testForNameCache() throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		Hashtable c=new Hashtable();
//		//Fill the cache
		for(int i=0;i<100;i++)
		{
			c.put("class_"+i,"class");
		}
		
		double tini=System.currentTimeMillis();
		for(int i=0;i<100000;i++)
		{
			Class<?> clzz=(Class<?>)c.get("rhp.aof4oop.oo7.datamodel.ComplexAssembly");
			if(clzz==null)
			{
				clzz=Class.forName("rhp.aof4oop.oo7.datamodel.ComplexAssembly");
				c.put("rhp.aof4oop.oo7.datamodel.ComplexAssembly",clzz);
			}
			ComplexAssembly a=(rhp.aof4oop.oo7.datamodel.ComplexAssembly)clzz.newInstance();
		}
		double tend=System.currentTimeMillis();
		return tend-tini;
	}
	public static double testJavaassist() throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		double tini=System.currentTimeMillis();
		for(int i=0;i<100000;i++)
		{
			
			ComplexAssembly a=(rhp.aof4oop.oo7.datamodel.ComplexAssembly)Class.forName("rhp.aof4oop.oo7.datamodel.ComplexAssembly").newInstance();
		}
		double tend=System.currentTimeMillis();
		return tend-tini;
	}
	public static void logTestResult(String schemaId,String operationType,String hot_cold,long time_ms) throws IOException 
	{
		File fout = new File("aof4oop_oo7_tests.csv");
		boolean new_file=!fout.exists();
		FileWriter fileWriter = new FileWriter(fout,true);
	 
		BufferedWriter bw = new BufferedWriter(fileWriter);
		try
		{
			if(new_file)
			{
				bw.write("dbType;schemaId;operationType;hot/cold;time_ms;statsTimeWeaving;statsTotalWeavings;statsTimeDirectMapping;statsTimeUserDefinedMapping;statsTimeWriteMapping;statsTimeReadMapping;statsTotalSavedObjects;statsTimeSavingObjects;statsTotalUpdatedObjects;statsTimeUpdatingObjects");
				bw.newLine();
			}
			bw.write("\""+dbType()+"\";\""+schemaId+"\";\""+operationType+"\";\""+hot_cold+"\";"+time_ms+";"+CPersistentRoot.statsTimeWeaving+";"+CPersistentRoot.statsTotalWeavings+";"+CPersistentRoot.statsTimeDirectMapping+";"+CPersistentRoot.statsTimeUserDefinedMapping+";"+CPersistentRoot.statsTimeWriteMapping+";"+CPersistentRoot.statsTimeReadMapping+";"+CPersistentRoot.statsTotalSavedObjects+";"+CPersistentRoot.statsTimeSavingObjects+";"+CPersistentRoot.statsTotalUpdatedObjects+";"+CPersistentRoot.statsTimeUpdatingObjects);
			bw.newLine();
		}
		finally
		{
			bw.close();
		}

	}
	public static String dbType()
	{
		return "C"+SharedArea.NumCompPerModule+"L"+SharedArea.NumAssmLevels;
	}
}
