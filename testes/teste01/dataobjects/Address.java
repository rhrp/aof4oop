package rhp.aof4oop.dataobjects;

public class Address 
{
	private String morada;
	private int numero;
	
	public Address()
	{
		super();
	}
	public Address(String morada, int numero)
	{
		super();
		this.morada = morada;
		this.numero = numero;
	}
	public String getMorada() {
		return morada;
	}
	public void setMorada(String morada) 
	{
		this.morada = morada;
	}
	public int getNumero() {
		return numero;
	}
	public void setNumero(int numero) {
		this.numero = numero;
	}
	public String toString()
	{
		return "Address:{"+morada+", "+numero+"}";
	}
}
