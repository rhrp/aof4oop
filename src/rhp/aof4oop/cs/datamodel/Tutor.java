package rhp.aof4oop.cs.datamodel;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "B")
public class Tutor extends Staff
{
	public Tutor()
	{
		super();
	}

	public Tutor(String title, String firstName, String middleName,
			String lastName, String address, String postCode,
			String telephoneNumber, String faxNumber, String mobileNumber,
			boolean passedD32Qualification, boolean passedD34Qualification,
			boolean passedD36Qualification) {
		super(title, firstName, middleName, lastName, address, postCode,
				telephoneNumber, faxNumber, mobileNumber, passedD32Qualification,
				passedD34Qualification, passedD36Qualification);
		// TODO Auto-generated constructor stub
	}
	
}
