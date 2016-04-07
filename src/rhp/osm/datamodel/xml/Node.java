package rhp.osm.datamodel.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "A")
@XmlRootElement(name = "node")
public class Node 
{
	//node id="298884269" lat="54.0901746" lon="12.2482632" user="SvenHRO" uid="46882" visible="true" version="1" changeset="676636" timestamp="2008-09-21T21:37:45Z"/>

	private long id;
	private float	lat;
	private float	lon;
	private String user;
	private long	uid;
	private boolean visible;
	private int version;
	private long changeset;
	private String timestamp;
	private Tag[] tag;
	
	
	@XmlAttribute
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@XmlAttribute
	public float getLat() {
		return lat;
	}
	public void setLat(float lat) {
		this.lat = lat;
	}
	@XmlAttribute
	public float getLon() {
		return lon;
	}
	public void setLon(float lon) {
		this.lon = lon;
	}
	@XmlAttribute
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	@XmlAttribute
	public long getUid() {
		return uid;
	}
	public void setUid(long uid) {
		this.uid = uid;
	}
	@XmlAttribute
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	@XmlAttribute
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	@XmlAttribute
	public long getChangeset() {
		return changeset;
	}
	public void setChangeset(long changeset) {
		this.changeset = changeset;
	}
	@XmlAttribute
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	@XmlElements(value = { @XmlElement(name="tag") })
	public Tag[] getTag() {
		return tag;
	}
	public void setTag(Tag tag[]) {
		this.tag = tag;
	}	
}
