package rhp.aof4oop.oo7.benchmarck;


public class SharedArea 
{
	public static int numberAssembliesForModule	=	1093;
	public static int nextBaseAssemblyId	=	1;
	public static int nextComplexAssemblyId	=	1;
	public static int nextCompositeId		=	1;
	public static int nextModuleId			=	1;
	public static int nextAtomicId			=	1;
	public static int NumAssmPerAssm	= 3;
	public static int NumAssmLevels		= 7;//7; //5
	public static int NumCompPerModule	=	500;		// Total of Composite Object per module (part of numberOfCompositeObjects that are connected to a module)
	public static int NumCompPerAssm	= 3;
	public static int NumAtomicPerComp	= 20; //20
	public static int NumConnPerAtomic	= 3;
	public static int numberOfCompositeObjects	=	500;//500;// Total of composite object
	public static int TotalModules		= 1;
	public static int TotalCompParts	= NumCompPerModule * TotalModules;
	
	// Traversal types:
	public static String Trav1 = "Trav1";
	public static String Trav1WW = "Trav1WW";
	public static String Trav2a = "Trav2a";
	public static String Trav2b = "Trav2b";
	public static String Trav2c = "Trav2c";
	public static String Trav3a = "Trav3a";
	public static String Trav3b = "Trav3b";
	public static String Trav3c = "Trav3c";
	public static String Trav4 = "Trav4";
	public static String Trav6 = "Trav6";
	public static String Trav5do = "Trav5do";
	public static String Trav5undo = "Trav5undo";

}
