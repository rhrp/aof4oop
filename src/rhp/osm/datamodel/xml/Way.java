package rhp.osm.datamodel.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "A")
@XmlRootElement(name = "way")
public class Way 
{
//way id="26659127" user="Masch" uid="55988" visible="true" version="5" changeset="4142606" timestamp="2010-03-16T11:47:08Z"
	private long id;
	private String user;
	private long uid;
	private boolean visible;
	private int version;
	private String changeset;
	private String timestamp;
	private NodeReference[] nodesRefs;
	private Tag[] tags;
	
	@XmlAttribute
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	public String getChangeset() {
		return changeset;
	}
	public void setChangeset(String changeset) {
		this.changeset = changeset;
	}
	@XmlAttribute
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	@XmlElements(value = { @XmlElement(name="nd") })
	public NodeReference[] getNodesRefs() {
		return nodesRefs;
	}
	public void setNodesRefs(NodeReference[] nodesRefs) {
		this.nodesRefs = nodesRefs;
	}
	@XmlElements(value = { @XmlElement(name="tag") })
	public Tag[] getTags() {
		return tags;
	}
	public void setTags(Tag[] tags) {
		this.tags = tags;
	}
}
