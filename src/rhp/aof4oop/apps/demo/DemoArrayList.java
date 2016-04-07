package rhp.aof4oop.apps.demo;


import java.util.ArrayList;

import rhp.aof4oop.dataobjects.CMyObjecto;
import rhp.aof4oop.framework.core.CPersistentRoot;

public class DemoArrayList 
{

	public static void main(String[] args) 
	{
		test2();
	}
	public static void test2()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		
		//Store
//		CMyObjecto myo=new CMyObjecto();
//		ArrayList<String>  list=new ArrayList<String>();
//		list.add("one");
//		list.add("two");
//		list.add("three");
//		myo.setListOfStrings(list);
//		myo.setAaaa(123456);
//		psRoot.setRootObject("test2",myo);
		
		//Load
		CMyObjecto myo=psRoot.getRootObject("test2");
		System.out.println("aaaa="+myo.getAaaa());
		myo.setAaaa(myo.getAaaa()+1);
		ArrayList<String>  list=myo.getListOfStrings();
		if(list!=null)
		{
			for(String it:list)
			{
				System.out.println(it);
			}
		}
		else
		{
			System.out.println("The list does not exists");
		}
	}
	public static void testArrayList()
	{
		CPersistentRoot psRoot=new CPersistentRoot();
		
		
		//Store
//		ArrayList<String>  list=new ArrayList<String>();
//		list.add("one");
//		list.add("two");
//		list.add("three");
//		psRoot.setRootObject("arrayListOfString",list);
		
		//Load
		ArrayList<String>  list=psRoot.getRootObject("arrayListOfString");
		if(list!=null)
		{
			for(String it:list)
			{
				System.out.println(it);
			}
		}
		else
		{
			System.out.println("The list does not exists");
		}
	}

}
