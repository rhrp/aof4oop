package rhp.aof4oop.framework.core;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import rhp.aof4oop.framework.core.CClassLoader;

/**
 * This class provides all tools to dynamically compile and load in ClassLoader a class in source form
 * @author rhp
 *
 */
public class CDynCompiler 
{
	private String classpath;
	private String outdir;

	public CDynCompiler(String classpath,String outdir) 
	{
		super();
		setClasspath(classpath);
		this.outdir=outdir;
		System.out.println("Init compiler with classpath "+getClasspath()+" in output dir "+getOutdir()+" ..");
	}

	//	public static void main(String args[]) throws Exception 
	//	{
	//
	//		StringBuffer code = new StringBuffer();
	//		code.append("package aaa; class HelloWorld {");
	//		code.append("  public static void main(String args[]) {");
	//		code.append("    System.out.println(\"My Test\");");    
	//		code.append("  }");
	//		code.append("}");
	//		
	//		
	//		if ((new CDynCompiler()).compile("aaa.HelloWorld",code.toString())) 
	//		{
	//			try 
	//			{
	//				Class clzz=((CClassLoader)ClassLoader.getSystemClassLoader()).getClass("aaa.HelloWorld");
	//				Method m=clzz.getDeclaredMethod("main", new Class[] { String[].class });
	//				m.setAccessible(true);
	//				m.invoke(null, new Object[] { null });
	//			
	//			} catch (NoSuchMethodException e) {
	//				System.err.println("No such method: " + e);
	//			} catch (IllegalAccessException e) {
	//				System.err.println("Illegal access: " + e);
	//			} catch (InvocationTargetException e) {
	//				System.err.println("Invocation target: " + e);
	//			}
	//		}
	//	}

	/**
	 * Compile and put on class path a class
	 * @param classCanonicalName
	 * @param code - java code
	 * @return
	 * @throws IOException
	 * @throws NotFoundException
	 * @throws CannotCompileException
	 */
	public boolean compile(String classCanonicalName,String code,CClassLoader cl) throws IOException, NotFoundException, CannotCompileException 
	{

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if(compiler==null)
		{
			throw new NotFoundException("In this platform no compiler is provided");
		}
		System.out.println("Compiler: "+compiler.toString());
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, Locale.getDefault(), null);
		JavaFileObject file = new JavaSourceFromString(classCanonicalName, code);

		System.out.println("Source: "+file.getName()+"  classCanonicalName:"+classCanonicalName);
		System.out.println("Outdir: "+getOutdir()+"  classpaph: "+getClasspath());
		String[] compileOptions;
		compileOptions = new String[]{"-d",getOutdir(),"-classpath",getClasspath()} ;
		Iterable<String> compilationOptions = Arrays.asList(compileOptions);

		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);
		CompilationTask task = compiler.getTask(null, stdFileManager, diagnostics, compilationOptions, null, compilationUnits);

		boolean success = task.call();
		if(diagnostics!=null)
		{
			for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) 
			{
				System.out.println("Code: "+diagnostic.getCode());
				System.out.println("Kind: "+diagnostic.getKind());
				System.out.println("Position: "+diagnostic.getPosition());
				System.out.println("StartPosition: "+diagnostic.getStartPosition());
				System.out.println("EndPosition: "+diagnostic.getEndPosition());
				System.out.println("Source: "+diagnostic.getSource());
				System.out.println("Message: "+diagnostic.getMessage(null));
			}
		}
		System.out.println("Success: " + success);
		if(success)
		{
			//		ClassPool cp=ClassPool.getDefault();
			//		ClassPath cc = cp.appendClassPath("tmp");
			//		CtClass c = cp.getCtClass(classCanonicalName);
			//		System.out.println("size="+c.toBytecode().length);
			if(cl!=null)
			{
				cl.register((getOutdir().endsWith("/")?getOutdir():getOutdir()+"/")+classCanonicalName.replace('.', '/')+".class",classCanonicalName,classCanonicalName);
			}
		}
		return success;
	}
	public String getClasspath() 
	{
		return classpath;
	}
	public void setClasspath(String classpath) 
	{
		this.classpath = classpath;
	}
	public String getOutdir() {
		return outdir;
	}
	public void setOutdir(String outdir) {
		this.outdir = outdir;
	}
	class JavaSourceFromString extends SimpleJavaFileObject 
	{
		private String qualifiedName ;
		private String sourceCode ;
		protected final ByteArrayOutputStream bos =  new ByteArrayOutputStream();

		/**
		 * Converts the name to an URI, as that is the format expected by JavaFileObject
		 * 
		 * 
		 * @param fully qualified name given to the class file
		 * @param code the source code string
		 */
		protected JavaSourceFromString(String name, String code) 
		{
			super(URI.create("string:///" +name.replaceAll(".", "/") + Kind.SOURCE.extension), Kind.SOURCE);
			this.qualifiedName = name ;
			this.sourceCode = code ;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors)
		throws IOException {
			return sourceCode ;
		}

		public String getQualifiedName() {
			return qualifiedName;
		}

		public void setQualifiedName(String qualifiedName) {
			this.qualifiedName = qualifiedName;
		}

		public String getSourceCode() {
			return sourceCode;
		}

		public void setSourceCode(String sourceCode) {
			this.sourceCode = sourceCode;
		}
		/**
		 * Will be used by our file manager to get the byte code that
		 * can be put into memory to instantiate our class
		 *
		 * @return compiled byte code
		 */
		public byte[] getBytes() {
			return bos.toByteArray();
		}
		@Override
		public OutputStream openOutputStream() throws IOException 
		{
			System.out.println("bytes"+getBytes().length);
			return bos;
		}
	}}
