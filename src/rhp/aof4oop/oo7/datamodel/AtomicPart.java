package rhp.aof4oop.oo7.datamodel;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;
import rhp.aof4oop.oo7.benchmarck.SharedArea;
@Aof4oopVersionAlias(alias = "S1")
public class AtomicPart extends DesignObject 
{
	private int x, y;
	private int docId;
	private Connection[] toConnections = null;// set of Connections
	private Connection[] fromConnections = null;// set of Connections
	// relationship: parts
	private CompositePart partOf = null;

	public AtomicPart()
	{
		super();
	}
	public AtomicPart(int newId, CompositePart compositePart) 
	{
		super(newId,"AtomicPart",System.currentTimeMillis());

		setPartOf(compositePart);

		setX((int)System.currentTimeMillis()%10000);
		setY((int)System.currentTimeMillis()%12345);


		int tmpDocId = (int)(System.currentTimeMillis() % SharedArea.TotalCompParts) + 1;
		setDocId(tmpDocId);
	}

	public void swapXY() 
	{
		int tmp = x;
		setX(y);
		setY(tmp);
		System.out.println("swapXY x="+x+"  y="+y);
	}
	public void toggleDate() 
	{

		if (getBuildDate() % 2 == 0) 
		{
			// even case
			setBuildDate(getBuildDate() - 1);
		} 
		else 
		{
			// odd case
			setBuildDate(getBuildDate() + 1);
		}
	}
	public int getDocId()
	{
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public Connection[] getFromConnections() 
	{
		return fromConnections;
	}

	public void setFromConnections(Connection[] fromConnections) 
	{
		this.fromConnections = fromConnections;
	}

	public CompositePart getPartOf() {
		return partOf;
	}

	public void setPartOf(CompositePart partOf) {
		this.partOf = partOf;
	}

	public Connection[] getToConnections() 
	{
		return toConnections;
	}

	public void setToConnections(Connection[] toConnections) {
		this.toConnections = toConnections;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() 
	{
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	public static void doNothing() 
	{
	}
}