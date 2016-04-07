package rhp.aof4oop.cs.utils;

import java.util.Date;

import rhp.aof4oop.cs.datamodel.Coordinator;
import rhp.aof4oop.cs.datamodel.ExamOfficer;
import rhp.aof4oop.cs.datamodel.Moderator;
import rhp.aof4oop.cs.utils.PersonCol;
import rhp.aof4oop.cs.datamodel.Principal;
import rhp.aof4oop.cs.datamodel.Student;
import rhp.aof4oop.cs.utils.StudentCol;
import rhp.aof4oop.cs.datamodel.Tutor;

public class InitDB 
{
	public static String DB_VERSION="Version A";
	public static PersonCol initPersonDB()
	{
		PersonCol db_persons=new PersonCol();
		db_persons.add(new Principal("Humberto Pereira","Rui","Eng","Rua Companhia dos Caulinos","4460","555123456","5559765541","5554748374",true,false,true));
		db_persons.add(new Principal("Carvalho Lopes","James","Dr","Rua de Cima","5434","666123456","6669765541","666476383",true,false,true));
    	for(int i=0;i<210;i++)
    	{
    		switch(i%5)
    		{
    			case 0: db_persons.add(new Coordinator("Carvalho#"+i+" Lopes#"+i,"Paul#"+1,"Dr.","Rua de Cima#"+i,"5434","666123456","6669765541","666476383",true,false,true));
    					break;
    			case 1: db_persons.add(new ExamOfficer("Carvalho#"+i+" Lopes#"+i,"James#"+i,"Dr","Rua de Cima#"+i,"5434","666123456","6669765541","666476383",true,false,true));
    				    break;
    			case 2: db_persons.add(new Moderator("Carvalho#"+i+" Lopes#"+i,"Peter#"+1,"Eng","Rua de Cima#"+i,"5434","666123456","6669765541","666476383",true,false,true));
						break;
    			case 3: db_persons.add(new Principal("Carvalho#"+i+" Lopes#"+i,"Arthur#"+i,"Dr","Rua de Cima#"+i,"5434","666123456","6669765541","666476383",true,false,true));
			    	    break;
    			default: db_persons.add(new Tutor("Carvalho#"+i+" Lopes#"+i,"Adam#"+i,"Dr.","Rua de Cima#"+i,"5434","666123456","6669765541","666476383",true,false,true));
	    	    	    break;
			    	    
    		}
    	}
    	return db_persons;
	}
	public static StudentCol initStudentDB()
	{
		StudentCol db_persons=new StudentCol();
    	for(int i=0;i<3500;i++)
    	{
    		Date birth=new Date(80+(i%50),(i%12),(i%28));
    		if(i%2==0)
    		{
    			db_persons.add(new Student("Adam#"+i,"Carvalho#"+i,"Lopes#"+i,birth,"M"));
    		}
    		else
    		{
    			db_persons.add(new Student("Sara#"+i,"Simth#"+i,"Silva#"+i,birth,"F"));
    		}
    	}
    	return db_persons;
	}
}
