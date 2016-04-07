package rhp.aof4oop.apps.openstreetmap;


//Version C
import org.openstreetmap.gui.jmapviewer.Coordinate;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;
import rhp.osm.datamodel.xml.Node;
import rhp.osm.datamodel.xml.Relation;
import rhp.osm.datamodel.xml.Way;

/**
 * 
 * @author rhp
 *
 */
@Aof4oopVersionAlias(alias = "C")
public class Area
{
	private String name;
	//Version A
//	private float minlat;
//	private float minlon;
//	private float maxlat;
//	private float maxlon;
	//Version B
//	private Coordinate[] bounds;
	//Version C
	private org.openstreetmap.gui.jmapviewer.Coordinate[] bounds;

	private Node[] nodes;
	private Way[] ways;
	private Relation[] relations;
	
	public Area() 
	{
		super();
	}
	public Area(String name,float minlat, float minlon, float maxlat, float maxlon,Node[] nodes,Way[] ways,Relation[] relations) 
	{
		super();
		this.name=name;
		this.nodes = nodes;
		this.ways=ways;
		this.relations=relations;
		//Version A
//		this.minlat = minlat;
//		this.minlon = minlon;
//		this.maxlat = maxlat;
//		this.maxlon = maxlon;

		//Version B C
		this.bounds=new Coordinate[]{new Coordinate(maxlat,minlon),new Coordinate(maxlat,maxlon),new Coordinate(minlat,maxlon),new Coordinate(minlat,minlon)};
	}
	
	public void setName(String name) 
	{
		this.name = name;
	}
	public String getName()
	{
		return name;
	}
	//Version A
//	public float getMinlat() {
//		return minlat;
//	}
//	public float getMinlon() {
//		return minlon;
//	}
//	public float getMaxlat() {
//		return maxlat;
//	}
//	public float getMaxlon() {
//		return maxlon;
//	}
//	public void setMinlat(float minlat) 
//	{
//		this.minlat = minlat;
//	}
//	public void setMinlon(float minlon) 
//	{
//		this.minlon = minlon;
//	}
//	public void setMaxlat(float maxlat) 
//	{
//		this.maxlat = maxlat;
//	}
//	public void setMaxlon(float maxlon) 
//	{
//		this.maxlon = maxlon;
//	}
	//Version B C
	public Coordinate[] getBounds() 
	{
		return bounds;
	}
	public void setBounds(Coordinate[] bounds) 
	{
		this.bounds=bounds;
	}
	public Node[] getNodes() {
		return nodes;
	}
	public Way[] getWays() {
		return ways;
	}
	public Relation[] getRelations()
	{
		return relations;
	}
	public void setRelations(Relation[] relations)
	{
		this.relations=relations;
	}
	public String toString()
	{
		if(name==null)
		{
			//Version A
//			return "("+getMinlat()+","+getMinlon()+")("+getMaxlat()+","+getMaxlon()+")";
			//Version B C
			return "("+(getBounds()!=null?getBounds().length:"NULL")+")";
		}
		else
		{
			return name;
		}
	}
	public String calcKey()
	{
		if(name==null)
		{
			//Version A
//			return "("+getMinlat()+","+getMinlon()+")("+getMaxlat()+","+getMaxlon()+") Version A";
			//Version B
//			return "("+(getBounds()!=null?getBounds().length:"NULL")+") Version B";
			//Version C
			return "("+(getBounds()!=null?getBounds().length:"NULL")+") Version C";

		}
		else
		{
//			return name+" (Version A)";
//			return name+" (Version B)";
			return name+" (Version C)";
		}
	}
}
