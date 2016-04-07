package rhp.aof4oop.oo7.datamodel;

import rhp.aof4oop.oo7.benchmarck.SharedArea;


public class Module extends DesignObject
{
	private Manual	manual;
	private Assembly[] assemblies;
	private ComplexAssembly designRoot;
	
	public Module()
	{
		super();
	}
	public Module(int id, long buildDate) 
	{
		super(id, "Module", buildDate);
		System.out.println("Module("+id+","+buildDate+")");
		// Build a manual
		manual = new Manual(getId(), this);

		int numAssmPerAssm = SharedArea.NumAssmPerAssm;
		int numAssmLevels = SharedArea.NumAssmLevels;

		// Reserve space for assemblies
		int size = numAssmPerAssm;

		for (int n = 1; n < numAssmLevels; n++)
		{
			size *= numAssmPerAssm;
		}

		// assemblies.capacity(size);

		// now create the assemblies for the module
		if (numAssmLevels > 1) 
		{
			int assmId = SharedArea.nextComplexAssemblyId++;
			designRoot = new ComplexAssembly(assmId, this, null, 1);
		}
	}
	public Manual getManual() 
	{
		return manual;
	}

	public void setManual(Manual manual) {
		this.manual = manual;
	}

	public Assembly[] getAssemblies() {
		return assemblies;
	}

	public void setAssemblies(Assembly[] assemblies) {
		this.assemblies = assemblies;
	}

	public ComplexAssembly getDesignRoot() {
		return designRoot;
	}

	public void setDesignRoot(ComplexAssembly designRoot) {
		this.designRoot = designRoot;
	}
	
}
