package rhp.aof4oop.apps.demo;

import java.util.List;


import rhp.aof4oop.dataobjects.Family;
import rhp.aof4oop.dataobjects.Foo;
import rhp.aof4oop.dataobjects.Person;
import rhp.aof4oop.framework.core.CPersistentRoot;
import rhp.aof4oop.framework.core.CQuery;


/**
 * This demo shows a tree of objects placed directly on the container.
 * All child objects are also made persistent at same time.
 * 
 * @author rhp
 *
 * Don't forget to add the parameter
 * -Djava.system.class.loader=rhp.aof4oop.framework.core.CClassLoader		
 * 
 * Or
 * 
 *	CClassLoader cl=new CClassLoader();
 *	Thread.currentThread().setContextClassLoader(cl);
 */
//TODO: parece que o GC não faz o trabalho todo de uma só vez. A dependencia nos dois sentidos entre links e objectos requer duas o mais execuções do GC

public class Demo04
{
	/**
	 * Testes a fazer copiar uma arvore de uma root para outra. Depois apagar uma delas. Aparentemente, as duas são apagadas ficando apenas a raiz da outra!!!
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception 
	{

		
		demoConstraintNotNull();
	}
	/**
	 * Demo of persistence
	 */
	public static void demoPersistence()
	{
		
		CPersistentRoot psRoot=new CPersistentRoot();
//		psRoot.showAllDB();
//		psRoot.setRootObject("aaa",new Address())
//		Address a=psRoot.getRootObject("aaa");
		
		
		Family f;
		Person p,m;
		
		
		
//		f=new Family();
//		f.setFather(new Person("Rui",null,new Address("Rua X",526),39));
//		f.setMother(new Person("Cristina","bicris",f.getFather().getAddress(),f.getFather().getAge()-2));
//		psRoot.setRootObject("Versao C",f);// Testar a exec multipla
		
//		psRoot.setRootObject("aaa",f);
//		System.out.println("Father:"+f.getFather());
//		f=(Family)psRoot.getRootObject("bbb");
		f=(Family)psRoot.getRootObject("Versao B");
//		psRoot.setRootObject("Versao C",null);
		
		p=f.getFather();
		m=f.getMother();
		
//		m.getAddress().setNumero(123);

//		p.setAddress(new Address("Rua Y",526));
//		m.setAddress(p.getAddress());
//		f.getFather().setAddress(f.getMother().getAddress()); 
		
//		f.setFather(new Person("Rui",null,new Address("Rua Y",526),39));

		

		
//		p.setName("Rui H. Pereira"); //não funciona. possivelmente por estar numa super class
//		p.setAge(40); //Ok
//		p.setAddress(new Address("Rua Xpto",526)); // Ok
//
//		p.getAddress();
//		p.setAddress(null);	//Ok
//		p.setAddress(m.getAddress());
//		p.getAddress().setMorada("Rua xpto"); // Ok
//		f.setWeddingDate(new Date());
//
//		m.setBirth(new Date());
		
//		p.setAge(41);
//		m.setAge(p.getAge()-2);
		
//		showFamily(psRoot,f);

//		psRoot.gc();
//		psRoot.showAllDB();
		psRoot.close();
//		psRoot.dumpCache(true);
	}
	public static void demoQuery()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		List<Family> families=psRoot.query(new CQuery(Family.class));
		for(Family f:families)
		{
			System.out.println("Family: "+f);
		}
//		psRoot.close();//Explicit close
	}
	public static void demoView()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		boolean r;
		
		r=psRoot.dropView("allPersons");
		System.out.println("The already exists. Was droped:"+r);
		
		r=psRoot.createView("allPersons",new CQuery(Person.class));
		System.out.println("A new one was created:"+r);
		
		List<Person> persons=psRoot.view("allPersons");
		for(Person p1:persons)
		{
			System.out.println("Person: "+p1);
		}
		//psRoot.close(); //Explicit close
	}
	public static void demoGenericity()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
//		//Write
//		Foo<Person> foo=CPersistentRoot.wrapper(new Foo<Person>(),new String[]{Person.class.getCanonicalName()});
//		foo.setFoo(new Person("Rui","123456789",null,40));
//		psRoot.setRootObject("fooOfPersons",foo);
		
		//Read
		Foo<Person> foo=psRoot.getRootObject("fooOfPersons");
		
		//present results
		String[] tp=CPersistentRoot.findRuntimeTypeParameters(foo);
		System.out.println("Foo Type Parameters="+(tp!=null?tp[0]:"NULL"));
		System.out.println("Foo Object"+foo.getFoo());
		
		psRoot.showAllDB(true);
	}
	public static void demoArray()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		
//		//Write
//		Family family=new Family();
//		Address addr=new Address("FooBar Street", 321);
//		psRoot.setRootObject("myFamily",family);
//		
//		//family.setWeddingDate(new Date());
//		family.setChilds(new Person[]{new Person("Inês","123",addr,3),new Person("Ana","123",addr,11)});
		
		//Read
		Family family=psRoot.getRootObject("myFamily");
		
//		Address addr=new Address("FooBar Street", 1234);
//		family.setChilds(new Person[]{new Person("Ana","123",addr,11),new Person("Inês","123",addr,3)});
//		family.getChilds()[0].setAddress(new Address("FooBar Street", 1234));
		
//		present results
		System.out.println("Family Object: "+family.getChilds()[0].getName()+"  "+family.getChilds()[0].getAddress().getMorada()+" "+family.getChilds()[0].getAddress().getNumero());
	}
	public static void demoConstraintNotNull()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		//Family family=new Family();
		
		//family.setName("Teste");
		//psRoot.setRootObject(family);
		Family family=psRoot.getRootObject();
		
		//family.setFather(new Person("Rui",null,null,0));
		//family.setName(null);
		Person father=family.getFather();
		//father.setName(null);
		System.out.println("Family name:"+family.getFather().getName());
	}
	
	private static void showFamily(CPersistentRoot psRoot,Family f)
	{
		Person p,m;
		
		p=f.getFather();
		m=f.getMother();
		
		System.out.println("Father  LOID="+psRoot.getLOID(p)+"   OID="+psRoot.getOID(p));
		System.out.println("Name:"+p.getName());
		System.out.println("Age:"+p.getAge());
		System.out.println("Birth:"+p.getBirth());
		System.out.println("Address:"+p.getAddress()+"  LOID="+psRoot.getLOID(p.getAddress())+"   OID="+psRoot.getOID(p.getAddress()));
		System.out.println("Mother  LOID="+psRoot.getLOID(m)+"   OID="+psRoot.getOID(m));
		System.out.println("Birth:"+m.getBirth());
		System.out.println("Name:"+m.getName());
		System.out.println("Age:"+m.getAge());
		System.out.println("Address:"+m.getAddress()+"  LOID="+psRoot.getLOID(m.getAddress())+"   OID="+psRoot.getOID(m.getAddress()));
		
		System.out.println("Wedding Date:"+f.getWeddingDate());
	}
}
