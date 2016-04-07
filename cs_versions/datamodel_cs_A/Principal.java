package rhp.aof4oop.cs.datamodel;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "A")
public class Principal extends Person 
{
	public Principal()
	{
		super();
	}
	public Principal(String surname, String firstName, String title,
			String address, String postCode, String telephoneNumber,
			String faxNumber, String mobileNumber,
			boolean passedD32Qualification, boolean passedD34Qualification,
			boolean passedD36Qualification) {
		super(surname, firstName, title, address, postCode, telephoneNumber, faxNumber,mobileNumber, passedD32Qualification, passedD34Qualification,passedD36Qualification);
	}
	
}
