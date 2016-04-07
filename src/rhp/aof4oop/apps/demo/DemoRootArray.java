package rhp.aof4oop.apps.demo;


import rhp.aof4oop.dataobjects.Address;
import rhp.aof4oop.dataobjects.Person;
import rhp.aof4oop.framework.core.CPersistentRoot;

public class DemoRootArray 
{
	public static void main(String[] args) throws Exception 
	{
		showArray();
	}
	
	private static void showArray()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		Person[] persons=psRoot.getRootObject("array_of_persons");
		
		if(persons==null)
		{
			System.out.println("The array does not exists");
			createDB();
		}
		else
		{
			System.out.println("The array exists");
			for(Person p:persons)
			{
				System.out.println("   "+p.getName()+" age "+p.getAge()+"   address="+(p.getAddress()==null?"none":p.getAddress().getMorada()+", "+p.getAddress().getNumero()));
			}
		}
	}
	
	private static void createDB()
	{
		Person[] persons=new Person[10];
		
		for(int i=0;i<persons.length;i++)
		{
			persons[i]=new Person("name#"+i,"bi#"+i,null,20+i);
			persons[i].setAddress(new Address("Street foo",123+i));
		}
		
		CPersistentRoot psRoot=new CPersistentRoot();
		
		psRoot.setRootObject("array_of_persons",persons);
	}
}
