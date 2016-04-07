package rhp.aof4oop.cs.utils;

import java.util.ArrayList;
import java.util.Arrays;

import rhp.aof4oop.cs.datamodel.Person;
import rhp.aof4oop.cs.datamodel.Student;
import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "B")
public class StudentCol 
{
	Student[] students;

		public StudentCol() 
		{
			super();
			this.students = new Student[0];
		}
		public StudentCol(Student[] students) 
		{
			super();
			this.students = students;
		}
		public StudentCol(ArrayList<Student> students)
		{
			this.students=new Student[students.size()];
			int i=0;
			for(Student s:students)
			{
				this.students[i]=s;
				i++;
			}
		}
		public Student[] getStudents() 
		{
			return students;
		}

		public void setPersons(Student[] students) 
		{
			this.students = students;
		}
		public Student get(int i)
		{
			return this.students[i];
		}
		public void set(int i,Student student)
		{
			Student[] tmp=this.students;
			tmp[i]=student;
			System.out.println("set "+i+" "+student.getFirstName());
			this.students=tmp;
		}
		public int size()
		{
			return students.length;
		}
		public void add(Student new_student)
		{
			Student[] tmp = Arrays.copyOf(this.students, this.students.length + 1);
			tmp[size()]=new_student;
			//System.out.println("set "+size()+" "+new_student.getFirstName());
			this.students=tmp;
		}
		public void remove(int index)
		{
			if(index<0 || index>=this.students.length)
			{
				throw new IllegalArgumentException("The index is out of range");
			}
			Student[] tmp = new Student[this.students.length-1];
			int n=0;
			for(int i=0;i<this.students.length;i++)
			{
				if(index!=i)
				{
					tmp[n]=this.students[i];
					n++;
				}
			}
			this.students=tmp;
		}
	}
