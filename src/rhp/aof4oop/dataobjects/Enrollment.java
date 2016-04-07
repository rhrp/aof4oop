package rhp.aof4oop.dataobjects;

public class Enrollment 
{
	private Student student;
	private Course course;
	private int grade;
	
	public Enrollment(Student student, Course course) 
	{
		super();
		this.student = student;
		this.course = course;
		this.grade = -1;
	}
	public Student getStudent() {
		return student;
	}
	public void setStudent(Student student) {
		this.student = student;
	}
	public Course getCourse() {
		return course;
	}
	public void setCourse(Course course) {
		this.course = course;
	}
	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}
	public String toString()
	{
		return "Enrollment{"+student+" "+course+"  Grade:"+grade+"}";
	}
}
