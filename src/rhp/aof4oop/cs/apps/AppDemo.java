package rhp.aof4oop.cs.apps;

import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import rhp.aof4oop.cs.datamodel.Person;
import rhp.aof4oop.cs.utils.PersonCol;
/** Version B */
import rhp.aof4oop.cs.datamodel.Staff;
/* */
import rhp.aof4oop.cs.datamodel.Student;
import rhp.aof4oop.cs.utils.StudentCol;
import rhp.aof4oop.cs.utils.InitDB;
import rhp.aof4oop.framework.core.CPersistentRoot;


/**
 *    
 * @author rhp
 * 
 * This is the case study app that we refer in the published papers.
 * Along the source code the user can change between versions A and B.
 * Besides, the persistence mechanisms also can be changed between the ones provided by AOF4OOP and db4o
 * 
 * Using Eclipse:
 * Don't forget to add the parameter
 * -Djava.system.class.loader=rhp.aof4oop.framework.core.CClassLoader		
 * 
 * Or
 * 
 *	CClassLoader cl=new CClassLoader();
 *	Thread.currentThread().setContextClassLoader(cl);
 *
 * Using Ant, run "srec" task:
 * ant rsec
 */
public class AppDemo extends JFrame implements ActionListener,ItemListener 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//Staff
	private JTextField textField_staff_title,textField_staff_firstName,textField_staff_address,textField_staff_postCode,textField_staff_phone,textField_staff_mobile,textField_staff_fax;
	/** Version A
	private JTextField textField_staff_surname;
	*/
	/** Version B */   
	private JTextField textField_staff_lastName,textField_staff_middleName;
	/* */
	private JComboBox<String> listBox_staff_category;
	private JCheckBox checkBox_staff_d32,checkBox_staff_d34,checkBox_staff_d36;
	//Student
	private JTextField textField_student_firstName,textField_student_lastName,textField_student_middleName;
	private JComboBox<String> listBox_student_gender;
	private JFormattedTextField dateField_student;
	//Menu
	private JButton[] btn_apply=new JButton[2];
	private JButton[] btn_remove=new JButton[2];
	private JButton[] btn_next=new JButton[2];
	private JButton[] btn_prev=new JButton[2];
	private JButton[] btn_new=new JButton[2];
	private JButton[] btn_first=new JButton[2];
	private JButton[] btn_last=new JButton[2];
	private JTextField[] textField_search=new JTextField[2];
	private JLabel[] label_person_index=new JLabel[2];
	
	
    protected static final String textFieldString = "JTextField";
    protected static final String passwordFieldString = "JPasswordField";
    protected static final String ftfString = "JFormattedTextField";
    protected static final String buttonString = "JButton";
    
    private StatusBar statusBar;
    
    
	private int line_heigth=27;
	private int labels_width=90;
	private Dimension labels_dim=new Dimension(labels_width,line_heigth-5);
    
    private static PersonCol persons;		// Database of persons
    private static StudentCol students;		// Database of students
    private static int[] person_index={0,0};				// The Person being edited
    
    /**
     * AOF4OOP
     */
    private static CPersistentRoot psRoot=new CPersistentRoot();
    private static String frameworkName	= "AOF4OOP";
    
    /**
     * db40
     * psRoot=new CPersistentRoot(); 		
     */
    
    private static AppDemo frameApp;
    private static int STUDENT_TAB=0;
    private static int STAFF_TAB=1;
    
	public AppDemo(String title) 
	{
		super();
		setTitle(title);
		initDB();
	}
	private void initControlsStaff(JPanel frame)
	{
		int vertical_pos=10;
		JLabel label_title,label_name,label_address,label_postCode,label_phone,label_mobile,label_fax,label_category;
		
		KeyAdapter key_listner=new KeyAdapter()
		{
			public void keyPressed(KeyEvent ke)
			{
				if(!(ke.getKeyChar()==27 || ke.getKeyChar()==65535))//this section will execute only when user is editing the JTextField
				{
					System.out.println("User is editing something in Staff's TextField");
					markObjectAsDirty(STAFF_TAB,true);
				}
			}
		};
		frame.setLayout(null);
		//frame.setBackground(new Color(200, 200,200));

		Container container_title=new JPanel();
		label_title = new JLabel("Title",null,JLabel.LEFT);
		label_title.setPreferredSize(labels_dim);
		container_title.add(label_title);
		textField_staff_title = new JTextField(4);
		textField_staff_title.addActionListener(this);
		textField_staff_title.addKeyListener(key_listner);
		container_title.add(textField_staff_title);
		addComponentsToPane(frame,10,vertical_pos,container_title);
		vertical_pos+=line_heigth;		
		
		Container container_name=new JPanel();
		label_name = new JLabel("Name",null,JLabel.LEFT);
		label_name.setPreferredSize(labels_dim);
		container_name.add(label_name);
		textField_staff_firstName = new JTextField(10);
		textField_staff_firstName.addActionListener(this);
		textField_staff_firstName.addKeyListener(key_listner);
		container_name.add(textField_staff_firstName);

		/** Version A
		textField_staff_surname = new JTextField(10);
		textField_staff_surname.addActionListener(this);
		textField_staff_surname.addKeyListener(key_listner);
		container_name.add(textField_staff_surname);
		*/
		
		/** Version B */ 
		textField_staff_middleName = new JTextField(10);
		textField_staff_middleName.addActionListener(this);
		textField_staff_middleName.addKeyListener(key_listner);
		container_name.add(textField_staff_middleName);

		textField_staff_lastName = new JTextField(10);
		textField_staff_lastName.addActionListener(this);
		container_name.add(textField_staff_lastName);
		/* */
		
		addComponentsToPane(frame,10,vertical_pos,container_name);
		vertical_pos+=line_heigth;
		
		Container container_address=new JPanel();
		label_address = new JLabel("Address",null,JLabel.LEFT);
		label_address.setPreferredSize(labels_dim);
		container_address.add(label_address);
		textField_staff_address = new JTextField(30);
		textField_staff_address.addActionListener(this);
		textField_staff_address.addKeyListener(key_listner);
		container_address.add(textField_staff_address);
		addComponentsToPane(frame,10,vertical_pos,container_address);
		vertical_pos+=line_heigth;		

		Container container_postCode=new JPanel();
		label_postCode = new JLabel("Post Code",null,JLabel.LEFT);
		label_postCode.setPreferredSize(labels_dim);
		container_postCode.add(label_postCode);
		textField_staff_postCode = new JTextField(10);
		textField_staff_postCode.addActionListener(this);
		textField_staff_postCode.addKeyListener(key_listner);
		container_postCode.add(textField_staff_postCode);
		addComponentsToPane(frame,10,vertical_pos,container_postCode);
		vertical_pos+=line_heigth;	
		
		
		Container container_phone=new JPanel();
		label_phone = new JLabel("Phone",null,JLabel.LEFT);
		label_phone.setPreferredSize(labels_dim);
		container_phone.add(label_phone);
		textField_staff_phone = new JTextField(20);
		textField_staff_phone.addActionListener(this);
		textField_staff_phone.addKeyListener(key_listner);
		container_phone.add(textField_staff_phone);
		addComponentsToPane(frame,10,vertical_pos,container_phone);
		vertical_pos+=line_heigth;	
		
		
		Container container_mobile=new JPanel();
		label_mobile = new JLabel("Mobile",null,JLabel.LEFT);
		label_mobile.setPreferredSize(labels_dim);
		container_mobile.add(label_mobile);
		textField_staff_mobile = new JTextField(20);
		textField_staff_mobile.addActionListener(this);
		textField_staff_mobile.addKeyListener(key_listner);
		container_mobile.add(textField_staff_mobile);
		addComponentsToPane(frame,10,vertical_pos,container_mobile);
		vertical_pos+=line_heigth;	
		
		Container container_fax=new JPanel();
		label_fax = new JLabel("Fax   ",null,JLabel.LEFT);
		label_fax.setPreferredSize(labels_dim);
		container_fax.add(label_fax);
		textField_staff_fax = new JTextField(20);
		textField_staff_fax.addActionListener(this);
		textField_staff_fax.addKeyListener(key_listner);
		container_fax.add(textField_staff_fax);
		addComponentsToPane(frame,10,vertical_pos,container_fax);
		vertical_pos+=line_heigth;	
		
		
		Container container_category=new JPanel(); 
		label_category=new JLabel("Category  ",null,JLabel.LEFT);
		label_category.setPreferredSize(labels_dim);
		container_category.add(label_category);
		String[] catStrings=new String[]{"Principal","Coordinator","ExamOfficer","Tutor","Moderator"};
		listBox_staff_category = new JComboBox<String>(catStrings);
		listBox_staff_category.setSelectedIndex(4);
		listBox_staff_category.addActionListener(this);
		container_category.add(listBox_staff_category);
		addComponentsToPane(frame,10,vertical_pos,container_category);
		vertical_pos+=line_heigth;

		Container container_submenu=new JPanel(new GridLayout(1,2));
		btn_remove[STAFF_TAB] = new JButton("Delete",null);
		btn_remove[STAFF_TAB].setVerticalTextPosition(AbstractButton.CENTER);
		btn_remove[STAFF_TAB].setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
		btn_remove[STAFF_TAB].setMnemonic(KeyEvent.VK_R);
		btn_remove[STAFF_TAB].setActionCommand("btn_remove_"+STAFF_TAB);
		btn_remove[STAFF_TAB].addActionListener(this);
		btn_remove[STAFF_TAB].setVisible(true);
		container_submenu.add(btn_remove[STAFF_TAB]);
		
		btn_apply[STAFF_TAB] = new JButton("Apply changes",null);
		btn_apply[STAFF_TAB].setVerticalTextPosition(AbstractButton.CENTER);
		btn_apply[STAFF_TAB].setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
		btn_apply[STAFF_TAB].setMnemonic(KeyEvent.VK_A);
		btn_apply[STAFF_TAB].setActionCommand("btn_apply_"+STAFF_TAB);
		btn_apply[STAFF_TAB].addActionListener(this);
		btn_apply[STAFF_TAB].setVisible(true);
		container_submenu.add(btn_apply[STAFF_TAB]);
		
		vertical_pos+=line_heigth;//double space
		addComponentsToPane(frame,10,vertical_pos,container_submenu);
		vertical_pos+=line_heigth;	
		


		checkBox_staff_d32 = new JCheckBox("D32");
    	checkBox_staff_d32.setMnemonic(KeyEvent.VK_X); 
    	checkBox_staff_d32.setSelected(false);
    	checkBox_staff_d32.addItemListener(this);
    	
    	checkBox_staff_d34 = new JCheckBox("D34");
    	checkBox_staff_d34.setMnemonic(KeyEvent.VK_Y); 
    	checkBox_staff_d34.setSelected(false);
    	checkBox_staff_d34.addItemListener(this);

    	checkBox_staff_d36 = new JCheckBox("D36");
    	checkBox_staff_d36.setMnemonic(KeyEvent.VK_Z); 
    	checkBox_staff_d36.setSelected(false);
    	checkBox_staff_d36.addItemListener(this);

    	JPanel checkPanel = new JPanel(new GridLayout(0, 1));
    	checkPanel.add(checkBox_staff_d32);
    	checkPanel.add(checkBox_staff_d34);
    	checkPanel.add(checkBox_staff_d36);

		checkPanel.setPreferredSize(new Dimension(240,110));
		checkPanel.setBorder(BorderFactory.createTitledBorder("Passed Qualifications"));
    
		addComponentsToPane(frame,360,100,checkPanel);


		addComponentsToPane(frame,10,300,createMenu(STAFF_TAB));
	}

	
	private void initControlsStudents(JPanel frame)
	{
		JLabel label_name,label_gender,label_birth;
		int vertical_pos=10;
		frame.setLayout(null);
		
		KeyAdapter key_listner=new KeyAdapter()
		{
			public void keyPressed(KeyEvent ke)
			{
				if(!(ke.getKeyChar()==27 || ke.getKeyChar()==65535))//this section will execute only when user is editing the JTextField
				{
					System.out.println("User is editing something in Student's TextField");
					markObjectAsDirty(STUDENT_TAB,true);
				}
			}
		};
		
		Container container_name=new JPanel();
		label_name = new JLabel("Name",null,JLabel.LEFT);
		label_name.setPreferredSize(labels_dim);
		container_name.add(label_name);
		textField_student_firstName = new JTextField(10);
		textField_student_firstName.addActionListener(this);
		textField_student_firstName.addKeyListener(key_listner);
		container_name.add(textField_student_firstName);
 
		textField_student_middleName = new JTextField(10);
		textField_student_middleName.addActionListener(this);
		textField_student_middleName.addKeyListener(key_listner);
		container_name.add(textField_student_middleName);

		textField_student_lastName = new JTextField(10);
		textField_student_lastName.addActionListener(this);
		textField_student_lastName.addKeyListener(key_listner);
		container_name.add(textField_student_lastName);
		
		addComponentsToPane(frame,10,vertical_pos,container_name);
		vertical_pos+=line_heigth;
		
		Container container_birth=new JPanel();
		label_birth=new JLabel("Birth date ",null,JLabel.LEFT);
		label_birth.setPreferredSize(labels_dim);
		container_birth.add(label_birth);
		dateField_student = new JFormattedTextField(new SimpleDateFormat("dd/MM/yyyy"));
		dateField_student.setColumns(11);
		dateField_student.setValue(new Date()); // today. It will be updated
		dateField_student.addActionListener(this);
		dateField_student.addKeyListener(key_listner);
		container_birth.add(dateField_student);
		addComponentsToPane(frame,10,vertical_pos,container_birth);
		vertical_pos+=line_heigth;
		
		
		Container container_gender=new JPanel(); 
		label_gender=new JLabel("Gender ",null,JLabel.LEFT);
		label_gender.setPreferredSize(labels_dim);
		container_gender.add(label_gender);
		String[] catStrings=new String[]{"M","F"};
		//Create the combo box, select the item at index 4.
		//Indices start at 0, so 4 specifies the pig.
		listBox_student_gender = new JComboBox<String>(catStrings);
		listBox_student_gender.setSelectedIndex(0);
		listBox_student_gender.addActionListener(this);
		container_gender.add(listBox_student_gender);
		addComponentsToPane(frame,10,vertical_pos,container_gender);
		vertical_pos+=line_heigth;
		
		
		JLabel picture;
		//Set up the picture.
		picture = new JLabel();
		picture.setFont(picture.getFont().deriveFont(Font.ITALIC));
		picture.setHorizontalAlignment(JLabel.CENTER);
		updateLabel(picture,"Student name");
		picture.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
		addComponentsToPane(frame,500,10,picture);
		
		
		Container container_submenu=new JPanel(new GridLayout(1,2));
		btn_remove[STUDENT_TAB] = new JButton("Delete",null);
		btn_remove[STUDENT_TAB].setVerticalTextPosition(AbstractButton.CENTER);
		btn_remove[STUDENT_TAB].setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
		btn_remove[STUDENT_TAB].setMnemonic(KeyEvent.VK_R);
		btn_remove[STUDENT_TAB].setActionCommand("btn_remove_"+STUDENT_TAB);
		btn_remove[STUDENT_TAB].addActionListener(this);
		btn_remove[STUDENT_TAB].setVisible(true);
		container_submenu.add(btn_remove[STUDENT_TAB]);
		
		btn_apply[STUDENT_TAB] = new JButton("Apply changes",null);
		btn_apply[STUDENT_TAB].setVerticalTextPosition(AbstractButton.CENTER);
		btn_apply[STUDENT_TAB].setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
		btn_apply[STUDENT_TAB].setMnemonic(KeyEvent.VK_A);
		btn_apply[STUDENT_TAB].setActionCommand("btn_apply_"+STUDENT_TAB);
		btn_apply[STUDENT_TAB].addActionListener(this);
		btn_apply[STUDENT_TAB].setVisible(true);
		container_submenu.add(btn_apply[STUDENT_TAB]);
		
		
		vertical_pos+=line_heigth;//double space
		addComponentsToPane(frame,10,vertical_pos,container_submenu);
		vertical_pos+=line_heigth;	
		
		
		addComponentsToPane(frame,10,300,createMenu(STUDENT_TAB));
	}
	/**
	 * Create the menu container and its controls
	 * @return
	 */
	private Container createMenu(int tab)
	{
		ImageIcon leftButtonIcon = null;//createImageIcon("images/right.gif");
		
		Container container_menu=new JPanel();
		
		
		btn_first[tab] = new JButton("|<", leftButtonIcon);
		btn_first[tab].setVerticalTextPosition(AbstractButton.CENTER);
		btn_first[tab].setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
		btn_first[tab].setMnemonic(KeyEvent.VK_P);
		btn_first[tab].setActionCommand("btn_first_"+tab);
		btn_first[tab].addActionListener(this);
		container_menu.add(btn_first[tab]);
		
		btn_prev[tab] = new JButton("<<", leftButtonIcon);
		btn_prev[tab].setVerticalTextPosition(AbstractButton.CENTER);
		btn_prev[tab].setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
		btn_prev[tab].setMnemonic(KeyEvent.VK_P);
		btn_prev[tab].setActionCommand("btn_prev_"+tab);
		btn_prev[tab].addActionListener(this);
		container_menu.add(btn_prev[tab]);
		
		label_person_index[tab]=new JLabel("? of ?",null,JLabel.CENTER);
		label_person_index[tab].setPreferredSize(new Dimension(90, 24));
		label_person_index[tab].setBorder(BorderFactory.createLineBorder(Color.black));
		container_menu.add(label_person_index[tab]);
		
		btn_next[tab] = new JButton(">>", leftButtonIcon);
		btn_next[tab].setVerticalTextPosition(AbstractButton.CENTER);
		btn_next[tab].setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
		btn_next[tab].setMnemonic(KeyEvent.VK_N);
		btn_next[tab].setActionCommand("btn_next_"+tab);
		btn_next[tab].addActionListener(this);
		container_menu.add(btn_next[tab]);
		
		btn_last[tab] = new JButton(">|", leftButtonIcon);
		btn_last[tab].setVerticalTextPosition(AbstractButton.CENTER);
		btn_last[tab].setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
		btn_last[tab].setMnemonic(KeyEvent.VK_P);
		btn_last[tab].setActionCommand("btn_last_"+tab);
		btn_last[tab].addActionListener(this);
		container_menu.add(btn_last[tab]);
		
		btn_new[tab] = new JButton("+", leftButtonIcon);
		btn_new[tab].setVerticalTextPosition(AbstractButton.CENTER);
		btn_new[tab].setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
		btn_new[tab].setMnemonic(KeyEvent.VK_C);
		btn_new[tab].setActionCommand("btn_new_"+tab);
		btn_new[tab].addActionListener(this);
		container_menu.add(btn_new[tab]);
		
		ImageIcon searchButtonIcon = new ImageIcon("images/search.gif");
		textField_search[tab]=new JTextField(20);
		textField_search[tab].addActionListener(this);
		container_menu.add(textField_search[tab]);
		JButton btn_search = new JButton(null, searchButtonIcon);
		btn_search.setBorder(BorderFactory.createEmptyBorder());
		btn_search.setContentAreaFilled(false);
		btn_search.setBorderPainted(false);
		btn_search.setActionCommand("btn_search_"+tab);
		btn_search.addActionListener(this);
		container_menu.add(btn_search);
		
		return container_menu;
	}
	/** Listens to the controls events */
	public void actionPerformed(ActionEvent e) 
	{
		System.out.println("Command: ["+e.getActionCommand()+"]");

        if (textField_staff_firstName==e.getSource()) 
        {
            //JTextField source = (JTextField)e.getSource();
            System.out.println("textField_firstName="+textField_staff_firstName.getText());
        }
        /** Version A
        else if (textField_staff_surname==e.getSource()) 
        {
            //JTextField source = (JTextField)e.getSource();
            System.out.println("textField_staff_surname="+textField_staff_surname.getText());
        } 
        */
        /** Version B */ 
        else if (textField_staff_middleName==e.getSource()) 
        {
            //JTextField source = (JTextField)e.getSource();
            System.out.println("textField_middleName="+textField_staff_middleName.getText());
        }
        else if (textField_staff_lastName==e.getSource()) 
        {
            //JTextField source = (JTextField)e.getSource();
            System.out.println("textField_staff_lastName="+textField_staff_lastName.getText());
        }  
        /* */
        else if(listBox_staff_category==e.getSource())
        {
    		@SuppressWarnings("unchecked")
			JComboBox<String> cb = (JComboBox<String>)e.getSource();
    		String categoryName = (String)cb.getSelectedItem();
    		System.out.println("Update Staff's category for: ["+categoryName+"]");
    		markObjectAsDirty(STAFF_TAB,true);
        }
        else if(listBox_student_gender==e.getSource())
        {
    		@SuppressWarnings("unchecked")
			JComboBox<String> cb = (JComboBox<String>)e.getSource();
    		String gender = (String)cb.getSelectedItem();
    		System.out.println("Student's gender for: ["+gender+"]");
    		markObjectAsDirty(STUDENT_TAB,true);
        }
        else if(("btn_prev_"+STUDENT_TAB).equals(e.getActionCommand()))
        {
        	refreshDataGUI(STUDENT_TAB,-1);
        }
        else if(("btn_next_"+STUDENT_TAB).equals(e.getActionCommand()))
        {
        	refreshDataGUI(STUDENT_TAB,1);
        }
        else if(("btn_first_"+STUDENT_TAB).equals(e.getActionCommand()))
        {
        	person_index[STUDENT_TAB]=0;
        	refreshDataGUI(STUDENT_TAB,0);
        }
        else if(("btn_last_"+STUDENT_TAB).equals(e.getActionCommand()))
        {
        	person_index[STUDENT_TAB]=(students.size()==0?0:students.size()-1);
        	refreshDataGUI(STUDENT_TAB,0);
        }
        else if(("btn_new_"+STUDENT_TAB).equals(e.getActionCommand()))
        {
    		//New Sudent
    		Student new_student=new Student(null,null,null,null,null);
		    /* */
    		students.add(new_student);
    		person_index[STUDENT_TAB]=students.size()-1;
        	refreshDataGUI(STUDENT_TAB,0);
        }
        else if(("btn_search_"+STUDENT_TAB).equals(e.getActionCommand()))
        {
        	search(textField_search[STUDENT_TAB].getText());
        	person_index[STUDENT_TAB]=0;
        	refreshDataGUI(STUDENT_TAB,0);
        }
        else if(("btn_apply_"+STUDENT_TAB).equals(e.getActionCommand()))
        {
        	copyDataFromGUI(STUDENT_TAB,person_index[STUDENT_TAB]);
        	refreshDataGUI(STUDENT_TAB,0);
        }
        else if(("btn_remove_"+STUDENT_TAB).equals(e.getActionCommand()))
        {
        	System.out.println("deleting student "+person_index[STUDENT_TAB]);
        	students.remove(person_index[STUDENT_TAB]);
        	if(person_index[STUDENT_TAB]>=students.size())
        	{
        		person_index[STUDENT_TAB]=students.size()-1;
        	}
        	refreshDataGUI(STUDENT_TAB,0);
        }
        else if(("btn_prev_"+STAFF_TAB).equals(e.getActionCommand()))
        {
        	refreshDataGUI(STAFF_TAB,-1);
        }
        else if(("btn_next_"+STAFF_TAB).equals(e.getActionCommand()))
        {
        	refreshDataGUI(STAFF_TAB,1);
        }
        else if(("btn_first_"+STAFF_TAB).equals(e.getActionCommand()))
        {
        	person_index[STAFF_TAB]=0;
        	refreshDataGUI(STAFF_TAB,0);
        }
        else if(("btn_last_"+STAFF_TAB).equals(e.getActionCommand()))
        {
        	person_index[STAFF_TAB]=(persons.size()==0?0:persons.size()-1);
        	refreshDataGUI(STAFF_TAB,0);
        }
        else if(("btn_new_"+STAFF_TAB).equals(e.getActionCommand()))
        {
    		//New Staff
			/** Version A 
    		Person new_person=new Person();
    		 */
			/** Version B */ 
    		Staff new_person=new Staff(null,null,null,null,"no address",null,null,null,null,false,false,false);
		    /* */
    		persons.add(new_person);
    		person_index[STAFF_TAB]=persons.size()-1;
        	refreshDataGUI(STAFF_TAB,0);
        }
        else if(("btn_search_"+STAFF_TAB).equals(e.getActionCommand()))
        {
        	search(textField_search[STAFF_TAB].getText());
        	person_index[STAFF_TAB]=0;
        	refreshDataGUI(STAFF_TAB,0);
        }
        else if(("btn_apply_"+STAFF_TAB).equals(e.getActionCommand()))
        {
        	copyDataFromGUI(STAFF_TAB,person_index[STAFF_TAB]);
        	refreshDataGUI(STAFF_TAB,0);
        }
        else if(("btn_remove_"+STAFF_TAB).equals(e.getActionCommand()))
        {
        	System.out.println("deleting staff "+person_index[STAFF_TAB]);
        	persons.remove(person_index[STAFF_TAB]);
        	if(person_index[STAFF_TAB]>=persons.size())
        	{
        		person_index[STAFF_TAB]=persons.size()-1;
        	}
        	refreshDataGUI(STAFF_TAB,0);
        }
        else if (buttonString.equals(e.getActionCommand())) 
        {
            Toolkit.getDefaultToolkit().beep();
        }
        else
        {

        }
	}
	/** Listens to the checkbox events */
	public void itemStateChanged(ItemEvent e) 
	{
		Object source = e.getItemSelectable();

        if (source == checkBox_staff_d32 || source == checkBox_staff_d34 || source == checkBox_staff_d36) 
        {
        	markObjectAsDirty(STAFF_TAB,true);
        } 
	
	}
	/**
	* Mark the current record as dirty, i.e, needs to be saved
	*/
	private void markObjectAsDirty(int tab,boolean dirty)
	{
		if(btn_apply[tab]!=null)
		{
			btn_apply[tab].setVisible(dirty);
		}
	}
	protected static void updateLabel(JLabel picture,String name) 
	{
		try 
		{
			ImageIcon icon;
			icon = new ImageIcon(new URL("https://online.iscap.ipp.pt/iscap/java/fotos/rhp/af7eff48874ed62706764d6c3aa9bd80/ddb735dfbf8fa6688bc1522309597625/e1423500309.jpg"));
			picture.setIcon(icon);
			picture.setToolTipText("A drawing of a " + name.toLowerCase());
			picture.setText(null);
		} 
		catch (MalformedURLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			picture.setText("Image not found: "+e.getMessage());
		}

	}
	
    private static void createAndShowGUI() 
    {
        //Create and set up the window.
    	frameApp = new AppDemo("Application for Academic Record - powered by "+frameworkName+" ("+InitDB.DB_VERSION+")");
    	frameApp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
		JTabbedPane tabbedPane = new JTabbedPane();
		JPanel panel_students= makeTextPanel("Panel #1");
        tabbedPane.addTab("Students",null, panel_students,"Students' records");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        
        JPanel panel_staff = makeTextPanel("Panel #2");
        tabbedPane.addTab("Staff",null, panel_staff,"Staff records");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_2);
        
        frameApp.add(tabbedPane);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        
        frameApp.initControlsStudents(panel_students);
        frameApp.initControlsStaff(panel_staff);
        
        
        
        //frame.initControls(frame);
        
        
        //Size and display the window.
        Insets insets = frameApp.getInsets();
        frameApp.setSize(710 + insets.left + insets.right,420 + insets.top + insets.bottom);
        frameApp.setVisible(true);
        
        
        frameApp.statusBar = new StatusBar();
        frameApp.add(frameApp.statusBar, BorderLayout.SOUTH);
        
        
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) 
            {
            	int tabid=((JTabbedPane)e.getSource()).getSelectedIndex();
            	frameApp.refreshDataGUI(tabid,0);
            }
        });
        frameApp.refreshDataGUI(STUDENT_TAB,0);
    }
	
	public static void main(String[] args) 
	{
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	/**
	 * Adds a component at position X,Y
	 * @param pane
	 * @param x
	 * @param y
	 * @param container
	 */
    public void addComponentsToPane(Container pane,int x,int y,Container container) 
    {
    	//Container pane=getContentPane();
        pane.add(container);
        //Insets insets = pane.getInsets();
        Dimension size = container.getPreferredSize();
        container.setBounds(x, y,size.width, size.height);
    }
    /**
     * Adds to main pane (the window itself)
     * @param x
     * @param y
     * @param container
     */
    public void addComponentsToMainPane(int x,int y,Container container) 
    {
    	Container pane=getContentPane();
    	addComponentsToPane(pane,x,y,container);
    }
    
    protected static JPanel makeTextPanel(String text) 
    {
        JPanel tabpanel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        tabpanel.setLayout(new GridLayout(1, 1));
        tabpanel.add(filler);
        return tabpanel;
    }
    private void refreshDataGUI(int tabid,int direction)
    {
    	Object person_edt;
    	if(tabid==STAFF_TAB)
    	{
	    	if(persons.size()==0)
	    	{
	    		person_edt=null;
	    	}
	    	else if(direction>0)
	    	{
	    		person_index[tabid]++;
            	if(person_index[tabid]>=persons.size())
            	{
            		person_index[tabid]=(persons.size()==0?0:persons.size()-1);
            	}
        		person_edt=persons.get(person_index[tabid]);
	    	}
	    	else if(direction<0)
	    	{
	    		person_index[tabid]--;
	        	if(person_index[tabid]<0)
	        	{
	        		person_index[tabid]=0;
	        	}
        		person_edt=persons.get(person_index[tabid]);
	    	}
	    	else if(person_index[tabid]>=0 && persons.size()>0)
	    	{
        		person_edt=persons.get(person_index[tabid]);
	    	}
	    	else
	    	{
	    		person_edt=null;
	    	}
    	}
    	else if(tabid==STUDENT_TAB)
    	{
	    	if(students.size()==0)
	    	{
	    		person_edt=null;
	    	}
	    	else if(direction>0)
	    	{
	    		person_index[tabid]++;
            	if(person_index[tabid]>=students.size())
            	{
            		person_index[tabid]=(students.size()==0?0:students.size()-1);
            	}
        		person_edt=students.get(person_index[tabid]);
	    	}
	    	else if(direction<0)
	    	{
	    		person_index[tabid]--;
	        	if(person_index[tabid]<0)
	        	{
	        		person_index[tabid]=0;
	        	}
        		person_edt=students.get(person_index[tabid]);
	    	}
	    	else if(person_index[tabid]>=0 && students.size()>0)
	    	{
        		person_edt=students.get(person_index[tabid]);
	    	}
	    	else
	    	{
	    		person_edt=null;
	    	}
    	}
    	else
    	{
    		person_edt=null;
    	}
    	int tab=-1;
    	/** Version A
    	if(person_edt!=null && person_edt instanceof Person)
    	*/
    	/** Version B */ 
    	if(person_edt!=null && person_edt instanceof Staff)
    	/* */
    	{
    		tab=STAFF_TAB;
    		/** Version A 
    		Person staff=(Person)person_edt;
    		*/
    		/** Version B */
    		Staff staff=(Staff)person_edt;
    		/* */ 
    		textField_staff_title.setText(staff.getTitle());
    		textField_staff_firstName.setText(staff.getFirstName());
    		/** Version A 
    		textField_staff_surname.setText(staff.getSurname());
    		*/
    		/** Version B */
    		textField_staff_middleName.setText(staff.getMiddleName());
    		textField_staff_lastName.setText(staff.getLastName());
    		/* */
    		textField_staff_address.setText(staff.getAddress());
    		textField_staff_postCode.setText(staff.getPostCode());
    		textField_staff_phone.setText(staff.getTelephoneNumber());
    		textField_staff_fax.setText(staff.getFaxNumber());
    		textField_staff_mobile.setText(staff.getMobileNumber());
    		
    		checkBox_staff_d32.setSelected(staff.isPassedD32Qualification());
    		checkBox_staff_d34.setSelected(staff.isPassedD34Qualification());
    		checkBox_staff_d36.setSelected(staff.isPassedD36Qualification());
    		btn_remove[tab].setVisible(true);
    	}
    	else if(person_edt!=null && person_edt instanceof Student)
    	{
    		tab=STUDENT_TAB;
    		Student student=(Student)person_edt;
    		textField_student_firstName.setText(student.getFirstName());
    		textField_student_middleName.setText(student.getMiddleName());
    		textField_student_lastName.setText(student.getLastName());
    		listBox_student_gender.setSelectedItem(student.getSex());
   			dateField_student.setValue(student.getBirth());
    		btn_remove[tab].setVisible(true);
    	}
    	else if(tabid==STAFF_TAB)
    	{
    		textField_staff_title.setText("");
    		textField_staff_firstName.setText("");
    		/** Version A 
    		textField_staff_surname.setText("");
    		*/
    		/** Version B */
    		textField_staff_middleName.setText("");
    		textField_staff_lastName.setText("");
    		/* */
    		
    		textField_staff_address.setText("");
    		textField_staff_postCode.setText("");
    		textField_staff_phone.setText("");
    		textField_staff_fax.setText("");
    		textField_staff_mobile.setText("");
    		
    		checkBox_staff_d32.setSelected(false);
    		checkBox_staff_d32.setSelected(false);
    		checkBox_staff_d32.setSelected(false);
    		btn_remove[tabid].setVisible(false);
    	}
    	else if(tabid==STUDENT_TAB)
    	{
    		textField_student_firstName.setText("");
    		textField_student_middleName.setText("");
    		textField_student_lastName.setText("");
    		btn_remove[tabid].setVisible(false);
    	}
    	if(tab==STAFF_TAB)
    	{
	    	if(person_index[tabid]>=0 && persons.size()>0)
	    	{
	    		label_person_index[tab].setText(""+(person_index[tabid]+1)+" of "+persons.size());
	    	}
	    	else
	    	{
	    		label_person_index[tab].setText("? of ?");
	    	}
	    	markObjectAsDirty(tab,false);
    	}
    	else if(tab==STUDENT_TAB)
    	{
	    	if(person_index[tabid]>=0 && students.size()>0)
	    	{
	    		label_person_index[tab].setText(""+(person_index[tabid]+1)+" of "+students.size());
	    	}
	    	else
	    	{
	    		label_person_index[tab].setText("? of ?");
	    	}
	    	markObjectAsDirty(tab,false);
    	}
    	else
    	{
    		
    	}
    }
 
   /**
    * Copy data from GUI to the students and staff objects
    * @param tabid
    * @param person_index
    */
    private void copyDataFromGUI(int tabid,int person_index)
    {
    	if(person_index<0)
    	{
    		if(tabid==STAFF_TAB)
    		{

    		}
    		else if(tabid==STUDENT_TAB)
    		{
    			//TODO
    		}
    	}
    	else
    	{
        	if(tabid==STAFF_TAB)
        	{
        		/** Version A 
        		Person updated_person=persons.get(person_index);
        		System.out.println("Updating Person...");
        		updated_person.setSurname(textField_staff_surname.getText());
        		*/
        		/** Version B */ 
        		Staff updated_person=(Staff)persons.get(person_index);
        		System.out.println("Updating Staff...");
        		updated_person.setMiddleName(textField_staff_middleName.getText());
        		updated_person.setLastName(textField_staff_lastName.getText());
        		/* */

        		updated_person.setTitle(textField_staff_title.getText());
        		updated_person.setFirstName(textField_staff_firstName.getText());
        		updated_person.setAddress(textField_staff_address.getText());
        		updated_person.setPostCode(textField_staff_postCode.getText());
        		updated_person.setTelephoneNumber(textField_staff_phone.getText());
        		updated_person.setFaxNumber(textField_staff_fax.getText());
        		updated_person.setMobileNumber(textField_staff_mobile.getText());
        		
        		updated_person.setPassedD32Qualification(checkBox_staff_d32.isSelected());
        		updated_person.setPassedD34Qualification(checkBox_staff_d34.isSelected());
        		updated_person.setPassedD36Qualification(checkBox_staff_d36.isSelected());
        		btn_apply[STAFF_TAB].setVisible(false);
        		
        		//update
        		persons.set(person_index,(Person)updated_person);
        		statusBar.setText("Staff's Record updated");
        	}
        	else if(tabid==STUDENT_TAB)
        	{
        		Student updated_person=students.get(person_index);
        		System.out.println("Updating Student...");
        		updated_person.setFirstName(textField_student_firstName.getText());
        		updated_person.setMiddleName(textField_student_middleName.getText());
        		updated_person.setLastName(textField_student_lastName.getText());
        		updated_person.setSex((String)listBox_student_gender.getSelectedItem());

        		updated_person.setBirth((Date)dateField_student.getValue());
        		btn_apply[STUDENT_TAB].setVisible(false);
        		
        		//update
        		students.set(person_index,(Student)updated_person);
        		statusBar.setText("Student's Record updated");
        	}
        	else
        	{
        		statusBar.setText("No Record was updated");
        	}
    	}
    }
 
    private void initDB()
    {
    	persons=psRoot.getRootObject("persons");
    	if(persons==null)
    	{
    		persons=InitDB.initPersonDB();
    		System.out.println("Initing Staff DB with "+persons.size()+" records..."); 
	    	psRoot.setRootObject("persons", persons);
	    	System.out.println("Init DB with "+persons.size()+" Staff");
    	}
    	else
    	{
    		System.out.println("Load DB with "+persons.getPersons().length+" Staff");
    	}
    	students=psRoot.getRootObject("students");
    	if(students==null)
    	{
    		students=InitDB.initStudentDB();
    		System.out.println("Initing Students DB with "+students.size()+" records..."); 
	    	psRoot.setRootObject("students", students);
	    	System.out.println("Init DB with "+students.size()+" students");
    	}
    	else
    	{
    		System.out.println("Load DB with "+students.getStudents().length+" objects");
    	}
    }
    /** Version A */
    private void search(String text)
    {
    }
    
    /** Version B
    private void search(String text)
    {
    	//TODO: improve query mechanisms in order to allow super class matching
    	statusBar.setText("Searching for \""+text+"\"...");
    	persons=new PersonCol();
    	List<Person> rs_persons=psRoot.query(new CQuery(Principal.class));
		for(Person p:rs_persons)
		{
			System.out.println("Person: "+p);
			if(p.getFirstName().indexOf(text)>=0  || p.getMiddleName().indexOf(text)>=0 || p.getLastName().indexOf(text)>=0 )
			{
				persons.add(p);
			}
		}
		rs_persons=psRoot.query(new CQuery(Coordinator.class));
		for(Person p:rs_persons)
		{
			System.out.println("Person: "+p);
			if(p.getFirstName().indexOf(text)>=0  || p.getMiddleName().indexOf(text)>=0 || p.getLastName().indexOf(text)>=0 )
			{
				persons.add(p);
			}
		}
		rs_persons=psRoot.query(new CQuery(ExamOfficer.class));
		for(Person p:rs_persons)
		{
			System.out.println("Person: "+p);
			if(p.getFirstName().indexOf(text)>=0  || p.getMiddleName().indexOf(text)>=0 || p.getLastName().indexOf(text)>=0 )
			{
				persons.add(p);
			}
		}
		rs_persons=psRoot.query(new CQuery(Moderator.class));
		for(Person p:rs_persons)
		{
			System.out.println("Person: "+p);
			if(p.getFirstName().indexOf(text)>=0  || p.getMiddleName().indexOf(text)>=0 || p.getLastName().indexOf(text)>=0 )
			{
				persons.add(p);
			}
		}
		rs_persons=psRoot.query(new CQuery(Tutor.class));
		for(Person p:rs_persons)
		{
			System.out.println("Person: "+p);
			if(p.getFirstName().indexOf(text)>=0  || p.getMiddleName().indexOf(text)>=0 || p.getLastName().indexOf(text)>=0 )
			{
				persons.add(p);
			}
		}
		statusBar.setText(""+persons.size()+" Persons were found");
    }
    */
}
/**
 * StatusBar implementation
 * @author rhp
 *
 */
class StatusBar extends JPanel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel text;
	  public StatusBar() 
	  {
	    setLayout(new BorderLayout());
	    setPreferredSize(new Dimension(10, 23));

	    JPanel rightPanel = new JPanel(new BorderLayout());
	    rightPanel.add(new JLabel(new AngledLinesWindowsCornerIcon()), BorderLayout.SOUTH);
	    rightPanel.setOpaque(false);
	    
	    text=new JLabel("");
	    add(text);

	    add(rightPanel, BorderLayout.EAST);
	    setBackground(SystemColor.control);
	  }
	  public void setText(String text)
	  {
		  this.text.setText(" "+text);
	  }
	  protected void paintComponent(Graphics g) 
	  {
	    super.paintComponent(g);

	    int y = 0;
	    g.setColor(new Color(156, 154, 140));
	    g.drawLine(0, y, getWidth(), y);
	    y++;
	    g.setColor(new Color(196, 194, 183));
	    g.drawLine(0, y, getWidth(), y);
	    y++;
	    g.setColor(new Color(218, 215, 201));
	    g.drawLine(0, y, getWidth(), y);
	    y++;
	    g.setColor(new Color(233, 231, 217));
	    g.drawLine(0, y, getWidth(), y);

	    y = getHeight() - 3;
	    g.setColor(new Color(233, 232, 218));
	    g.drawLine(0, y, getWidth(), y);
	    y++;
	    g.setColor(new Color(233, 231, 216));
	    g.drawLine(0, y, getWidth(), y);
	    y = getHeight() - 1;
	    g.setColor(new Color(221, 221, 220));
	    g.drawLine(0, y, getWidth(), y);

	  }

	}

	class AngledLinesWindowsCornerIcon implements Icon {
	  private static final Color WHITE_LINE_COLOR = new Color(255, 255, 255);

	  private static final Color GRAY_LINE_COLOR = new Color(172, 168, 153);
	  private static final int WIDTH = 13;

	  private static final int HEIGHT = 13;

	  public int getIconHeight() {
	    return WIDTH;
	  }

	  public int getIconWidth() {
	    return HEIGHT;
	  }

	  public void paintIcon(Component c, Graphics g, int x, int y) {

	    g.setColor(WHITE_LINE_COLOR);
	    g.drawLine(0, 12, 12, 0);
	    g.drawLine(5, 12, 12, 5);
	    g.drawLine(10, 12, 12, 10);

	    g.setColor(GRAY_LINE_COLOR);
	    g.drawLine(1, 12, 12, 1);
	    g.drawLine(2, 12, 12, 2);
	    g.drawLine(3, 12, 12, 3);

	    g.drawLine(6, 12, 12, 6);
	    g.drawLine(7, 12, 12, 7);
	    g.drawLine(8, 12, 12, 8);

	    g.drawLine(11, 12, 12, 11);
	    g.drawLine(12, 12, 12, 12);

	  }
	}