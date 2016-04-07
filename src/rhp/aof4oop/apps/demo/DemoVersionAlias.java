package rhp.aof4oop.apps.demo;

import java.util.Date;

import rhp.aof4oop.dataobjects.Family;
import rhp.aof4oop.framework.core.CPersistentRoot;

public class DemoVersionAlias 
{
	public static void main(String[] args) throws Exception 
	{
		CPersistentRoot psRoot;
		Family family;
		
		psRoot=new CPersistentRoot();
		
		family=new Family();
		family.setName(null);
		family.setWeddingDate(new Date());
//		psRoot.setRootObject("testVersionAlias",family);

		family=psRoot.getRootObject("testVersionAlias");
		System.out.println("Family name: "+family.getName());
		System.out.println("Family xpto: "+family.getXpto());
		System.out.println("Family wedding Date: "+family.getWeddingDate());
	}
}
