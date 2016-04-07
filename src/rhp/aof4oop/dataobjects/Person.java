package rhp.aof4oop.dataobjects;

import java.util.Date;

public class Person extends CEntity
{
//	private String name;
	private String bi;
	private Address address;
	private int age;
	private Date birth;

	public Person()
	{
		super();
	}
	public Person(String name, String bi, Address address, int age)
	{
		super(name);
//		this.name = name;
		this.bi = bi;
		this.address = address;
		this.age = age;
	}
//	public String getName() {
//		return name;
//	}
//	public void setName(String name) {
//		this.name = name;
//	}
	public String getBi() {
		return bi;
	}
	public void setBi(String bi) {
		this.bi = bi;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public Date getBirth() {
		return birth;
	}
	public void setBirth(Date birth) {
		this.birth = birth;
	}
	public String toString()
	{
		return "Person:  {Name:"+getName()+" Age:"+getAge()+" "+address+" birth:"+birth+"}";
	}
}
