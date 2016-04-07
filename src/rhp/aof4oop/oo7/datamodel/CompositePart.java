package rhp.aof4oop.oo7.datamodel;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;
import rhp.aof4oop.oo7.benchmarck.GeneralParameters;
import rhp.aof4oop.oo7.benchmarck.RandomUtil;
import rhp.aof4oop.oo7.benchmarck.SharedArea;


@Aof4oopVersionAlias(alias = "oo7")
public class CompositePart extends DesignObject 
{

	private Document documentation = null;
	// relationship: componentsPriv
	private BaseAssembly[] usedInPrivate = null;// set of BaseAssemblies
	// relationship: componentsShared
	private BaseAssembly[] usedInShared = null;// set of BaseAssemblies
	// relationship: partOF
	private AtomicPart[] parts = null;// Set of AtomicParts

	private AtomicPart rootPart = null;
	
	public CompositePart()
	{
		super();
	}
	public CompositePart(int id)
	{
		super(id,"CompositePart",System.currentTimeMillis());
		// TODO: PVZ: SET THE BUILD DATE: EITHER YOUNG OR OLD
		// for the build date, decide if this part is young or old, and then
		// randomly choose a date in the required range

		if ((getId() % GeneralParameters.youngCompFrac) == 0) 
		{
			// young one
			//System.out.println("young composite part, id = " + getId());
			setBuildDate(GeneralParameters.minYoungCompDate + (RandomUtil.nextPositiveInt() % (GeneralParameters.maxYoungCompDate - GeneralParameters.minYoungCompDate + 1)));
		} 
		else 
		{
			// old one
			//System.out.println("old composite part, id = " + getId());
			setBuildDate(GeneralParameters.minOldCompDate + (RandomUtil.nextPositiveInt() % (GeneralParameters.maxOldCompDate - GeneralParameters.minOldCompDate + 1)));
		}

		Document document = new Document(id, this);
		setDocumentation(document);

		
		// now create the atomic parts (indexed by their ids) ...
		int numAtomicPerComp = SharedArea.NumAtomicPerComp;
		int atomicId = 0;
		AtomicPart atomicPart = null;
		//System.out.println("Cria PARTS numAtomicPerComp="+numAtomicPerComp);
		for (int i = 0; i < numAtomicPerComp; i++) 
		{

			atomicId = SharedArea.nextAtomicId + i;
			// atomicId++; or ++atomicID if we start from 0 and not 1

			/*
			 * create a new atomic part the AtomicPart constructor takes care of
			 * setting up the back pointer to the containing CompositePart()
			 */
			if (getParts() == null) 
			{
				setParts(new AtomicPart[0]);
			}
			atomicPart = new AtomicPart(atomicId, this);

			setParts(add(getParts(),atomicPart));
		}// end for

		// first atomic part is the root part
		setRootPart((AtomicPart) parts[0]);

		/*
		 * ... and then wire them semi-randomly together (as a ring plus random
		 * additional connections to ensure full part reachability for
		 * traversals)
		 */

		int numConnPerAtomic = SharedArea.NumConnPerAtomic;
		//System.out.println("Cria os Atomic Parts: numAtomicPerComp="+numAtomicPerComp+"  numConnPerAtomic="+numConnPerAtomic);
		AtomicPart fromAtomicPart = null;
		AtomicPart toAtomicPart = null;
		Connection connection = null;

		for (int from = 0; from < numAtomicPerComp; from++) 
		{
			fromAtomicPart = getParts()[from];

			// OZONE's implementation calls this the next
			int to = (from + 1) % numAtomicPerComp;
			
			// OZONE's implementation has i< numConnPerAtomic-1???? Why?
			for (int i = 0; i < numConnPerAtomic; i++) 
			{
				toAtomicPart = getParts()[to];
				connection = new Connection(fromAtomicPart,toAtomicPart);
				to = RandomUtil.nextPositiveInt() % numAtomicPerComp;
				//to = RandomUtil.nextPositiveInt(numAtomicPerComp);
			}// end for i

		}// end for from
		SharedArea.nextAtomicId += numAtomicPerComp;
	}
	public Document getDocumentation() {
		return documentation;
	}
	public void setDocumentation(Document documentation) {
		this.documentation = documentation;
	}
	public BaseAssembly[] getUsedInPrivate() {
		return usedInPrivate;
	}
	public void setUsedInPrivate(BaseAssembly[] usedInPrivate) {
		this.usedInPrivate = usedInPrivate;
	}
	public BaseAssembly[] getUsedInShared() {
		return usedInShared;
	}
	public void setUsedInShared(BaseAssembly[] usedInShared) {
		this.usedInShared = usedInShared;
	}
	public AtomicPart[] getParts() {
		return parts;
	}
	public void setParts(AtomicPart[] parts) {
		this.parts = parts;
	}
	public AtomicPart getRootPart() {
		return rootPart;
	}
	public void setRootPart(AtomicPart rootPart) {
		this.rootPart = rootPart;
	}
	public static AtomicPart[] add(AtomicPart[] in,AtomicPart toadd)
	{
		AtomicPart[] out=new AtomicPart[in.length+1];
		for(int i=0;i<in.length;i++)
		{
			out[i]=in[i];
		}
		out[in.length]=toadd;
		return out;
	}
	public static CompositePart[] add(CompositePart[] in,CompositePart toadd)
	{
		CompositePart[] out;
		int n;
		
		if(in==null)
		{
			out=new CompositePart[1];
			n=0;
		}
		else
		{
			out=new CompositePart[in.length+1];
			for(int i=0;i<in.length;i++)
			{
				out[i]=in[i];
			}
			n=in.length;
		}
		out[n]=toadd;
		return out;
	}
	public void addShared(BaseAssembly baseAssemblyShared) 
	{
		/*
		 * add this assembly to the list of assemblies in which this composite
		 * part is used as a shared member
		 */
		if (getUsedInShared() == null) 
		{
			BaseAssembly[] ba=new BaseAssembly[1];
			ba[0]=baseAssemblyShared;
			setUsedInShared(ba);
		}
		else
		{
			setUsedInPrivate(BaseAssembly.addBaseAssembly(getUsedInShared(),baseAssemblyShared));
		}
		/*
		 * add this composite part cp the list of usedInShared parts used in the
		 * assembly
		 */
		if (baseAssemblyShared.getComponentsShared() == null) 
		{
			CompositePart[] ass=new CompositePart[1];
			ass[0]=this;
			baseAssemblyShared.setComponentsShared(ass);
		}
		else
		{
			baseAssemblyShared.setComponentsShared(add(baseAssemblyShared.getComponentsShared(),this));
		}
	}
	public void addPrivate(BaseAssembly baseAssemblyPrivate) 
	{

		/*
		 * first add this assembly to the list of assemblies in which this
		 * composite part is used as a private member
		 */
		if (getUsedInPrivate() == null)
		{
			BaseAssembly[] ba=new BaseAssembly[1];
			ba[0]=baseAssemblyPrivate;
			setUsedInPrivate(ba);
		}
		else
		{
			setUsedInPrivate(BaseAssembly.addBaseAssembly(getUsedInPrivate(),baseAssemblyPrivate));
		}
		/*
		 * add this composite part to the list of usedInPrivate parts usedin the
		 * assembly
		 */
		if (baseAssemblyPrivate.getComponentsPrivate() == null)
		{
			CompositePart[] ass=new CompositePart[1];
			ass[0]=this;
			baseAssemblyPrivate.setComponentsPrivate(ass);
		}
		else
		{
			baseAssemblyPrivate.setComponentsPrivate(add(baseAssemblyPrivate.getComponentsPrivate(),this));
		}

	}
}