package rhp.aof4oop.apps.openstreetmap;

import java.awt.geom.Point2D;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;


/**
 * 
 * @author rhp
 *
 */
@Aof4oopVersionAlias(alias = "A")
public class Coordinate implements ICoordinate
{
	public Point2D.Double data;

	public Coordinate() 
	{
		super();
	}
	public Coordinate(double lat, double lon) 
	{
		super();
		data = new Point2D.Double(lat,lon);
	}
	public double getLat() 
	{
		return data.x;
	}
	public void setLat(double lat) 
	{
		this.data.x=lat;
	}
	public double getLon() 
	{
		return data.y;
	}
	public void setLon(double lon) 
	{
		this.data.x=lon;
	}
	
	
}
