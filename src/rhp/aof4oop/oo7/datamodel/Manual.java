package rhp.aof4oop.oo7.datamodel;

public class Manual 
{
	private String title;
	private int	id;
	private String text;
	private int textLength;
	private Module module;
	
	public Manual()
	{
		super();
	}
	public Manual(int id,Module module) 
	{
		super();
		this.title = "title manual "+id;
		this.id = id;
		this.text = " texto manual "+id;
		this.textLength = text.length();
		this.module = module;
	}
	public String getTitle()
	{
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getTextLength() {
		return textLength;
	}
	public void setTextLength(int textLength) {
		this.textLength = textLength;
	}
	public Module getModule() {
		return module;
	}
	
	
	
}
