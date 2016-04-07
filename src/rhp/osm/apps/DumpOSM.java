package rhp.osm.apps;


import rhp.osm.datamodel.xml.Node;
import rhp.osm.datamodel.xml.OSMFile;

public class DumpOSM 
{
	public static void main(String[] args) throws Exception
	{
		//String file_name="/home/rhp/workspace/aof4oop/osm_files/porto_cidade.osm";
		String file_name="/home/rhp/workspace/aof4oop/osm_files/Orense.osm";
		//String file_name="zona_casa.osm";
		OSMFile osm=OSMFile.load(file_name);
		
		System.out.println("File: "+file_name);
		System.out.println("Version: "+osm.getVersion());
		System.out.println("Generator: "+osm.getGenerator());
		if(osm.getBounds()!=null)
		{
			System.out.println("  MinLat: "+osm.getBounds().getMinlat());
			System.out.println("  MaxLat: "+osm.getBounds().getMaxlat());
			System.out.println("  MinLon: "+osm.getBounds().getMinlon());
			System.out.println("  MaxLon: "+osm.getBounds().getMaxlon());
		}
		if(osm.getNodes()!=null)
		{
			int c=0;
			System.out.println("  Total of nodes: "+osm.getNodes().length);
			for(Node n:osm.getNodes())
			{
				if(n.getTag()!=null && n.getTag().length>0)
				{
					c++;
				}
			}
			System.out.println("  Total of taged nodes: "+c);
		}
		if(osm.getWays()!=null)
		{
			int w_valid=0;
			int w_invalid=0;
			int w_withoutnodes=0;
			System.out.println("  Total of ways: "+osm.getWays().length);
			for(int i=0;i<osm.getWays().length;i++)
			{
				if(osm.getWays()[i].getNodesRefs()!=null)
				{
					int n=0;
					for(int j=0;j<osm.getWays()[i].getNodesRefs().length;j++)
					{
						if(osm.getWays()[i].getNodesRefs()[j]!=null)
						{
							long ref=osm.getWays()[i].getNodesRefs()[j].getRef();
							if(containsNode(osm.getNodes(),ref))
							{
								n++;
							}
						}
					}
					if(osm.getWays()[i].getNodesRefs().length==n)
					{
						w_valid++;
					}
					else
					{
						w_invalid++;
					}
				}
				else
				{
						w_withoutnodes++;
				}
			}
			System.out.println("  Total ways which nodes are valid: "+w_valid);
			System.out.println("  Total ways which nodes are invalid: "+w_invalid);
			System.out.println("  Total ways without node references: "+w_withoutnodes);
		}
		if(osm.getRelations()!=null)
		{
			int t_members=0;
			int t_tags=0;
			System.out.println("  Total of relations: "+osm.getRelations().length);
			for(int i=0;i<osm.getRelations().length;i++)
			{
				if(osm.getRelations()[i].getMember()!=null)
				{
					t_members+=osm.getRelations()[i].getMember().length;
				}
				if(osm.getRelations()[i].getTag()!=null)
				{
					t_tags+=osm.getRelations()[i].getTag().length;
				}
			}
			System.out.println("  Total members: "+t_members);
			System.out.println("  Total tags: "+t_tags);
		}
	}
	public static boolean containsNode(Node[] nodes,long id)
	{
		for(int i=0;i<nodes.length;i++)
		{
			if(nodes[i].getId()==id)
			{
				return true;
			}
		}
		return false;
	}
}
