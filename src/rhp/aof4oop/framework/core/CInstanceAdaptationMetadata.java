package rhp.aof4oop.framework.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import rhp.aof4oop.framework.core.CClassLoader;
import rhp.aof4oop.framework.core.CDefinitions;
import rhp.aof4oop.framework.core.CDynCompiler;
import rhp.aof4oop.framework.core.datamodel.CClassVersionMetaObject;
import rhp.aof4oop.framework.core.datamodel.CInstanceMetaObject;
import rhp.aof4oop.framework.core.datamodel.CAttributeMetaObject;
import rhp.aof4oop.framework.core.datamodel.CUpdateBackdateMetaObject;
import rhp.aof4oop.framework.core.exceptions.EFrameworkFault;

/**
 * This class implements all logic related with Instance Adaptation
 * Loads and manages all UBMOs
 * Also provides several matching operations
 * @author rhp
 *
 */
@XmlRootElement(name = "AOF4OOP")
public class CInstanceAdaptationMetadata 
{
	private String version;
	@XmlAttribute(name="version",required=true)
	public String getVersion() 
	{
		return version;
	}
	public void setVersion(String version) 
	{
		this.version = version;
	}
	CUpdateBackdateMetaObject  ubmo[];
	@XmlElements(value = { @XmlElement(name="ubmo") })
	public CUpdateBackdateMetaObject[] getUbmo()
	{
		return ubmo;
	}
	public void setUbmo(CUpdateBackdateMetaObject[] ubmo) 
	{
		this.ubmo = ubmo;
	}
	/**
	 * Dump to screen all UBMOs
	 */
	public void dump()
	{
		if(getUbmo()==null)
		{
			System.out.println("null");
		}
		for(CUpdateBackdateMetaObject ubmo:getUbmo())
		{
			System.out.println("UBMO:");
			System.out.println("   sperClassName="+ubmo.getMatchSuperClassName());
			System.out.println("   className="+ubmo.getMatchClassName());
			System.out.println("   oldVersion="+ubmo.getMatchOldClassVersion());
			System.out.println("   apply default="+ubmo.isApplyDefault());
			System.out.println(ubmo.getConversionSourceCode());
		}
	}
	/**
	 * find the FIRST UBMO that matches with @param cvmo
	 * @param cvmo - CVMO of loaded object
	 * @return
	 * @throws ClassNotFoundException
	 * @throws CannotCompileException 
	 * @throws IOException 
	 * @throws NotFoundException 
	 */
	public CUpdateBackdateMetaObject match(CClassLoader cl,CClassVersionMetaObject cvmo,CClassVersionMetaObject pcvmo,CAttributeMetaObject parentLink) throws ClassNotFoundException, NotFoundException, IOException, CannotCompileException
	{
		if(getUbmo()==null)
		{
			System.out.println("null");
		}
		for(CUpdateBackdateMetaObject ubmo:getUbmo())
		{
//			System.out.println("UBMO:");
//			System.out.println("   currentSuperClassName="+ubmo.getMatchSuperClassName());
//			System.out.println("   className="+ubmo.getMatchClassName());
//			System.out.println("   oldVersion="+ubmo.getMatchOldClassVersion());
			//System.out.println("   apply default="+ubmo.isApplyDefault());
			//System.out.println(ubmo.getConversion());
			if(ubmo.getMatchCurrentClassVersion()!=null)
			{
				if(!matchClassVersion(ubmo.getMatchCurrentClassVersion(),CClassVersionMetaObject.calcVersion(cvmo.getClassCanonicalName())))
				{
					continue;
				}
				//System.out.println("OK MatchCurrentClassVersion: "+ubmo.getMatchCurrentClassVersion()+" == "+CClassVersionMetaObject.calcVersion(cvmo.getClassCanonicalName()));
			}

			if(ubmo.getMatchSuperClassName()!=null)
			{
				if(!matchClassName(ubmo.getMatchSuperClassName(),cvmo.getSuperClass().getCanonicalName()))
				{
					continue;
				}
				//System.out.println("OK MatchSuperClassName: "+ubmo.getMatchSuperClassName()+" == "+cvmo.getSuperClass().getCanonicalName());
			}
			if(ubmo.getMatchOldClassVersion()!=null)
			{
				if(!matchClassVersion(ubmo.getMatchOldClassVersion(),cvmo.getClassVersion()))
				{
					continue;
				}
				//System.out.println("OK MatchOldClassVersion:"+ubmo.getMatchOldClassVersion()+" == "+cvmo.getClassVersion());
			}
			if(ubmo.getMatchClassName()!=null)
			{
				if(!matchClassName(ubmo.getMatchClassName(),cvmo.getClassCanonicalName()))
				{
					continue;
				}
				//System.out.println("OK MatchClassName:"+ubmo.getMatchClassName()+" == "+cvmo.getClassCanonicalName());
			}
			if(ubmo.getMatchParentMember()!=null)
			{
				if(parentLink==null)
				{
					continue;
				}
				if(!ubmo.getMatchParentMember().equals(parentLink.getMember()))
				{
					continue;
				}
			}
			if(ubmo.getMatchParentClassName()!=null)
			{
				if(parentLink==null)
				{
					continue;
				}
				CInstanceMetaObject pimo = CPersistentRoot.findMetaObjectInstance(parentLink.getParentObjectId());
				if(!matchClassName(ubmo.getMatchParentClassName(),pimo.getClassName()))
				{
					continue;
				}
			}
			if(ubmo.getMatchParentClassVersion()!=null)
			{
				if(parentLink==null)
				{
					CDebugger.debug("parentLink is null");						
					continue;
				}
				Object pobj=CPersistentRoot.findCachedObject(parentLink.getParentObjectId());
				CClassVersionMetaObject tmp=cl.findApplicationObjectClassVersion(pobj.getClass().getCanonicalName());
				CDebugger.debug("parentLink "+ubmo.getMatchParentClassVersion()+"("+ubmo.getMatchParentClassName()+") :: "+tmp.getClassVersion()+" ("+tmp.getClassCanonicalName()+")");					
				if(!matchClassVersion(ubmo.getMatchParentClassVersion(),tmp.getClassVersion()))
				{
					continue;
				}
			}
			if(ubmo.getMatchOldParentClassVersion()!=null)
			{
				if(pcvmo==null)
				{
					CDebugger.debug("PCVMO is null");					
					continue;
				}
				CDebugger.debug("PCVMO "+ubmo.getMatchOldParentClassVersion()+"  :: "+pcvmo.getClassVersion());				
				if(!matchClassVersion(ubmo.getMatchOldParentClassVersion(),pcvmo.getClassVersion()))
				{
					continue;
				}
			}
			return ubmo;
		}
		return null;
	}
	private static boolean matchClassName(String ubmo,String cvmo)
	{
		if(ubmo==null || cvmo==null)
		{
			return false;
		}
		boolean out;
		if("*".equals(ubmo))
		{
			out=true;
		}
		else if(ubmo.indexOf("*")>=0)
		{
			out=cvmo.matches(ubmo);
		}
		else
		{
			out=ubmo.equals(cvmo);
		}
		CDebugger.debug("Match ('"+ubmo+"','"+cvmo+"')="+out);
		return out;
	}
	/**
	 * 
	 * @param ubmo - UBMO's parameter
	 * @param cvmo - CVMO's data
	 * @return
	 */
	private static boolean matchClassVersion(String ubmo,String cvmo)
	{
		if(ubmo==null || cvmo==null)
		{
			return false;
		}
		if("*".equals(ubmo))
		{
			return true;
		}
		else 
		{
			String[] tmp=ubmo.split("\\|");
			if(tmp.length>1)
			{
				for(String v:tmp)
				{
					if(cvmo.equals(v.trim()))
					{
						CDebugger.debug("Match OR :: ubmo:"+ubmo+" ("+tmp.length+")   cvmo: "+cvmo+"   v="+v);
						return true;
					}
				}
				return false;
			}
			else
			{
				return cvmo.equals(ubmo);
			}
		}
	}
	/**
	 * Generates the package used for conversion classes
	 * @return
	 */
	public String genClassPackage()
	{
		return "rhp.aof4oop.framework.tmp.dynweaver";
	}
	/**
	 * Generates the class name for a conversion class of an UBMO
	 * @param ubmo
	 * @return
	 */
	public String genConversionClassName(CClassVersionMetaObject oldClassVersion,CUpdateBackdateMetaObject ubmo)
	{
		return genClassPackage()+"."+ubmo.toConversionClassName(oldClassVersion);
	}
	/**
	 * Weaves and register the conversion class from @param oldClassVersion to an output class defined in UBMO
	 * 
	 * The weaved class and method returns an object's getOutputClassName() and receive as parameter old and new object
	 * The oldObj object is a pre instantiated oldClassVersion (e.g. Person$A) while newObj has the same type of return method
	 * 
	 * Since same UBMO can be applied to several classes which share the same super class, the return type can be any of those classes
	 * 
	 * @throws IOException
	 * @throws NotFoundException
	 * @throws CannotCompileException
	 */
	public void weaveCode(CClassVersionMetaObject oldClassVersion,CUpdateBackdateMetaObject ubmo) throws IOException, NotFoundException, CannotCompileException 
	{
		weaveCode(((CClassLoader)ClassLoader.getSystemClassLoader()),oldClassVersion,ubmo);
	}
	public void weaveCode(CClassLoader cl,CClassVersionMetaObject oldClassVersion,CUpdateBackdateMetaObject ubmo) throws IOException, NotFoundException, CannotCompileException
	{
		if(cl.getClass(genConversionClassName(oldClassVersion,ubmo))!=null)
		{
			CDebugger.debug("Conversion Class is already loaded");
			return;
		}
		__weaveCode(cl,oldClassVersion,ubmo);
	}
	/**
	 * Utility for calculate the system's classpath
	 * @param p1
	 * @param p
	 * @return
	 */
	private String calcClasspath(String p0,String[] p1)
	{
		String out="";
		
		if(p0!=null)
		{
			out+=p0;
		}
		if(p1!=null && p1.length>0)
		{
			for(String p:p1)
			{
				if(out.length()>0)
				{
					out+=":";
				}
				out+=p;
			}
		}
		return out;
	}
	private void __weaveCode(CClassLoader cl,CClassVersionMetaObject oldClassVersion,CUpdateBackdateMetaObject ubmo) throws IOException, NotFoundException, CannotCompileException 
	{
		StringBuffer code = new StringBuffer();
		//doConversion:
		// parameters: dbclass outputclass 
		// return: outputclass
		code.append("package "+genClassPackage()+";");
		code.append("\nclass "+ubmo.toConversionClassName(oldClassVersion));
		code.append("\n{");
		code.append("\n public static "+ubmo.getOutputClassName()+" doConversion("+oldClassVersion.toVersionClassName()+" oldObj,"
																				  +(ubmo.getMatchParentClassName()!=null && ubmo.getMatchOldParentClassVersion()!=null?CClassVersionMetaObject.toVersionClassName(ubmo.getMatchParentClassName(),ubmo.getMatchOldParentClassVersion()):"java.lang.Object")+" parentOldObj,"
		                                                                          +ubmo.getOutputClassName()+" newObj,"
				                                                                  +(ubmo.getMatchParentClassName()!=null?ubmo.getMatchParentClassName():"java.lang.Object")+" parentNewObj"
				                                                                  +") throws Exception");
		code.append("\n {");
		code.append(  ubmo.getConversionSourceCode());
		code.append("\n }");
		code.append("\n}");
		System.out.println("Class "+ubmo.toConversionClassName(oldClassVersion)+" must be loaded");
		CDebugger.debug("CODE:"+code.toString());
		

		System.out.println("Start compiling process..");
		boolean sucess=(new CDynCompiler(calcClasspath(CDefinitions.AOF4OOP_DYN_WEAVER_CLASSPATH,CDefinitions.AOF4OOP_APPCLASSPATH),CDefinitions.AOF4OOP_DYN_WEAVER_OUTDIR)).compile(genConversionClassName(oldClassVersion,ubmo), code.toString(),cl);
		if(!sucess)
		{
			System.out.println("Error compiling Class "+ubmo.toConversionClassName(oldClassVersion));
			System.out.println("CODE:\n--------------------------------------------\n"+code.toString()+"\n--------------------------------");
			throw new EFrameworkFault("Error compiling Class "+ubmo.toConversionClassName(oldClassVersion));
		}
		System.out.println("Compiling ok");
	}
	/**
	 * Execute the waved code
	 * 
	 * @param oldObject - The old version in his class version
	 * @param oldClassVersion - Class version meta-object
	 * @param newObject - An pre instantiated object 
	 * @param ubmo - The meta-object which contains conversion code
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	public Object convert(Object oldObject,Object oldObjectParent,CClassVersionMetaObject oldClassVersion,Object newObject,Object newObjectParent,CUpdateBackdateMetaObject ubmo) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException
	{
		CClassLoader cl=((CClassLoader)ClassLoader.getSystemClassLoader());
		Class<?> clzz=cl.getClass(genConversionClassName(oldClassVersion,ubmo));
		Class<?> old_clzz=cl.getClass(oldClassVersion.toVersionClassName());
		if(old_clzz==null)
		{
			throw new EFrameworkFault("The class "+oldClassVersion.toVersionClassName()+" does not exists");
		}
		Class<?> new_clzz=cl.loadClass(ubmo.getOutputClassName()); // get the Class, if necessary is loaded. It may note yet be loaded
		if(new_clzz==null)
		{
			throw new EFrameworkFault("The output class "+ubmo.getOutputClassName()+" does not exists");
		}		
		System.out.print("old class:"+oldClassVersion.toVersionClassName()+" ("+(oldObject!=null?"Ok":"Null")+")");
		System.out.print(" -->  ");
		System.out.print("new class:"+ubmo.getOutputClassName()+" ("+(newObject!=null?newObject.getClass().getCanonicalName():"???")+")");
		System.out.println();
		Method m=clzz.getMethod("doConversion",old_clzz,
				                               (ubmo.getMatchParentClassName()!=null && ubmo.getMatchOldParentClassVersion()!=null?cl.getClass(CClassVersionMetaObject.toVersionClassName(ubmo.getMatchParentClassName(),ubmo.getMatchOldParentClassVersion())):Object.class),
				                               new_clzz,
				                               (ubmo.getMatchParentClassName()!=null?cl.getClass(ubmo.getMatchParentClassName()):Object.class));
		m.setAccessible(true);
		m.invoke(null,oldObject,oldObjectParent,newObject,newObjectParent);
		
		return newObject;
	}
	/**
	 * Loads UBMO database
	 * @param verbose - If true, error and warning messages are displayed to console
	 * @return
	 * @throws JAXBException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static CInstanceAdaptationMetadata loadSchemaEvolution(boolean verbose) throws JAXBException,ParserConfigurationException, IOException, SAXException
	{
		int n=0;
		for(ParserMessage msg:validate(verbose))
		{
			if(msg.isError())
			{
				n++;
			}
		}
		if(n>0)
		{
			throw new EFrameworkFault("UBMO database is not valid: "+n+" error(s)");
		}
		
		
		File file = new File(CDefinitions.AOF4OOP_UBMODB);
		JAXBContext jaxbContext = JAXBContext.newInstance(CInstanceAdaptationMetadata.class); 
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		CInstanceAdaptationMetadata iamd=(CInstanceAdaptationMetadata) jaxbUnmarshaller.unmarshal(file);
		if(verbose)
		{
			System.out.println("UBMO database is valid - Version:"+iamd.getVersion()+"\t Total of UBMO:"+iamd.getUbmo().length+"\t warnings(s): "+n);
		}
		return iamd;
	}
	/**
	 * Validates UBMD database
	 * JABX already validates XML.
	 * However, with this our validator we can an higher level of verbosity
	 * @param verbose
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static ArrayList<ParserMessage> validate(boolean verbose) throws ParserConfigurationException,SAXException,IOException
	{
		ArrayList<ParserMessage> out=new ArrayList<ParserMessage>();
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setValidating(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		builder.setErrorHandler(new ParserErrorHandler(out, verbose));
		@SuppressWarnings("unused")
		Document doc = builder.parse(CDefinitions.AOF4OOP_UBMODB);
		return out;
	}

}
/**
 * Message of XML parsing
 * @author rhp
 *
 */
class ParserMessage
{
	private String type;
	private boolean error;
	private int line;
	private int column;
	private String message;
	
	public ParserMessage(String type,boolean error, int line, int column, String message) 
	{
		super();
		this.type = type;
		this.error=error;
		this.line = line;
		this.column = column;
		this.message = message;
		
	}

	public String getType() {
		return type;
	}

	public boolean isError() {
		return error;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	public String getMessage() {
		return message;
	}
}
/**
 * Customized Error handler
 * @author rhp
 *
 */
class ParserErrorHandler implements ErrorHandler
{
	private ArrayList<ParserMessage> out;
	private boolean verbose;
	public ParserErrorHandler(ArrayList<ParserMessage> out,boolean verbose) 
	{
		this.out=out;
		this.verbose=verbose;
	}
	/*@Override*/
	public void warning(SAXParseException exception) throws SAXException 
	{
		handleMsg("Warning",false,exception);
	}
	/*@Override*/
	public void error(SAXParseException exception) throws SAXException 
	{
		handleMsg("Error",true,exception);		
	}
	/*@Override*/
	public void fatalError(SAXParseException exception) throws SAXException 
	{
		handleMsg("Fatal error",true,exception);
	}
	private void handleMsg(String type,boolean error,SAXParseException exception)
	{
		if(verbose)
		{
			System.out.println("Error[Line "+exception.getLineNumber()+" Col "+exception.getColumnNumber()+"]: "+exception.getMessage());
		}
		out.add(new ParserMessage(type,error, exception.getLineNumber(), exception.getColumnNumber(),exception.getMessage()));
	}
}
/**
When you must validate a DTD via JAXB using SAX source you must configure SAXParserFactory:

SAXParserFactory parserFactory = SAXParserFactory.newInstance();
[b]parserFactory.setValidating(true); // (1)[/b]
SAXParser saxParser = parserFactory.newSAXParser();
XMLReader xmlReader = saxParser.getXMLReader();
EntityResolver entityResolver = new EntityResolver()
{
public InputSource resolveEntity (String publicId, String systemId)
{
// some local resource via getClass().getResourceAsStream() for example
return ....; //
}
};
[b]xmlReader.setEntityResolver(entityResolver); // (2)[/b]
SAXSource saxSource = new SAXSource(xmlReader, source);
Unmarshaller unmarshaller = JAXBContext.newInstance("somepackage").createUnmarshaller();
ValidationEventCollector eventCollector = new ValidationEventCollector();
unmarshaller.setEventHandler(eventCollector);
Objec umarshalledSata = unmarshaller.unmarshal(saxSource);
if (eventCollector.hasEvents())
{
for(ValidationEvent event: eventCollector.getEvents())
log.error(event.getMessage(), event.getLinkedException());
throw ....; // here throw an exception or what you want
}

If you want to disable DTD validation, turn off via:

[b]parserFactory.setValidating(false);// from (1)[/b]

If this does not work remove line with mark (2).
*/