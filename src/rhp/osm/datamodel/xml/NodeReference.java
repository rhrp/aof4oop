package rhp.osm.datamodel.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "A")
@XmlRootElement(name = "nd")
public class NodeReference 
{
		private long ref;
		@XmlAttribute
		public long getRef() {
			return ref;
		}
		public void setRef(long ref) {
			this.ref = ref;
		}
}