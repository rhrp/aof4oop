package rhp.aof4oop.oo7.datamodel;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "oo7")
public class DesignObject
{
	private int id;
	private String type;
	private long	buildDate;
	
	public DesignObject()
	{
		super();
	}
	public DesignObject(int id, String type, long buildDate) 
	{
		super();
		this.id = id;
		this.type = type;
		this.buildDate = buildDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getBuildDate() {
		return buildDate;
	}

	public void setBuildDate(long buildDate) {
		this.buildDate = buildDate;
	}
	
	
}
