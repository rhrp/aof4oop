package rhp.aof4oop.cs.datamodel;

import java.util.Date;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "A")
public class Student 
{
	private	String lastName;
	private String firstName;
	private String middleName;
	private Date birth;
	private String Sex;
	
	public Student()
	{
		super();
	}
	public Student(String firstName, String middleName, String lastName,Date birth,String sex) 
	{
		setFirstName(firstName);
		setMiddleName(middleName);
		setLastName(lastName);
		setBirth(birth);
		setSex(sex);
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

	public Date getBirth() {
		return birth;
	}

	public void setBirth(Date birth) {
		this.birth = birth;
	}

	public String getSex() {
		return Sex;
	}

	public void setSex(String sex) {
		Sex = sex;
	}
	
}
