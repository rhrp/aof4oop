package rhp.aof4oop.framework.apps.utils;

import rhp.aof4oop.framework.core.CPersistentRoot;


public class ShowDatabase 
{

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception
	{
		CPersistentRoot psRoot;
		
		psRoot=new CPersistentRoot();
		
		//psRoot.gc(1);
		psRoot.showAllDB(false);
		CPersistentRoot.printStats();
	}

}
