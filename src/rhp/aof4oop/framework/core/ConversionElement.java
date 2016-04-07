package rhp.aof4oop.framework.core;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "conversion")
public class ConversionElement 
{
		private String sourceCode;
		private boolean applyDefault;
		private String outputClassName;
		
		public ConversionElement() 
		{
			super();
		}
		@XmlElement(name = "sourceCode")
		public void setSourceCode(String sourceCode) 
		{
			this.sourceCode = sourceCode;
		}
		@XmlAttribute(name="applyDefault")
		public void setApplyDefault(boolean applyDefault) 
		{
			this.applyDefault = applyDefault;
		}
		
		public String getSourceCode() 
		{
			return sourceCode;
		}
		public boolean isApplyDefault() 
		{
			return applyDefault;
		}
		@XmlAttribute(name="outputClassName")
		public void setOutputClassName(String outputClassName) 
		{
			this.outputClassName = outputClassName;
		}
		public String getOutputClassName() 
		{
			return outputClassName;
		}
}