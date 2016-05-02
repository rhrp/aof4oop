package rhp.aof4oop.framework.core;

public class CDefinitions 
{
	//Object and meta-object file repository
	public static final String AOF4OOP_DATABASE					=	"aof4oop.dbf";
	// UBMO meta-objects repository
	public static final String AOF4OOP_UBMODB					=	"ubmo.xml";
	//Class path given to dynamic weaver
	public static final String AOF4OOP_DYN_WEAVER_CLASSPATH		=	"dweaver/classpath;eclipse/bin;dist/classes";// eclipse/bin allows access to framework code as well as dist/classes when running by means of Ant 
	//Place where class loader exports all known class versions
	public static final String AOF4OOP_CLASSLOADER_CLASSPATH	=	"dweaver/classpath";
	//List of all Apps' JARs
	public static final String[] AOF4OOP_APPCLASSPATH			=	new String[]{};//JMapViewer bolongs to src {"lib/JMapViewer_29618.jar"};
	
	//Place where dynamic weaver put all compiled code
	public static final String AOF4OOP_DYN_WEAVER_OUTDIR		= 	"dweaver/out";
}
