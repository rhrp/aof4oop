package rhp.aof4oop.framework.core.datamodel;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import rhp.aof4oop.framework.core.ConversionElement;


@XmlRootElement(name = "ubmo")
public class CUpdateBackdateMetaObject extends CMetaObject
{
	private String name;						// UBMO's name
	private String matchClassName;				// app class name
	private String matchSuperClassName;			// app super class name
	private String matchCurrentClassVersion;	// app class version
	private String matchOldClassVersion;		// old class version
	private String matchOldParentClassVersion;	// parent's class version of old database object 
	private String matchParentClassName;		// app parent class name
	private String matchParentClassVersion;		// app parent class version
	private String matchParentMember;			// parent's member that points to the converted object
	private ConversionElement conversionElement;

	
	public CUpdateBackdateMetaObject() 
	{
		super();
	}
	@XmlAttribute(name="name",required=true)
	public String getName() 
	{
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@XmlAttribute
	public String getMatchClassName() 
	{
		return matchClassName;
	}
	public void setMatchClassName(String className) 
	{
		this.matchClassName = className;
	}
	@XmlAttribute
	public String getMatchSuperClassName() 
	{
		return matchSuperClassName;
	}
	public void setMatchSuperClassName(String currentSuperClassName) 
	{
		this.matchSuperClassName = currentSuperClassName;
	}
	@XmlAttribute
	public String getMatchCurrentClassVersion()
	{
		return matchCurrentClassVersion;
	}
	public void setMatchCurrentClassVersion(String currentClassVersion) 
	{
		this.matchCurrentClassVersion = currentClassVersion;
	}
	@XmlAttribute
	public String getMatchOldClassVersion() 
	{
		return matchOldClassVersion;
	}
	public void setMatchOldClassVersion(String oldClassVersion) 
	{
		this.matchOldClassVersion = oldClassVersion;
	}
	@XmlElement(name="conversion")
	public ConversionElement getConversionElement()
	{
		return conversionElement;
	}
	public void setConversionElement(ConversionElement conversionElement) 
	{
		this.conversionElement = conversionElement;
	}
	public boolean isApplyDefault()
	{
		return (getConversionElement()!=null?getConversionElement().isApplyDefault():false);
	}
	public String getConversionSourceCode()
	{
		return (getConversionElement()!=null?getConversionElement().getSourceCode():null);
	}
	public String getOutputClassName()
	{
		return (getConversionElement()!=null?getConversionElement().getOutputClassName():null);
	}

	@XmlAttribute(name="matchParentClassName")
	public String getMatchParentClassName() {
		return matchParentClassName;
	}
	public void setMatchParentClassName(String matchParentClassName) {
		this.matchParentClassName = matchParentClassName;
	}
	@XmlAttribute(name="matchParentClassVersion")
	public String getMatchParentClassVersion() {
		return matchParentClassVersion;
	}
	public void setMatchParentClassVersion(String matchParentClassVersion) {
		this.matchParentClassVersion = matchParentClassVersion;
	}
	@XmlAttribute(name="matchOldParentClassVersion")
	public String getMatchOldParentClassVersion() {
		return matchOldParentClassVersion;
	}
	public void setMatchOldParentClassVersion(String matchOldParentClassVersion) {
		this.matchOldParentClassVersion = matchOldParentClassVersion;
	}
	@XmlAttribute(name="matchParentMember")
	public String getMatchParentMember() {
		return matchParentMember;
	}
	public void setMatchParentMember(String matchParentMember) {
		this.matchParentMember = matchParentMember;
	}
	public String toConversionClassName(CClassVersionMetaObject oldClassVersion)
	{
		return "CUBMOConversion$"+getName()+"$"+oldClassVersion.toVersionClassCanonicalName().replace(".","$");
	}
}
