package rhp.aof4oop.dataobjects;

public class Student extends Person
{
	private int code;
	
	public Student()
	{
		super();
	}
	public Student(int code,String name,String bi,Address address,int age)
	{
		super(name,bi,address,age);
		this.code = code;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String toString()
	{
		return "Student {"+code+" "+super.toString()+"}";
	}
//	public String sendMail(Enrollment enroll,int to)
//	{
//		System.out.println("enrol="+enroll+"  to="+to);
//		return "ok";
//	}
}
