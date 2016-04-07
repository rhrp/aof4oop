package rhp.aof4oop.framework.core.datamodel;

import rhp.aof4oop.framework.core.IQuery;

public class CView 
{
	private String name;
	private IQuery query;
	
	public CView(String name, IQuery query) 
	{
		super();
		this.name = name;
		this.query = query;
	}

	public String getName() {
		return name;
	}

	public IQuery getQuery() {
		return query;
	}
	public String toString()
	{
		return "View::"+getName();
	}
	
}
