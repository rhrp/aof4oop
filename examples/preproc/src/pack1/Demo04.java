package pack1;

import rhp.aof4oop.dataobjects.Enrollment;
import rhp.aof4oop.dataobjects.Student;
import rhp.aof4oop.framework.core.CPersistentRoot;

public class Demo04 
{
	CPersistentRoot psRootGlobal;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		// run one at a time
		CPersistentRoot psRoot=new CPersistentRoot();

		// run one at a time
		CPersistentRoot psRoot1;
		psRoot1=new CPersistentRoot();

		//Simple case of inference
		Student student;
		//Student student=psRoot.getRootObject("Rui");
		student=psRoot.getRootObject("Rui");
		System.out.println("Name: "+student.getName());

		// Get the Enrollment reference and invoke methods
		Enrollment enrollment=psRoot.getRootObject("enroll_Rui_AI");
		System.out.println("Name: "+enrollment.getStudent().getName());

		// In this case, inexplicably, the cast is not applied
		// The argument is an enrollment, so the cast should be applied similarly as above 
		// The current version of precomp still does not suport this case
		//showStudentNameEnrollment(psRoot.getRootObject("enroll_Rui_AI"));
		//TODO: showStudentNameEnrollment(psRoot.getRootObject("enroll_Rui_AI"));
		
        CPersistentRoot anotherPsRoot=new CPersistentRoot();

		// Using reflection to invoke methods on unknown data types
		// At compile-time the data type is unknown (Generic type) the compiler dont allow 
		//System.out.println("Name: "+psRoot.getRootObject("enroll_Rui_AI").getStudent().getName());
		System.out.println("Name: "+psRoot.getRootObject("enroll_Rui_AI").getStudent().getName());

		//Send by e-mail a enrollment notification
		//psRoot1.getRootObject("enroll_Rui_AI").getStudent().sendMail(enrollment,16);
		psRoot1.getRootObject("enroll_Rui_AI").getStudent().sendMail(enrollment,16);

		
		//student=anotherPsRoot.getRootObject("enroll_Rui_AI").getStudent();
        student=anotherPsRoot.getRootObject("enroll_Rui_AI").getStudent();
        
        
        
        anotherPsRoot.dumpCache();

	}
	public void method  (CPersistentRoot psRootArg) throws Exception
	{
		int a;
		//System.out.println("Name: "+psRootGlobal.getRootObject("enroll_Rui_AI").getStudent().getName());
		System.out.println("Name: "+psRootGlobal.getRootObject("enroll_Rui_AI").getStudent().getName());
	}
	public void anotherMethod(CPersistentRoot psRoot1,float[] aaa) throws Exception
	{
		Enrollment enrollment=null;
		Student student=null;
float[] bbb={1,2,3};
		//psRoot1.getRootObject("enroll_Rui_AI").getStudent().sendMail(enrollment,15,student);
		psRoot1.getRootObject("enroll_Rui_AI").getStudent().sendMail(enrollment,15,student);
	}
	public void oneMoreMethod(CPersistentRoot psRoot1,Enrollment enrollment,Student student) throws Exception
	{
		//psRoot1.getRootObject("enroll_Rui_AI").getStudent().sendMail(enrollment,15,student);
		psRoot1.getRootObject("enroll_Rui_AI").getStudent().sendMail(enrollment,15,student);
	}
}
