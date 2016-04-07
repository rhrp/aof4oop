package rhp.aof4oop.apps.demo;

import java.util.Date;
import java.util.List;

import rhp.aof4oop.dataobjects.Address;
import rhp.aof4oop.dataobjects.Family;
import rhp.aof4oop.dataobjects.Person;
import rhp.aof4oop.framework.core.CPersistentRoot;
import rhp.aof4oop.framework.core.CQuery;


public class TestBigDB 
{
	public static void main(String[] args) throws Exception 
	{
		createDB();
	}

	//
	private static void createDB()
	{
		CPersistentRoot psRoot;
		int dbSize=100;
		
		psRoot=new CPersistentRoot();
		for(int i=0;i<dbSize;i++)
		{
			Family family;
			Address addr;
			
			addr=new Address("Rua X",1);
			family=new Family();
			family.setFather(new Person("Rui","12345",addr,40));
			family.setMother(new Person("Cristina","12345",addr,40));
			family.setWeddingDate(new Date());
			psRoot.setRootObject(""+System.currentTimeMillis(),family);
			System.out.println(i);
		}
	}
	private static void showDB()
	{
		CPersistentRoot psRoot;
		
		psRoot=new CPersistentRoot();
		
		List<Family> families=psRoot.query(new CQuery(Family.class));
		System.out.println("families="+families.size());
		for(Family f:families)
		{
			System.out.println("Family: "+f.getFather().getName());
		}
	}
	private static void showOneFamily()
	{
		CPersistentRoot psRoot = new CPersistentRoot();
		
		Family  family=psRoot.getRootObject("1335045189504");
		System.out.println("Family: "+family.getFather().getName());	
		System.out.println("        "+family.getFather().getAddress().getMorada());	
	}
}
