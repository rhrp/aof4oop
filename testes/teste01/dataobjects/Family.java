package rhp.aof4oop.dataobjects;

import java.util.Date;

public class Family 
{
	private Person father;
	private Person mother;
	private Person[] childs;
	private Date weddingDate;

	public Family()
	{
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
		return "Family:{"+(getFather()!=null?getFather().toString()+"  / ":"")+(getMother()!=null?getMother().toString()+"  / ":"")+"  WD:"+(getWeddingDate()!=null?getWeddingDate().toString():"null")+"}";
	}
}
