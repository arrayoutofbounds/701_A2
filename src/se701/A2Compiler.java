package se701;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.visitor.DefinitionVisitor;
import japa.parser.ast.visitor.DumpVisitor;
import japa.parser.ast.visitor.ResolvingVisitor;
import japa.parser.ast.visitor.SetYieldVisitor;
import japa.parser.ast.visitor.TypingVisitor;

public class A2Compiler {
	
	/*
	 * This is the only method you should need to change inside this class. But do not modify the signature of the method! 
	 */
	public static void compile(File file) throws ParseException, FileNotFoundException {

		// parse the input, performs lexical and syntatic analysis already done for you.
		JavaParser parser = new JavaParser(new FileReader(file));
		CompilationUnit ast = parser.CompilationUnit();
		
		
		// perform visit N and print
		//TestVisitor testVisitor = new TestVisitor();
		//ast.accept(testVisitor, null);
		
		
		// add the scopes, classes, methods
		TypingVisitor typingVisitor =  new TypingVisitor();
		ast.accept(typingVisitor, null);
		
		// define all the variables, parameteres and check that the method returns a valid type
		// and all variables not duplicated 
		DefinitionVisitor definitionVisitor = new DefinitionVisitor();
		ast.accept(definitionVisitor, null);
		
		// set yield on method declaration when yield statement is reached so that it can print in dump visitor
		SetYieldVisitor setYield = new SetYieldVisitor();
		ast.accept(setYield, null);
		
		// ensure that variables etc match what they say they are
		ResolvingVisitor resolvingVisitor = new ResolvingVisitor();
		ast.accept(resolvingVisitor, null);
		
		// perform visit N and print
		DumpVisitor printVisitor = new DumpVisitor();
		ast.accept(printVisitor, null);
		
		String result = printVisitor.getSource();
		
		// save the result into a *.java file, same level as the original file
		File javaFile = getAsJavaFile(file);
		writeToFile(javaFile, result);
	}
	
	/*
	 * Given a *.javax File, this method returns a *.java File at the same directory location  
	 */
	private static File getAsJavaFile(File javaxFile) {
		String javaxFileName = javaxFile.getName();
		File containingDirectory = javaxFile.getAbsoluteFile().getParentFile();
		String path = containingDirectory.getAbsolutePath()+System.getProperty("file.separator");
		String javaFilePath = path + javaxFileName.substring(0,javaxFileName.lastIndexOf("."))+".java";
		return new File(javaFilePath);
	}
	
	/*
	 * Given the specified file, writes the contents into it.
	 */
	private static void writeToFile(File file, String contents) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(file);
		writer.print(contents);
		writer.close();
	}
}
