package rhp.aof4oop.apps.demo;

import rhp.aof4oop.dataobjects.Course;
import rhp.aof4oop.dataobjects.Degree;
import rhp.aof4oop.dataobjects.Enrollment;
import rhp.aof4oop.dataobjects.Student;
import rhp.aof4oop.framework.core.CPersistentRoot;



public class Demo03 {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// run one at a time
		//program01();
		//program02();
		program03();
	}
	/**
	 * Create a school database
	 * The reference to the Computer Science degree it is lost.
	 */
	public static void program01()
	{
		CPersistentRoot psRoot=new CPersistentRoot();

		Degree computerScience=new Degree("01","Computer Science");
		computerScience.setCourses(new Course[]{new Course("MAT","Mathematics"),new Course("AI","Artificial Intelligence"),new Course("OT","Object Technology")});
		Student ana=new Student(1,"Ana",null,null,20);
		Student rui=new Student(2,"Rui",null,null,25);
		
		psRoot.setRootObject("enroll_Ana_MAT",new Enrollment(ana,computerScience.getCourses()[0]));
		psRoot.setRootObject("enroll_Ana_AI",new Enrollment(ana,computerScience.getCourses()[1]));
		psRoot.setRootObject("enroll_Ana_OT",new Enrollment(ana,computerScience.getCourses()[2]));
		
		psRoot.setRootObject("enroll_Rui_AI",new Enrollment(rui,computerScience.getCourses()[1]));
		
		psRoot.dumpCache();
	}
	/**
	 * Get a complete list of students
	 */
	public static void program02()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		System.out.println("The cache on the start");
		psRoot.dumpCache();
		
		System.out.println("List of students");
		for(Object e:psRoot.getRootObjects().values())
		{
			if(e instanceof Enrollment)
			{
				System.out.printf("%s\t%s\n",((Enrollment)e).getStudent().getName(),((Enrollment)e).getCourse().getName());
			}
		}
		
		System.out.println("The cache after");
		psRoot.dumpCache();
	}
	public static void program03()
	{

		CPersistentRoot psRoot=new CPersistentRoot();

		// Get the Enrollment reference and invoke methods
		Enrollment enrollment=psRoot.getRootObject("enroll_Rui_AI");
		System.out.println("Name: "+enrollment.getStudent().getName());
		
		// In this case, inexplicably, the cast is not applied
		// The argument is an enrollment, so the cast should be applied similarly as above 
		//showStudentNameEnrollment(psRoot.getRootObject("enroll_Rui_AI"));
		
		// Using reflection to invoke methods on unknown data types
		// At compile-time the data type is unknown (Generic type) the compiler dont allow 
		// System.out.println("Name: "+psRoot.getRootObject("enroll_Rui_AI").getStudent().getName());
		try
		{
			System.out.println("Using reflection: get enroll");
			Object oEnroll=psRoot.getRootObject("enroll_Rui_AI");
			System.out.println("Using reflection: get setudent");
			Object oStudent=oEnroll.getClass().getMethod("getStudent").invoke(oEnroll,(Object[])null);
			System.out.println("Using reflection: get name");
			Object oString=oStudent.getClass().getMethod("getName").invoke(oStudent,(Object[])null);
			System.out.println("Name: "+oString);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	
		// Student "rui" also have a pointer from root
		Student student=psRoot.getRootObject("rui");
		System.out.println("Name: "+student.getName());

		//Check if the two instances of the Student object are the same
		System.out.println("enrollment.getStudent() and student are the same object instance :"+(enrollment.getStudent()==student));
	}
	public static void showStudentNameEnrollment(Enrollment enroll)
	{
		System.out.println("Name: "+enroll.getStudent().getName());
	}
}
