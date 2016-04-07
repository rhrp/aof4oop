package rhp.aof4oop.cs.datamodel;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "A")
public class Person 
{
	private String surname;
	private String firstName;
	private String title;
	private String address;
	private String postCode;
	private String telephoneNumber;
	private String faxNumber;
	private String mobileNumber;
	
	private boolean passedD32Qualification;
	private boolean passedD34Qualification;
	private boolean passedD36Qualification;
	
	public Person()
	{
		super();
	}

	public Person(String surname, String firstName, String title,
			String address, String postCode, String telephoneNumber,
			String faxNumber, String mobileNumber,
			boolean passedD32Qualification, boolean passedD34Qualification,
			boolean passedD36Qualification) {
		super();
		this.surname = surname;
		this.firstName = firstName;
		this.title = title;
		this.address = address;
		this.postCode = postCode;
		this.telephoneNumber = telephoneNumber;
		this.faxNumber = faxNumber;
		this.mobileNumber = mobileNumber;
		this.passedD32Qualification = passedD32Qualification;
		this.passedD34Qualification = passedD34Qualification;
		this.passedD36Qualification = passedD36Qualification;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public String getFaxNumber() {
		return faxNumber;
	}

	public void setFaxNumber(String faxNumber) {
		this.faxNumber = faxNumber;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public boolean isPassedD32Qualification() {
		return passedD32Qualification;
	}

	public void setPassedD32Qualification(boolean passedD32Qualification) {
		this.passedD32Qualification = passedD32Qualification;
	}

	public boolean isPassedD34Qualification() {
		return passedD34Qualification;
	}

	public void setPassedD34Qualification(boolean passedD34Qualification) {
		this.passedD34Qualification = passedD34Qualification;
	}

	public boolean isPassedD36Qualification() {
		return passedD36Qualification;
	}

	public void setPassedD36Qualification(boolean passedD36Qualification) {
		this.passedD36Qualification = passedD36Qualification;
	}
	
}
