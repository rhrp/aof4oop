package rhp.aof4oop.cs.datamodel;

import java.util.Date;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "B")
public class Student extends Person
{
	private Date birth;
	private String Sex;
	
	public Student()
	{
		super();
	}
	
	public Student(String firstName, String middleName, String lastName,Date birth,String sex) 
	{
		super(firstName, middleName, lastName);
		setBirth(birth);
		setSex(sex);
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
