package rhp.aof4oop.oo7.datamodel;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "oo7")
public class Assembly extends DesignObject
{
	private ComplexAssembly superAssembly;
	private Module module;
	
	public Assembly()
	{
		super();
	}
	public Assembly(int id, String type, long buildDate) 
	{
		super(id, type, buildDate);
	}
	public Assembly(int id,long buildDate) 
	{
		this(id, "Assembly", buildDate);
	}
	public ComplexAssembly getSuperAssembly() 
	{
		return superAssembly;
	}
	public void setSuperAssembly(ComplexAssembly superAssembly) 
	{
		this.superAssembly = superAssembly;
	}
	public Module getModule() 
	{
		return module;
	}
	public void setModule(Module module) {
		this.module = module;
	}
	public static Assembly[] addAssembly(Assembly[] in,ComplexAssembly toadd)
	{
		Assembly[] out=new Assembly[in.length+1];
		for(int i=0;i<in.length;i++)
		{
			out[i]=in[i];
		}
		out[in.length]=toadd;
		return out;		
	}
	public static Assembly[] addAssembly(Assembly[] in,BaseAssembly toadd)
	{
		Assembly[] out=new Assembly[in.length+1];
		for(int i=0;i<in.length;i++)
		{
			out[i]=in[i];
		}
		out[in.length]=toadd;
		return out;
	}
	public static BaseAssembly[] addBaseAssembly(BaseAssembly[] in,BaseAssembly toadd)
	{
		BaseAssembly[] out=new BaseAssembly[in.length+1];
		for(int i=0;i<in.length;i++)
		{
			out[i]=in[i];
		}
		out[in.length]=toadd;
		return out;
	}
}
