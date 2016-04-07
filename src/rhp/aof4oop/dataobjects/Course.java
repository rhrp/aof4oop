package rhp.aof4oop.dataobjects;

public class Course 
{
	private String code;
	private String name;
	
	public Course()
	{
		super();
	}
	public Course(String code, String name) {
		super();
		this.code = code;
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String toString()
	{
		return "Course: {"+code+" - "+name+"}";
	}
}
