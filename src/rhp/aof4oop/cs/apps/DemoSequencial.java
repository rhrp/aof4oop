package rhp.aof4oop.cs.apps;

import java.util.List;

import rhp.aof4oop.framework.core.CPersistentRoot;
import rhp.aof4oop.framework.core.CQuery;
import rhp.aof4oop.framework.core.datamodel.CInstanceMetaObject;
import rhp.aof4oop.cs.datamodel.Principal;
import rhp.aof4oop.cs.datamodel.Student;

/**
 * Demo application that shows how an application in three different versions can can share just one database in one of those versions.
 * In first step the database is created at version A (ran createDB() with symbolic link to datamodel_cs_A)
 * After that, half database is updated to version B  (ran updateHalfDB() and change symbolic link to datamodel_cs_B)
 * At third step all database is read by a version C application
 * To achieve this update/backdate application compatibility three ubmo meta-objects are needed. 
 * @author rhp
 *
 */
public class DemoSequencial 
{

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		//createDB_VersionA();			//All DB on version A (use datamodel_cs_A)
		//readDB_VersionA();
		
		//updateHalfDB();		//Half database on version A and the other on B (use datamodel_cs_B)
		readDB_VersionB();
		
		//readDB_VersionC();				//read database (use datamodel_cs_C)
		
		CPersistentRoot.printStats();
	}
//	/**
//	 * Used to create database with object at version A
//	 */
//	private static void createDB_VersionA()
//	{
//		CPersistentRoot ps=new CPersistentRoot();
//		Student[] ss=new Student[3000];
//		for(int i=0;i<ss.length;i++)
//		{
//			Student s=new Student();
//			s.setFirstName("firstname"+i);
//			s.setLastName("lastName"+i);
//			s.setMiddleName("middleName"+i);
//			s.setSex(i%2==0?"M":"F");
//			s.setBirth(null);
//			ss[i]=s;
//			System.out.println("new student "+i);
//		}
//		ps.setRootObject("students",new StudentCol(ss));
//		
//		Principal[] pp=new Principal[20];
//		for(int i=0;i<pp.length;i++)
//		{
//			Principal p=new Principal();
//			p.setFirstName("firstname"+i);
//			p.setSurname("surname"+i);
//			pp[i]=p;
//			System.out.println("new Principal "+i);
//		}
//		ps.setRootObject("principals",new PrincipalCol(pp));
//	}
//	/**
//	 * Read data base with application's schema at version A 
//	 */
//	private static void readDB_VersionA()
//	{
//		CPersistentRoot ps=new CPersistentRoot();
//		List<Student> students=ps.query(new CQuery(Student.class));
//		for(Student s:students)
//		{
//			System.out.println("Student: "+s.getFirstName()+" "+s.getSex());
//		}
//		List<Principal> principals=ps.query(new CQuery(Principal.class));
//		for(Principal p:principals)
//		{
//			//At version C
//			long loid=CPersistentRoot.findCachedLogicalObjectID(p);
//			CInstanceMetaObject imo=CPersistentRoot.findCachedMetaObjectInstance(loid);
//			String v=(imo!=null?imo.getClassVersion():"?");
//			System.out.println("Principal: "+p.getFirstName()+"  "+p.getSurname());
//		}
//	}
	
	
//	/**
//	 * Used to update half of database's objects with structure of B version
//	 */
//	public static void updateHalfDB()
//	{
//		int n;
//		CPersistentRoot ps=new CPersistentRoot();
//		List<Student> students=ps.query(new CQuery(Student.class));
//		n=0;
//		for(Student s:students)
//		{
//			if(n%2==0)
//			{
//				s.setBirth(new java.util.Date());
//			}
//			System.out.println("Student: "+s.getFirstName()+" "+s.getSex());
//			n++;
//		}
//		n=0;
//		List<Principal> principals=ps.query(new CQuery(Principal.class));
//		for(Principal p:principals)
//		{
//			if(n%2==0)
//			{
//				p.setAddress("address "+p.getFirstName());
//			}
//			System.out.println("Principal: "+p.getFirstName()+"  "+p.getMiddleName()+"   "+p.getLastName());
//			n++;
//		}
//	}
	/**
	 * Read data base with application's schema at version B
	 */
	private static void readDB_VersionB()
	{
		CPersistentRoot ps=new CPersistentRoot();
		List<Student> students=ps.query(new CQuery(Student.class));
		for(Student s:students)
		{
			System.out.println("Student: "+s.getFirstName()+" "+s.getSex());
		}
		List<Principal> principals=ps.query(new CQuery(Principal.class));
		for(Principal p:principals)
		{
			//At version C
			long loid=CPersistentRoot.findCachedLogicalObjectID(p);
			CInstanceMetaObject imo=CPersistentRoot.findCachedMetaObjectInstance(loid);
			String v=(imo!=null?imo.getClassVersion():"?");
			//System.out.println("Principal: "+p.getFirstName()+"  "+p.getMiddleName()+" "+p.getLastName());
		}
	}
//	/**
//	 * Read data base with application's schema at version C 
//	 */
//	public static void readDB_VersionC()
//	{
//		CPersistentRoot ps=new CPersistentRoot();
//		List<Student> students=ps.query(new CQuery(Student.class));
//		for(Student s:students)
//		{
//			long loid=CPersistentRoot.findCachedLogicalObjectID(s);
//			CInstanceMetaObject imo=CPersistentRoot.findCachedMetaObjectInstance(loid);
//			String v=(imo!=null?imo.getClassVersion():"?");
//
//			System.out.println("Student(LOID:"+loid+" version:"+v+"): "+s.getFirstName()+" "+s.getSex());
//		}
//		List<Principal> principals=ps.query(new CQuery(Principal.class));
//		for(Principal p:principals)
//		{
//			//At version C
//			long loid=CPersistentRoot.findCachedLogicalObjectID(p);
//			CInstanceMetaObject imo=CPersistentRoot.findCachedMetaObjectInstance(loid);
//			String v=(imo!=null?imo.getClassVersion():"?");
//			System.out.println("Principal(LOID:"+loid+" version:"+v+"): "+p.getFirstName()+"  "+p.getMiddleName()+"   "+p.getLastName()+"   email:"+p.getEmail());
//		}
//	}

}
