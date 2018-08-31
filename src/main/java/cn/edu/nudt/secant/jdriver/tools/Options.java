package cn.edu.nudt.secant.jdriver.tools;

import java.util.HashSet;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.kohsuke.args4j.Option;



/**
 * Options for the Kelinci Instrumentor.
 * Currently -i and -o flags to specify input and output, respectively,
 * and -threads to specify the number of runner threads.
 * and -m to specify ranking algorithm
 *
 * -class single class
 * -method -desc single method mode
 *
 * 
 * @author rodykers, huang
 *
 */
public class Options {

	@Option(name = "-i", usage = "Specify input file/dir", required = true)
	private String input;
	private HashSet<String> inputClasses;
	
	public String getRawInput() {
		return input;
	}
	
	public HashSet<String> getInput() {
		if (inputClasses == null) {
			inputClasses = new HashSet<>();
			if (input.endsWith(".class")) {
				// single class file, has to be a relative path from a directory on the class path
				inputClasses.add(input);
			} else if (input.endsWith(".jar")) {
				// JAR file
				JarFileIO.extractJar(input, inputClasses);
				addToClassPath(input);
			} else {
				// directory
				System.out.println("Loading dir: " + input);
				loadDirectory(input, inputClasses);
				addToClassPath(input);
			}
		}
		return inputClasses;
	}
	
	/*
	 * Add an element to the class path. Can be either a directory or a JAR.
	 * //fixme, changed private to public by huang
	 */
	public static void addToClassPath(String url) {
		try {
			File file = new File(url);
			Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
			method.setAccessible(true);
		    method.invoke(ClassLoader.getSystemClassLoader(), new Object[]{file.toURI().toURL()});
		} catch (Exception e) {
			throw new RuntimeException("Error adding location to class path: " + url);
		}
	    
	}

	private void loadDirectory(String input, HashSet<String> inputClasses) {
		final int dirprefix;
		if (input.endsWith("/"))
			dirprefix = input.length();
		else
			dirprefix = input.length()+1;
		
		try {
			Files.walk(Paths.get(input)).filter(Files::isRegularFile).forEach(filePath -> {
				String name = filePath.toString();
				System.out.println("Found file " + name);
				if (name.endsWith(".class")) {
					inputClasses.add(name.substring(dirprefix));
				}

			});
		} catch (IOException e) {
			throw new RuntimeException("Error reading from directory: " + input);
		}
	}

	@Option(name = "-o", usage = "Specificy output file/dir")
	private String output = "./tests/output/";
	public String getOutput() {
		return output;
	}
	
	public boolean outputJar() {
		boolean outjar = output.endsWith(".jar");
		if (outjar && !input.endsWith(".jar"))
			throw new RuntimeException("Cannot output JAR if the input is not a JAR");
		return output.endsWith(".jar");
	}


	@Option(name="-helperalone", usage = "generate helper methods alone")
	private boolean helper = true;
	public boolean isHelperAlone() {return helper;}

	@Option(name = "-v", usage = "show information verbose")
	private boolean verbose = false;
	public boolean isVerbose() {return  verbose;}


	/**
	 * -desc, -method, -class, specify a single method to test
	 * method and classname are required
	 */
	@Option(name="-desc", usage = "specify the method description")
	private String desc;
	public String getDesc() {return desc;}

	@Option(name = "-method", usage = "test single method")
	private String methodName = null;
	public String getMethodName() {return methodName;}

	/**
	 * -class specify generation for single class file
	 */
	@Option(name="-class", usage = "specify package classname")
	private String className = null;
	public String getClassName() {return className;}


	@Option(name = "-r", usage = "specify whether to rank mehtod or not")
	private boolean ranking = false;
	public boolean isRanking() {return ranking;}

	/**
	 * add by huang, this is to specify the compare standard
	 */
	@Option(name = "-s", usage = "compare standard, Selfdefined/Branch/Call/Target/Insn")
	private char mode = 's';
	public char getMode() {return mode;}
	public void setMode(char m) {mode = m;}

	public String getModeString() {
		String s = "";
		switch (mode) {
			case 'b':
				s = " branch";
				break;
			case 'c':
				s = " number of callers (incoming)";
				break;
			case 't':
				s = " number of targets (outgooing)";
				break;
			case 'i':
				s = " number of instructions";
				break;
			case 's':
			default:
				s = " self-defined metric";
				break;
		}
		return s;

	}


	/**
	 * Singleton
	 */
	private static Options options;

	public static void resetInstance() {
		options = null;
	}

	public static Options v() {
		if (null == options) {
			options = new Options();
		}
		return options;
	}

	private Options() {
	}



}

