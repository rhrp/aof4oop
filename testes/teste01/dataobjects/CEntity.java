package rhp.aof4oop.dataobjects;

public class CEntity extends CMyObjecto
{
	private String name;
	private int aaa;


	public CEntity() 
	{
		super();
	}
	public CEntity(String name) {
		super();
		this.name = name;
	}

	public String getName() 
	{
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
