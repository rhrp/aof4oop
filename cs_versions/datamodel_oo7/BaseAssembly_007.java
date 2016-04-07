package rhp.aof4oop.oo7.datamodel;


import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;


@Aof4oopVersionAlias(alias = "S1")
public class BaseAssembly extends Assembly
/*
@Aof4oopVersionAlias(alias = "S3")
public class BaseAssembly extends DummyClass
*/
{
	private CompositePart[] componentsPrivate;
	private CompositePart[] componentsShared;
	
	public BaseAssembly()
	{
		super();
	}
	public BaseAssembly(int id,long buildDate) 
	{
		super(id, "BaseAssembly", buildDate);
	}
	public BaseAssembly(int newId, Module module, ComplexAssembly parentAssembly)
	{
		this(newId, module.getBuildDate());
		//System.out.println(getType()+"("+newId+")");

		setModule(module);
		if (parentAssembly != null) 
		{
			setSuperAssembly(parentAssembly);
		}
		// all Base Assemblies are added to the module
		if (module.getAssemblies() == null) 
		{
			module.setAssemblies(new Assembly[0]);
		}

		//module.getAssemblies().add(this);
		module.setAssemblies(addAssembly(module.getAssemblies(),this));
	}
	public CompositePart[] getComponentsPrivate() 
	{
		return componentsPrivate;
	}
	public void setComponentsPrivate(CompositePart[] compomentsPrivate) 
	{
		//System.out.println("BA "+getId()+" now has "+compomentsPrivate.length+" private elements");
		this.componentsPrivate = compomentsPrivate;
	}
	public CompositePart[] getComponentsShared() 
	{
		return componentsShared;
	}
	public void setComponentsShared(CompositePart[] componentsShared) 
	{
		//System.out.println("BA "+getId()+" now has "+componentsShared.length+" shared elements");
		this.componentsShared = componentsShared;
	}
	
}
