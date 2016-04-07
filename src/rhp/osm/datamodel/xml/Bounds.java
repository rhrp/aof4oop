package rhp.osm.datamodel.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "bounds")
public class Bounds 
{
	float minlat;
	float minlon;
	float maxlat;
	float maxlon;
	
	@XmlAttribute
	public float getMinlat() {
		return minlat;
	}
	public void setMinlat(float minlat) {
		this.minlat = minlat;
	}
	@XmlAttribute
	public float getMinlon() {
		return minlon;
	}
	public void setMinlon(float minlon) {
		this.minlon = minlon;
	}
	@XmlAttribute
	public float getMaxlat() {
		return maxlat;
	}
	public void setMaxlat(float maxlat) {
		this.maxlat = maxlat;
	}
	@XmlAttribute
	public float getMaxlon() {
		return maxlon;
	}
	public void setMaxlon(float maxlon) {
		this.maxlon = maxlon;
	}

	
}
