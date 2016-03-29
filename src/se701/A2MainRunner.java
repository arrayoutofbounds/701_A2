package se701;

import java.io.File;
import java.io.FileNotFoundException;

import japa.parser.ParseException;

public class A2MainRunner {

	public static void main(String[] args) {
		
		/*
		 * These tests will be testing correctness of your Semantic Analysis visitors. The marker will be using their own files here. 
		 */
		for (int i = 1; i <= 2; i++) { 
			String file = "tests"+System.getProperty("file.separator")+"Test"+i+".javax";
			try {
				A2Compiler.compile(new File(file));
				System.out.println(file+" ... OK");
			} catch (ParseException e) {
				System.err.println(file+" Parser exception... "+e.getMessage());
				e.printStackTrace();
			} catch (A2SemanticsException e) {
				System.err.println(file+" Semantics exception... "+e.getMessage());
				e.printStackTrace();
			}  catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		

		/*
		 * This is to compile StudentSample.javax. The marker will run with the supplied *.javax 
		 * file which is associated to your selected feature. 		 
		 */
		try {
			A2Compiler.compile(new File("src"+System.getProperty("file.separator")+"se701"+System.getProperty("file.separator")+"StudentSample.javax"));
			System.out.println("src/se701.StudentSample compiled correctly");
		} catch (ParseException e) {
			System.err.println("Sample file should not have any errors! ");
		} catch (A2SemanticsException e) {
			System.err.println("Sample file should not have any errors! ");
		}  catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}