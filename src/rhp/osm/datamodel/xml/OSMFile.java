package rhp.osm.datamodel.xml;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "osm")
public class OSMFile 
{
	private String version;
	private String generator;
	private Bounds bounds;
	private Node[] nodes;
	private Way[] ways;
	private Relation[] relations;
	
	public OSMFile()
	{
		version=null;
		generator=null;
	}
	@XmlAttribute
	public String getVersion() 
	{
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	@XmlAttribute
	public String getGenerator() 
	{
		return generator;
	}
	public void setGenerator(String generator) 
	{
		this.generator = generator;
	}
	
	public Bounds getBounds() {
		return bounds;
	}
	public void setBounds(Bounds bounds) {
		this.bounds = bounds;
	}
	@XmlElements(value = { @XmlElement(name="node")})
	public Node[]  getNodes() {
		return nodes;
	}
	public void setNodes(Node[] nodes) 
	{
		this.nodes = nodes;
	}
	@XmlElements(value = { @XmlElement(name="way")})
	public Way[] getWays() {
		return ways;
	}
	public void setWays(Way[] ways) {
		this.ways = ways;
	}
	@XmlElements(value = { @XmlElement(name="relation")})
	public Relation[] getRelations() {
		return relations;
	}
	public void setRelations(Relation[] relations) {
		this.relations = relations;
	}
	public static OSMFile load(String filename) throws JAXBException
	{
		File file = new File(filename);
		JAXBContext jaxbContext = JAXBContext.newInstance(OSMFile.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return (OSMFile) jaxbUnmarshaller.unmarshal(file);
	}
}
