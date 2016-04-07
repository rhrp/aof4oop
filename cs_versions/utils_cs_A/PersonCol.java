package rhp.aof4oop.cs.utils;

import java.util.ArrayList;
import java.util.Arrays;

import rhp.aof4oop.cs.datamodel.Person;
import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "A")
public class PersonCol 
{
	Person[] persons;

	public PersonCol() 
	{
		super();
		this.persons = new Person[0];
	}
	public PersonCol(Person[] persons) 
	{
		super();
		this.persons = persons;
	}
	public PersonCol(ArrayList<Person> persons)
	{
		this.persons=new Person[persons.size()];
		int i=0;
		for(Person p:persons)
		{
			this.persons[i]=p;
			i++;
		}
	}
	public Person[] getPersons() 
	{
		return persons;
	}

	public void setPersons(Person[] persons) 
	{
		this.persons = persons;
	}
	public Person get(int i)
	{
		return this.persons[i];
	}
	public void set(int i,Person person)
	{
		Person[] tmp=this.persons;
		tmp[i]=person;
		System.out.println("set "+i+" "+person.getFirstName());
		this.persons=tmp;
	}
	public int size()
	{
		return persons.length;
	}
	public void add(Person new_person)
	{
//		Person[] tmp=new Person[size()+1];
//		for(int i=0;i<this.persons.length;i++)
//		{
//			Person p=get(i);
//			System.out.println("set "+i+" "+p.getFirstName());
//			tmp[i]=p;
//		}
//		tmp[size()]=new_person;
		Person[] tmp = Arrays.copyOf(this.persons, this.persons.length + 1);
		tmp[size()]=new_person;
		//System.out.println("set "+size()+" "+new_person.getFirstName());
		this.persons=tmp;
	}
}
