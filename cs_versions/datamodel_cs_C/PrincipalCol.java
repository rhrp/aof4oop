package rhp.aof4oop.cs.datamodel;

public class PrincipalCol 
{
	private Principal[] principals;

	public PrincipalCol() 
	{
		super();
	}
	
	public PrincipalCol(Principal[] principals) 
	{
		super();
		this.principals = principals;
	}

	public Principal[] getPrincipals() {
		return principals;
	}

	public void setPrincipals(Principal[] principals) {
		this.principals = principals;
	}
	
	
}
