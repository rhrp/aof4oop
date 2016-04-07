package rhp.aof4oop.dataobjects;

import java.util.Date;

import rhp.aof4oop.framework.core.annotations.Aof4oopDefault;
import rhp.aof4oop.framework.core.annotations.Aof4oopNotNull;
import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;

@Aof4oopVersionAlias(alias = "E")
public class Family 
{
	@Aof4oopDefault(value = "não definido",classVersion={"*"})
	@Aof4oopNotNull(message="The family name canot be null")
	private String name;
	@Aof4oopDefault(value = "7",classVersion={"C","E","*"})
	private int    xpto;
	private Person father;
	private Person mother;
	private Person[] childs;
	private Date weddingDate;

	public Family()
	{
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getXpto() {
		return xpto;
	}

	public void setXpto(int xpto) {
		this.xpto = xpto;
	}

	public Person getFather() {
		return father;
	}
	public void setFather(Person father) {
		this.father = father;
	}
	public Person getMother() {
		return mother;
	}
	public void setMother(Person mother) {
		this.mother = mother;
	}
	public Person[] getChilds() {
		return childs;
	}
	public void setChilds(Person[] childs) {
		this.childs = childs;
	}
	public Date getWeddingDate() {
		return weddingDate;
	}
	public void setWeddingDate(Date weddingDate) {
		this.weddingDate = weddingDate;
	}
	public String toString()
	{
		return "Family:{"+(getFather()!=null?getFather().toString()+"  / ":"")+(getMother()!=null?getMother().toString()+"  / ":"")+"  WD:"+(getWeddingDate()!=null?getWeddingDate().toString():"null")+(getChilds()!=null?" Childs:"+getChilds().length:"null")+"}";
	}
}
