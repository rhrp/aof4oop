package rhp.aof4oop.cs.datamodel;


import rhp.aof4oop.framework.core.annotations.Aof4oopNotNull;
import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "B")
public class Staff extends Person
{
	private String title;
	@Aof4oopNotNull(message="The address is mandatory")
	private String address;
	private String postCode;
	private String telephoneNumber;
	private String faxNumber;
	private String mobileNumber;
	private boolean passedD32Qualification;
	private boolean passedD34Qualification;
	private boolean passedD36Qualification;
	
	public Staff()
	{
		super();
	}
	
	public Staff(String title,String firstName,String middleName,String lastName,
			String address, String postCode,
			String telephoneNumber, String faxNumber,String mobileNumber,
			boolean passedD32Qualification, boolean passedD34Qualification,
			boolean passedD36Qualification) {
		super(firstName,middleName,lastName);
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
