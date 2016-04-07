package rhp.aof4oop.apps.demo;

/**
 * This demo shows several type of operations: creation, updating and deleting objects
 */
import rhp.aof4oop.dataobjects.Address;
import rhp.aof4oop.dataobjects.Family;
import rhp.aof4oop.dataobjects.Person;
import rhp.aof4oop.framework.core.CPersistentRoot;

public class Demo01 {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception 
	{
		// run sequentially program01 to program08, one at a time
		programTransctions01();
		//programShowDataBase();// shows the database content
		//CPersistentRoot.dumpCache();
		
	}

	/**
	 * Create root object
	 */
	public static void program01()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		System.out.println("Create root object");
		Family family=new Family();
		family.setName("Pereira");
		psRoot.setRootObject(family);	
		showFamily(family);
	}
	/**
	 * Add the father and the mother
	 */
	public static void program02()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		System.out.println("Add the father and the mother");
		Family family;
		family=(Family)psRoot.getRootObject();
		
		Person father=new Person();
		father.setName("Rui");
		family.setFather(father);
		
		Person mother=new Person();
		mother.setName("Isabel");
		family.setMother(mother);
		
		showFamily(family);
	}
	/**
	 * Set the father age
	 */
	public static void program03()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		System.out.println("Set the father age");
		Family family;
		family=psRoot.getRootObject();
		
		family.getFather().setAge(39);
		
		showFamily(family);
	}
	/**
	 * Set the mother address 
	 */
	public static void program04()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		System.out.println("Set the mother address ");
		Family family;
		family=psRoot.getRootObject();
		
		family.getMother().setAddress(new Address("Rua xpto",526));
		
		showFamily(family);
	}
	/**
	 * Add two childs
	 */
	public static void program05()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		System.out.println("add two childs");
		Family family;
		family=(Family)psRoot.getRootObject();
		
		family.setChilds(new Person[]{new Person("Ana",null,null,9),new Person("Inês",null,null,1)});
		
		showFamily(family);
	}
	/**
	 * Set the same address to all family
	 */
	public static void program06()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		System.out.println("Set the same address to all family");
		Family family;
		family=psRoot.getRootObject();
		
		family.getFather().setAddress(family.getMother().getAddress());
		for(Person p:family.getChilds())
		{
			p.setAddress(family.getFather().getAddress());
		}
		showFamily(family);
	}
	/**
	 * Delete the father
	 */
	public static void program07()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		System.out.println("Delete the father");
		Family family;
		family=psRoot.getRootObject();
		
		family.setFather(null);
		
		showFamily(family);
	}
	/**
	 * add a father in two steps
	 */
	public static void program08()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		System.out.println("Add a new father with no name and, following that, set the correct name");
		Family family;
		family=psRoot.getRootObject();
		
		family.setFather(new Person("no name",null,family.getMother().getAddress(),39));// add a father without name
		family.getFather().setName("Rui"); // sets the correct name
		
		showFamily(family);
	}
	/**
	 * This phase only shows the family object content 
	 */
	public static void programShowDataBase()
	{
		CPersistentRoot psRoot;
		psRoot=new CPersistentRoot();
		Family family=psRoot.getRootObject();
		showFamily(family);
	}
	public static void programTransctions01()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		Family family;
		family=psRoot.getRootObject();
		
		Family family2=new Family();
		family2.setFather(new Person("Rui","bi",null,43));
		

		
//		System.out.println("ID:"+System.identityHashCode(family));
//		System.out.println("Persistent:"+CPersistentRoot.isPersistent(family));
//		System.out.println("cached:"+CPersistentRoot.isCached(family));
//		System.out.println("Reachable:"+CPersistentRoot.isReachable(family));
//		System.out.println("add two childs to "+family.getName()+"  childs:"+(family.getChilds()!=null?family.getChilds().length:"null"));


		Person child1=family.getChilds()[0];
		System.out.println("child1 ID:"+System.identityHashCode(child1));		
		CPersistentRoot.beginTransaction();

		try
		{
			System.out.println("Initial state: father's age:"+family.getFather().getAge()+"    child1's name="+child1.getName()+" age="+child1.getAge()+"  child1 ID:"+System.identityHashCode(child1));
			
			System.out.println("Set "+child1.getName()+"'s name as Ana Pereira and increments his age, as well father's age");
			child1.setName("Ana Pereira");
			child1.setAge(child1.getAge()+1);
			family.getFather().setAge(child1.getAge()+30);
			System.out.println("Two steps before the error: father's age:"+family.getFather().getAge()+" name="+child1.getName()+" child1's age="+child1.getAge()+"  child1 ID:"+System.identityHashCode(child1));
			
			System.out.println("Replace childs as "+child1.getName()+"'s name and age as Ana and 9");
			family.setChilds(new Person[]{new Person("Ana",null,null,9),new Person("Inês",null,null,1)});
			child1=family.getChilds()[0]; // Refresh child1 reference
			System.out.println("Just before the error: father's age:"+family.getFather().getAge()+" name="+child1.getName()+" child1's age="+child1.getAge()+"  child1 ID:"+System.identityHashCode(child1));

			
			//Set family2 father's age as 44
			System.out.println("Set family2 father's age as 44");
			family2.getFather().setAge(44);		
			
			//Error!!!
			((String)null).isEmpty();
			
			CPersistentRoot.commitTransaction();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			CPersistentRoot.rollBackTransaction();
		}
		child1=family.getChilds()[0]; 
		System.out.println("After: father's age:"+family.getFather().getAge()+" name="+child1.getName()+" child1's age="+child1.getAge()+"  child1 ID:"+System.identityHashCode(child1));
		
		
		System.out.println("After: father2's age:"+family2.getFather().getAge()+" (non-persistent)");
		//showFamily(family);
	}
	private static void showFamily(Family family)
	{
		int n=0;
		if(family==null)
		{
			System.out.println("no family");
			return;
		}
		System.out.println(family);
		showPerson("Father",family.getFather());
		showPerson("Mother",family.getMother());
		if(family.getChilds()!=null)
		{
			for(Person p:family.getChilds())
			{
				showPerson("Child "+(++n),p);
			}
		}
		else
		{
			System.out.println("Childs: none");
		}
	}
	private static void showPerson(String label,Person person)
	{
		System.out.println(label+" Name:"+(person!=null?person.getName():"null"));
		System.out.println(label+" Age:"+(person!=null?person.getAge():"null"));
		System.out.println(label+" Address:"+(person!=null?person.getAddress():"null"));	
	}
}
