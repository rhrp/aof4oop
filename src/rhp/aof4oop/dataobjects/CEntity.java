package rhp.aof4oop.dataobjects;


import rhp.aof4oop.framework.core.annotations.Aof4oopNotNull;
import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "listWithAddress")
public class CEntity extends CMyObjecto
{
	@Aof4oopNotNull(message="The entity name canot be null")
	private String name;

	/**
	 * @
	 */
	public CEntity() 
	{
		super();
	}
	public CEntity(String name) 
	{
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


