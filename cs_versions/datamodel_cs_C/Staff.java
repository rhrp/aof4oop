package rhp.aof4oop.cs.datamodel;

public class Staff extends Person
{
	private String title;
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
