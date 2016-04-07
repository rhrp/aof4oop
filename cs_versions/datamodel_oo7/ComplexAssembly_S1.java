package rhp.aof4oop.oo7.datamodel;
import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;
import rhp.aof4oop.oo7.benchmarck.SharedArea;

/**
 * 
 * @author rhp
 *
 */
@Aof4oopVersionAlias(alias = "S1")
public class ComplexAssembly extends Assembly
{
	private Assembly[] subAssemblies;

	public ComplexAssembly()
	{
		super();
	}
	public ComplexAssembly(int newID, Module module,ComplexAssembly parentAssembly, int level)
	{
		super(newID, "ComplexAssembly",module.getBuildDate());
		//System.out.println(getType()+"("+newID+") level="+level);
		int nextId;

		// initialize the simple stuff (some of it randomly)
		if (parentAssembly != null) 
		{
			setSuperAssembly(parentAssembly);
		}

		if (getSubAssemblies() == null)
		{
			setSubAssemblies(new Assembly[0]);
		}

		// recursively create subassemblies for this complex assembly
		for (int i = 0; i < SharedArea.NumAssmPerAssm; i++) 
		{
			if (level < SharedArea.NumAssmLevels - 1) 
			{
				// create a complex assembly as the subassembly
				nextId = SharedArea.nextComplexAssemblyId++;
				ComplexAssembly complexAssembly = new ComplexAssembly(nextId,module, this, level + 1);
				this.setSubAssemblies(addAssembly(this.getSubAssemblies(),complexAssembly));
			} 
			else 
			{
				// create a base assembly as the subassembly
				nextId = SharedArea.nextBaseAssemblyId++;
				BaseAssembly baseAssembly = new BaseAssembly(nextId, module,this);
				this.setSubAssemblies(addAssembly(this.getSubAssemblies(),baseAssembly));
			}// end if-else levelNo
		}// end for
	}
//
//	public ComplexAssembly(int id, long buildDate,Assembly[] subAssemblies) 
//	{
//		super(id, "ComplexAssembly", buildDate);
//		this.subAssemblies=subAssemblies;
//	}
	public Assembly[] getSubAssemblies() 
	{
		return subAssemblies;
	}

	public void setSubAssemblies(Assembly[] subAssemblies) 
	{
		this.subAssemblies = subAssemblies;
	}

}
