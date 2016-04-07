package rhp.aof4oop.cs.datamodel;


public class StudentCol
{
	Student[] students;
	
	public StudentCol()
	{
		super();
	}
	public StudentCol( Student[] students)
	{
		this();
		setStudents(students);
	}
	public Student[] getStudents() {
		return students;
	}

	public void setStudents(Student[] students) {
		this.students = students;
	}
}
