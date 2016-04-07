package rhp.aof4oop.cs.datamodel;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "B")
public class Person 
{
	private String firstName;
	private String lastName;
	private String middleName;
	
	public Person()
	{
		super();
	}

	public Person(String firstName, String middleName,String lastName) 
	{
		super();
		this.lastName = lastName;
		this.middleName = middleName;
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

}
