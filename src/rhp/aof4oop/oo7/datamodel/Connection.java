package rhp.aof4oop.oo7.datamodel;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "oo7")
public class Connection 
{
	private String type = null;
	private int length;
	private AtomicPart from = null;
	private AtomicPart to = null;
	
	public Connection()
	{
		super();
	}
	public Connection(AtomicPart fromPart, AtomicPart toPart) 
	{
		setFrom(fromPart);
		setTo(toPart);
		//System.out.println("new Connection "+fromPart.getId()+" to "+toPart.getId());
		if (fromPart.getToConnections() == null) 
		{
			// the first time
			fromPart.setToConnections(new Connection[0]);
		}
		fromPart.setToConnections(add(fromPart.getToConnections(),this));
		if (toPart.getFromConnections() == null) 
		{
			// the first time
			toPart.setFromConnections(new Connection[0]);
		}
		toPart.setFromConnections(add(toPart.getFromConnections(),this));
	}
	private static Connection[] add(Connection[] conns,Connection toadd)
	{
		Connection[] out=new Connection[conns.length+1];
		for(int i=0;i<conns.length;i++)
		{
			out[i]=conns[i];
		}
		out[conns.length]=toadd;
		return out;
	}
	public AtomicPart getFrom() 
	{
		return from;
	}
	public void setFrom(AtomicPart from) 
	{
		this.from = from;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}


	public AtomicPart getTo() {
		return to;
	}

	public void setTo(AtomicPart to) {
		this.to = to;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
