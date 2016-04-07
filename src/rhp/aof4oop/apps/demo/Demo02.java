package rhp.aof4oop.apps.demo;

import java.util.Date;

import rhp.aof4oop.dataobjects.Address;
import rhp.aof4oop.dataobjects.Family;
import rhp.aof4oop.dataobjects.Person;
import rhp.aof4oop.framework.core.CPersistentRoot;

/**
 * This demo shows a tree of objects placed directly on the container.
 * All child objects are also made persistent at same time.
 * 
 * @author rhp
 *
 */
public class Demo02 
{
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception 
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		Person reference;
		
		Family family=new Family();
		family.setFather(new Person("Rui",null,null,39));
		family.setMother(new Person("Isabel",null,null,37));
		family.setChilds(new Person[]{new Person("Ana",null,null,9),new Person("Inês",null,null,1)});
		family.getMother().setAddress(new Address("Rua XPTO",526));
		family.getFather().setAddress(family.getMother().getAddress());
		family.setWeddingDate(new Date());
		for(Person p:family.getChilds())
		{
			p.setAddress(family.getFather().getAddress());
		}
		
		System.out.println("The cache before");		
		psRoot.dumpCache();
		
		System.out.println("Turns the object persistent");
		psRoot.setRootObject("pereira",family);

		family.getChilds()[1].getAge();
		System.out.println("The cache after");
		psRoot.dumpCache();
		
		reference=family.getMother();
		System.out.println("Is this object Person persistente:"+CPersistentRoot.isPersistent(reference));
	}
}
