package rhp.osm.datamodel.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "A")
@XmlRootElement(name = "relation")
public class Relation 
{
	private long id;
	private boolean visible;
	private int version;
	private long changeset;
	private String timestamp;
	private String user;
	private long uid;
	private Member[] member;
	private Tag tag[];
	
	public Relation() 
	{
		super();
		// TODO Auto-generated constructor stub
	}
	
	@XmlAttribute
	public long getId() 
	{
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	@XmlElements(value = {@XmlElement(name="member")})
	public Member[] getMember() {
		return member;
	}
	public void setMember(Member[] member) {
		this.member = member;
	}
	@XmlElements(value = {@XmlElement(name="tag")})
	public Tag[] getTag() {
		return tag;
	}
	public void setTag(Tag[] tag) {
		this.tag = tag;
	}
	
	
}
