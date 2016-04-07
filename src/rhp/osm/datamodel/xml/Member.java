package rhp.osm.datamodel.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "A")
@XmlRootElement(name = "member")
public class Member 
{
 	private String type;
	private long ref;
	private String role;
	
	
	public Member() 
	{
		super();
	}
	@XmlAttribute
	public String getType() 
	{
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@XmlAttribute
	public long getRef() {
		return ref;
	}
	public void setRef(long ref) {
		this.ref = ref;
	}
	@XmlAttribute
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
}
