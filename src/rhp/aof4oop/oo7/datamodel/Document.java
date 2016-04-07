package rhp.aof4oop.oo7.datamodel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rhp.aof4oop.framework.core.annotations.Aof4oopVersionAlias;
import rhp.aof4oop.oo7.benchmarck.GeneralParameters;

@Aof4oopVersionAlias(alias = "oo7")
public class Document {

	private String title = null;
	private int id;
	private String text = null;
	// relationship: documentation
	private CompositePart part = null;

	public Document()
	{
		super();
	}
	public Document(int newId, CompositePart compositePart) 
	{
		setPart(compositePart);
		setId(newId);

		// pvz: TODO: Set title and text using a large document.

		// prepare and fill in the document title
		// TODO: NOTE: add more functionality to determine manual size etc.
		// I deviated here!
		setTitle(GeneralParameters.documentText + " " + newId);
		setText(GeneralParameters.documentText);


		// setTextLength(GeneralParameters.manualText.length());

	}

	/**
	 * @return 30-Apr-2006
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return 30-Apr-2006
	 */
	public CompositePart getPart() {
		return part;
	}

	public void setPart(CompositePart part) {
		this.part = part;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 
	 * NOT REALLY PART OF THE BASIC MODEL utility method
	 * 
	 * 
	 * Used in traversals
	 * 
	 * @param charToFind
	 * @return 24-Apr-2006
	 */
	public int searchText(String charToFind)
	{
		// count occurrences of the indicated letter (for busy work)
		int count = 0;

		// TODO: GET NUMBER OF OCCURENCES: check for correctness!!!!!!pvz
		Pattern pattern = Pattern.compile(charToFind);
		Matcher matcher = pattern.matcher(getText());
		while (matcher.find()) 
		{
			count++;
		}


		return count;

	}

	/**
	 * 
	 * original comment:
	 * 
	 * replaceText Method for use in traversals
	 * 
	 * @param oldString
	 * @param newString
	 * @return 24-Apr-2006
	 */
	public int replaceText(String oldString, String newString) 
	{
		// check to see if the text starts with the old string
		boolean foundMatch = false;

		// if so, change it to start with the new string instead
		foundMatch = (getText().indexOf(oldString) == 0);
		if (foundMatch) 
		{
			getText().replaceAll(oldString, newString);
		}
		if (foundMatch) 
		{
			return 1;
		} 
		else 
		{
			return 0;
		}
	}

}

