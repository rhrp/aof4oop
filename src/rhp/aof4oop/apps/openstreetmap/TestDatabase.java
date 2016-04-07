package rhp.aof4oop.apps.openstreetmap;

import java.util.List;


import rhp.aof4oop.framework.core.CPersistentRoot;
import rhp.aof4oop.framework.core.CQuery;
import rhp.aof4oop.framework.core.datamodel.CInstanceMetaObject;
import rhp.osm.datamodel.xml.Node;
import rhp.osm.datamodel.xml.Way;

/**
 * This test application shows all objects related with OSM Database
 * @author rhp
 *
 */
public class TestDatabase 
{
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		System.out.println("OSM Database tester");
		
		System.out.println("Init psRoot");
		CPersistentRoot psRoot=new CPersistentRoot();
		psRoot.setGcVerbose(1);
		//psRoot.gc(2);
				
		System.out.println("Loading database...");
		
		//Loads all Nodes
		List<Node> nodes = psRoot.query(new CQuery(Node.class));
		System.out.println("Loaded Nodes: "+nodes.size());
		
		List<Node> ways = psRoot.query(new CQuery(Way.class));
		System.out.println("Loaded Ways : "+ways.size());
		
		List<Area> areas = psRoot.query(new CQuery(Area.class));
		System.out.println("Loaded Areas : "+areas.size());

		int t_nodes=0;
		int t_ways=0;
		for(Area a:areas)
		{
			t_nodes+=(a.getNodes()!=null?a.getNodes().length:0);
			t_ways+=(a.getWays()!=null?a.getWays().length:0);
			CInstanceMetaObject imo = CPersistentRoot.findMetaObjectInstance(CPersistentRoot.findCachedLogicalObjectID(a));
			System.out.println("Area ("+imo.getClassName()+"  v:"+imo.getClassVersion()+"): "+a.getName()+"  nodes:"+(a.getNodes()!=null?""+a.getNodes().length:"NULL")+"  ways: "+(a.getWays()!=null?""+a.getWays().length:"NULL"));
		}
		System.out.println("Total of Nodes: "+t_nodes+"   total of ways: "+t_ways);
		//Nodes
		for(Area a1:areas)
		{
			if(a1.getNodes()!=null)
			{
				for(Area a2:areas)
				{
					if(a1!=a2)
					{
						int n=0;
						if(a1.getNodes()!=null)
						{
							for(Node n1:a1.getNodes())
							{
								for(Node n2:a2.getNodes())
								{
									if(n1==n2)
									{
										n++;
									}
								}	
							}
						}
						System.out.println(a1.getName()+"/"+a2.getName()+" Share "+n+" nodes");
					}
				}
			}
		}
		//Ways
		for(Area a1:areas)
		{
			if(a1.getWays()!=null)
			{
				for(Area a2:areas)
				{
					if(a1!=a2)
					{
						int n=0;
						if(a1.getWays()!=null)
						{
							for(Way w1:a1.getWays())
							{
								for(Way w2:a2.getWays())
								{
									if(w1==w2)
									{
										n++;
									}
								}	
							}
						}
						System.out.println(a1.getName()+"/"+a2.getName()+" Share "+n+" ways");
					}
				}
			}
		}
		
	}

}
