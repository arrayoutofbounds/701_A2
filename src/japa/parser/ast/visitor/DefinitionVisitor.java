/*
 * Copyright (C) 2007 J�lio Vilmar Gesser.
 * 
 * This file is part of Java 1.5 parser and Abstract Syntax Tree.
 *
 * Java 1.5 parser and Abstract Syntax Tree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Java 1.5 parser and Abstract Syntax Tree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Java 1.5 parser and Abstract Syntax Tree.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Created on 05/10/2006
 */
package japa.parser.ast.visitor;

import japa.parser.ast.BlockComment;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.LineComment;
import japa.parser.ast.Node;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.TypeParameter;
import japa.parser.ast.body.AnnotationDeclaration;
import japa.parser.ast.body.AnnotationMemberDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.EmptyMemberDeclaration;
import japa.parser.ast.body.EmptyTypeDeclaration;
import japa.parser.ast.body.EnumConstantDeclaration;
import japa.parser.ast.body.EnumDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.InitializerDeclaration;
import japa.parser.ast.body.JavadocComment;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.ArrayInitializerExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.CharLiteralExpr;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.ConditionalExpr;
import japa.parser.ast.expr.DoubleLiteralExpr;
import japa.parser.ast.expr.EnclosedExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.InstanceOfExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.IntegerLiteralMinValueExpr;
import japa.parser.ast.expr.LongLiteralExpr;
import japa.parser.ast.expr.LongLiteralMinValueExpr;
import japa.parser.ast.expr.MarkerAnnotationExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.NullLiteralExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.expr.SingleMemberAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.expr.SuperExpr;
import japa.parser.ast.expr.SuperMemberAccessExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.AssertStmt;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.CatchClause;
import japa.parser.ast.stmt.ContinueStmt;
import japa.parser.ast.stmt.DoStmt;
import japa.parser.ast.stmt.EmptyStmt;
import japa.parser.ast.stmt.ExplicitConstructorInvocationStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ForeachStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.LabeledStmt;
import japa.parser.ast.stmt.ReturnStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.stmt.SwitchStmt;
import japa.parser.ast.stmt.SynchronizedStmt;
import japa.parser.ast.stmt.ThrowStmt;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.stmt.TypeDeclarationStmt;
import japa.parser.ast.stmt.WhileStmt;
import japa.parser.ast.stmt.YieldStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import japa.parser.ast.type.VoidType;
import japa.parser.ast.type.WildcardType;
import se701.A2SemanticsException;
import symtab.MethodSymbol;
import symtab.Scope;
import symtab.Symbol;
import symtab.VariableSymbol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author	anmol desai
 */

public final class DefinitionVisitor implements VoidVisitor<Object> {

	private final SourcePrinter printer = new SourcePrinter();

	private MethodCallExpr currentMethodCall = null;

	private Scope currentScope;

	public String getSource() {
		return printer.getSource();
	}

	private void printModifiers(int modifiers) {
		if (ModifierSet.isPrivate(modifiers)) {
			printer.print("private ");
		}
		if (ModifierSet.isProtected(modifiers)) {
			printer.print("protected ");
		}
		if (ModifierSet.isPublic(modifiers)) {
			printer.print("public ");
		}
		if (ModifierSet.isAbstract(modifiers)) {
			printer.print("abstract ");
		}
		if (ModifierSet.isStatic(modifiers)) {
			printer.print("static ");
		}
		if (ModifierSet.isFinal(modifiers)) {
			printer.print("final ");
		}
		if (ModifierSet.isNative(modifiers)) {
			printer.print("native ");
		}
		if (ModifierSet.isStrictfp(modifiers)) {
			printer.print("strictfp ");
		}
		if (ModifierSet.isSynchronized(modifiers)) {
			printer.print("synchronized ");
		}
		if (ModifierSet.isTransient(modifiers)) {
			printer.print("transient ");
		}
		if (ModifierSet.isVolatile(modifiers)) {
			printer.print("volatile ");
		}
	}

	private void printMembers(List<BodyDeclaration> members, Object arg) {
		for (BodyDeclaration member : members) {
			printer.printLn();
			member.accept(this, arg);
			printer.printLn();
		}
	}

	private void printMemberAnnotations(List<AnnotationExpr> annotations, Object arg) {
		if (annotations != null) {
			for (AnnotationExpr a : annotations) {
				a.accept(this, arg);
				printer.printLn();
			}
		}
	}

	private void printAnnotations(List<AnnotationExpr> annotations, Object arg) {
		if (annotations != null) {
			for (AnnotationExpr a : annotations) {
				a.accept(this, arg);
				printer.print(" ");
			}
		}
	}

	private void printTypeArgs(List<Type> args, Object arg) {
		if (args != null) {
			printer.print("<");
			for (Iterator<Type> i = args.iterator(); i.hasNext();) {
				Type t = i.next();
				t.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
			printer.print(">");
		}
	}

	private void printTypeParameters(List<TypeParameter> args, Object arg) {
		if (args != null) {
			printer.print("<");
			for (Iterator<TypeParameter> i = args.iterator(); i.hasNext();) {
				TypeParameter t = i.next();
				t.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
			printer.print(">");
		}
	}

	public void visit(Node n, Object arg) {
		throw new IllegalStateException(n.getClass().getName());
	}

	public void visit(CompilationUnit n, Object arg) {
		if (n.getPakage() != null) {
			n.getPakage().accept(this, arg);
		}
		if (n.getImports() != null) {
			for (ImportDeclaration i : n.getImports()) {
				i.accept(this, arg);
			}
			printer.printLn();
		}
		if (n.getTypes() != null) {
			for (Iterator<TypeDeclaration> i = n.getTypes().iterator(); i.hasNext();) {
				i.next().accept(this, arg);
				printer.printLn();
				if (i.hasNext()) {
					printer.printLn();
				}
			}
		}
	}

	public void visit(PackageDeclaration n, Object arg) {
		printAnnotations(n.getAnnotations(), arg);
		printer.print("package ");
		n.getName().accept(this, arg);
		printer.printLn(";");
		printer.printLn();
	}

	public void visit(NameExpr n, Object arg) {
		//System.out.println(n + " " + n.getBeginLine());
		printer.print(n.getName());
	}

	public void visit(QualifiedNameExpr n, Object arg) {
		n.getQualifier().accept(this, arg);
		printer.print(".");
		printer.print(n.getName());
	}

	public void visit(ImportDeclaration n, Object arg) {
		printer.print("import ");
		if (n.isStatic()) {
			printer.print("static ");
		}
		n.getName().accept(this, arg);
		if (n.isAsterisk()) {
			printer.print(".*");
		}
		printer.printLn(";");
	}

	public void visit(ClassOrInterfaceDeclaration n, Object arg) {

		//System.out.println( "get parameters :\n" + n.getTypeParameters());
		//System.out.println("get name :\n" +n.getName());
		//System.out.println("get data :\n" +n.getData());
		//System.out.println("get members :\n" +n.getMembers());

		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		printMemberAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers());

		if (n.isInterface()) {
			printer.print("interface ");
		} else {
			printer.print("class ");
		}

		printer.print(n.getName());

		printTypeParameters(n.getTypeParameters(), arg);

		if (n.getExtends() != null) {
			printer.print(" extends ");
			for (Iterator<ClassOrInterfaceType> i = n.getExtends().iterator(); i.hasNext();) {
				ClassOrInterfaceType c = i.next();
				c.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}

		if (n.getImplements() != null) {
			printer.print(" implements ");
			for (Iterator<ClassOrInterfaceType> i = n.getImplements().iterator(); i.hasNext();) {
				ClassOrInterfaceType c = i.next();
				c.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}

		printer.printLn(" {");
		printer.indent();
		if (n.getMembers() != null) {
			printMembers(n.getMembers(), arg);
		}
		printer.unindent();
		printer.print("}");
	}

	public void visit(EmptyTypeDeclaration n, Object arg) {
		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		printer.print(";");
	}

	public void visit(JavadocComment n, Object arg) {
		printer.print("/**");
		printer.print(n.getContent());
		printer.printLn("*/");
	}

	public void visit(ClassOrInterfaceType n, Object arg) {
		if (n.getScope() != null) {
			n.getScope().accept(this, arg);
			printer.print(".");
		}
		printer.print(n.getName());
		printTypeArgs(n.getTypeArgs(), arg);
	}

	public void visit(TypeParameter n, Object arg) {
		printer.print(n.getName());
		if (n.getTypeBound() != null) {
			printer.print(" extends ");
			for (Iterator<ClassOrInterfaceType> i = n.getTypeBound().iterator(); i.hasNext();) {
				ClassOrInterfaceType c = i.next();
				c.accept(this, arg);
				if (i.hasNext()) {
					printer.print(" & ");
				}
			}
		}
	}

	public void visit(PrimitiveType n, Object arg) {
		switch (n.getType()) {
		case Boolean:
			printer.print("boolean");
			break;
		case Byte:
			printer.print("byte");
			break;
		case Char:
			printer.print("char");
			break;
		case Double:
			printer.print("double");
			break;
		case Float:
			printer.print("float");
			break;
		case Int:
			printer.print("int");
			break;
		case Long:
			printer.print("long");
			break;
		case Short:
			printer.print("short");
			break;
		}
	}

	public void visit(ReferenceType n, Object arg) {
		n.getType().accept(this, arg);
		for (int i = 0; i < n.getArrayCount(); i++) {
			printer.print("[]");
		}
	}

	public void visit(WildcardType n, Object arg) {
		printer.print("?");
		if (n.getExtends() != null) {
			printer.print(" extends ");
			n.getExtends().accept(this, arg);
		}
		if (n.getSuper() != null) {
			printer.print(" super ");
			n.getSuper().accept(this, arg);
		}
	}

	public void visit(FieldDeclaration n, Object arg) {

		// get the current scope of this node
		currentScope = n.getThisNodeScope();


		Symbol symOfVariable = currentScope.resolve(n.getType().toString());

		//System.out.println(n.getType());

		/*
		 * THis checks that the "type" of the variable is not null
		 * and an instance of the type interface ( i.e its already a object thats been defined 
		 * or it is a primitive type. Since both class and built in symbol are of type interface
		 * 
		 * So it only goes ahead with adding the variable to the scope if it has a proper type 
		 */

		// this check needs to be done for every variable,parameter 
		if(symOfVariable == null){
			throw new A2SemanticsException(n.getType().toString() + " on line " + n.getType().getBeginLine() + " is not a defined type");
		}
		// checks that the instance of of a type. i.e a class or a built in type
		if(!(symOfVariable instanceof symtab.Type)){
			throw new A2SemanticsException(n.getType().toString() + " on line " + n.getType().getBeginLine() + " is not a valid type");
		}

		
		//System.out.println(n.getType() + " on line " + n.getBeginLine());


		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		printMemberAnnotations(n.getAnnotations(), arg); // this prints annotations like @ 
		printModifiers(n.getModifiers()); // this prints the modifiers like public, private etc 
		n.getType().accept(this, arg); // this sends this visitor to the type that processes/prints it. i,e String, int

		// now that the modifiers have been printed, the variables can be printed as well
		// since variables can be comma separated i.e int a,b,c they are done in a loop
		// hence they need to be added to the scope in the loop
		printer.print(" ");
		for (Iterator<VariableDeclarator> i = n.getVariables().iterator(); i.hasNext();) {
			VariableDeclarator var = i.next();
			var.accept(this, arg);
			if (i.hasNext()) {
				printer.print(", ");
			}

			String varName = var.getId().toString(); // gets the name of the variable
			//System.out.println(type);

			if (currentScope.resolve(varName) != null) {
				throw new A2SemanticsException("Sorry this variable " +  varName + " has already been defined. You cannot redefine it on line " + n.getBeginLine());
			}

			// so we know that the variable has not been defined. Hence define it and add to the current scope of this node

			Symbol variableSymbol = new VariableSymbol(varName,(symtab.Type)symOfVariable);

			currentScope.define(variableSymbol); // adds to the scope of the node

		}

		printer.print(";");
	}

	public void visit(VariableDeclarator n, Object arg) {
		//System.out.println(n + " " + n.getBeginLine());

		n.getId().accept(this, arg);
		if (n.getInit() != null) {
			printer.print(" = ");
			n.getInit().accept(this, arg);
		}
	}

	public void visit(VariableDeclaratorId n, Object arg) {
		printer.print(n.getName());
		for (int i = 0; i < n.getArrayCount(); i++) {
			printer.print("[]");
		}
	}

	public void visit(ArrayInitializerExpr n, Object arg) {
		printer.print("{");
		if (n.getValues() != null) {
			printer.print(" ");
			for (Iterator<Expression> i = n.getValues().iterator(); i.hasNext();) {
				Expression expr = i.next();
				expr.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
			printer.print(" ");
		}
		printer.print("}");
	}

	public void visit(VoidType n, Object arg) {
		printer.print("void");
	}

	public void visit(ArrayAccessExpr n, Object arg) {
		n.getName().accept(this, arg);
		printer.print("[");
		n.getIndex().accept(this, arg);
		printer.print("]");
	}

	public void visit(ArrayCreationExpr n, Object arg) {
		printer.print("new ");
		n.getType().accept(this, arg);
		printTypeArgs(n.getTypeArgs(), arg);

		if (n.getDimensions() != null) {
			for (Expression dim : n.getDimensions()) {
				printer.print("[");
				dim.accept(this, arg);
				printer.print("]");
			}
			for (int i = 0; i < n.getArrayCount(); i++) {
				printer.print("[]");
			}
		} else {
			for (int i = 0; i < n.getArrayCount(); i++) {
				printer.print("[]");
			}
			printer.print(" ");
			n.getInitializer().accept(this, arg);
		}
	}

	private symtab.Type getTypeOfExpression(Scope currentScope, Expression init) {
		symtab.Type type = null;
		if(init != null){
			Symbol sym = null;
			if(init.getClass() == NameExpr.class){ // if the expression is a variable
				sym = currentScope.resolve(init.toString());
				if(sym == null){
					throw new A2SemanticsException(init + " is not defined on line " + init.getBeginLine());
				}
				if(!(sym.getType() instanceof symtab.Type)){
					throw new A2SemanticsException(init + " is not valid on line " + init.getBeginLine());
				}
				type = sym.getType();
			}else{
				//NOTE: IntegerLiteralExpr extends StringLiteralExpr, so must check IntegerLiteralExpr first
				if(init.getClass() == IntegerLiteralExpr.class){
					sym = currentScope.resolve("int");
				}else if (init.getClass() == StringLiteralExpr.class){
					sym = currentScope.resolve("String");
				}else if (init.getClass() == BooleanLiteralExpr.class) {
					sym = currentScope.resolve("boolean");
				}else if (init.getClass() == DoubleLiteralExpr.class) {
					sym = currentScope.resolve("double");
				}else if (init.getClass() == CharLiteralExpr.class) {
					sym = currentScope.resolve("char");
				}else if(init.getClass() == LongLiteralExpr.class) {
					sym = currentScope.resolve("long");
				}
				//TODO other primitive types (and others?)
				else{
					System.out.println("Add " + init.getClass() + " to getTypeofExpression helper method");
				}
				type = (symtab.Type)sym; 
				//System.out.println(type +  " " + init.getBeginLine());
			}
		}
		return type;
	}


	public void visit(AssignExpr n, Object arg) {


		// this gets the expression i.e a = 1;
		//System.out.println("assign expr " + n + " on line " +  n.getBeginLine());

		// ensure that the left and right hand side of expression are valid
		// rhs of expression can be a method.

		// get the current scope of this node
		//currentScope = n.getThisNodeScope();

		//System.out.println(n.getTarget());
		//System.out.println(n.getValue());

		//Expression lhs = n.getTarget();
		//Expression rhs = n.getValue();

		// this gets the type, like int, boolean, foo etc
		//symtab.Type typeOfVariableLhs = getTypeOfExpression(currentScope,lhs);
		//symtab.Type typeOfVariableRhs = getTypeOfExpression(currentScope,rhs);




		n.getTarget().accept(this, arg);
		printer.print(" ");
		switch (n.getOperator()) {
		case assign:
			printer.print("=");
			break;
		case and:
			printer.print("&=");
			break;
		case or:
			printer.print("|=");
			break;
		case xor:
			printer.print("^=");
			break;
		case plus:
			printer.print("+=");
			break;
		case minus:
			printer.print("-=");
			break;
		case rem:
			printer.print("%=");
			break;
		case slash:
			printer.print("/=");
			break;
		case star:
			printer.print("*=");
			break;
		case lShift:
			printer.print("<<=");
			break;
		case rSignedShift:
			printer.print(">>=");
			break;
		case rUnsignedShift:
			printer.print(">>>=");
			break;
		}
		printer.print(" ");
		n.getValue().accept(this, arg);
	}

	public void visit(BinaryExpr n, Object arg) {
		n.getLeft().accept(this, arg);
		printer.print(" ");
		switch (n.getOperator()) {
		case or:
			printer.print("||");
			break;
		case and:
			printer.print("&&");
			break;
		case binOr:
			printer.print("|");
			break;
		case binAnd:
			printer.print("&");
			break;
		case xor:
			printer.print("^");
			break;
		case equals:
			printer.print("==");
			break;
		case notEquals:
			printer.print("!=");
			break;
		case less:
			printer.print("<");
			break;
		case greater:
			printer.print(">");
			break;
		case lessEquals:
			printer.print("<=");
			break;
		case greaterEquals:
			printer.print(">=");
			break;
		case lShift:
			printer.print("<<");
			break;
		case rSignedShift:
			printer.print(">>");
			break;
		case rUnsignedShift:
			printer.print(">>>");
			break;
		case plus:
			printer.print("+");
			break;
		case minus:
			printer.print("-");
			break;
		case times:
			printer.print("*");
			break;
		case divide:
			printer.print("/");
			break;
		case remainder:
			printer.print("%");
			break;
		}
		printer.print(" ");
		n.getRight().accept(this, arg);
	}

	public void visit(CastExpr n, Object arg) {
		//System.out.println(n + " in case expr");
		printer.print("(");
		n.getType().accept(this, arg);
		printer.print(") ");
		n.getExpr().accept(this, arg);
	}

	public void visit(ClassExpr n, Object arg) {
		n.getType().accept(this, arg);
		printer.print(".class");
	}

	public void visit(ConditionalExpr n, Object arg) {
		n.getCondition().accept(this, arg);
		printer.print(" ? ");
		n.getThenExpr().accept(this, arg);
		printer.print(" : ");
		n.getElseExpr().accept(this, arg);
	}

	public void visit(EnclosedExpr n, Object arg) {
		//System.out.println(n + " in enclosed expr");
		printer.print("(");
		n.getInner().accept(this, arg);
		printer.print(")");
	}

	public void visit(FieldAccessExpr n, Object arg) {
		//System.out.println(n + " in field access expr " + "at line " + n.getBeginLine());
		n.getScope().accept(this, arg);
		printer.print(".");
		printer.print(n.getField());
	}

	public void visit(InstanceOfExpr n, Object arg) {
		n.getExpr().accept(this, arg);
		printer.print(" instanceof ");
		n.getType().accept(this, arg);
	}

	public void visit(CharLiteralExpr n, Object arg) {
		printer.print("'");
		printer.print(n.getValue());
		printer.print("'");
	}

	public void visit(DoubleLiteralExpr n, Object arg) {
		printer.print(n.getValue());
	}

	public void visit(IntegerLiteralExpr n, Object arg) {
		printer.print(n.getValue());
	}

	public void visit(LongLiteralExpr n, Object arg) {
		printer.print(n.getValue());
	}

	public void visit(IntegerLiteralMinValueExpr n, Object arg) {
		printer.print(n.getValue());
	}

	public void visit(LongLiteralMinValueExpr n, Object arg) {
		printer.print(n.getValue());
	}

	public void visit(StringLiteralExpr n, Object arg) {
		//System.out.println(n + " in string expr " + "at line " + n.getBeginLine());
		printer.print("\"");
		printer.print(n.getValue());
		printer.print("\"");
	}

	public void visit(BooleanLiteralExpr n, Object arg) {
		printer.print(n.getValue().toString());
	}

	public void visit(NullLiteralExpr n, Object arg) {
		printer.print("null");
	}

	public void visit(ThisExpr n, Object arg) {
		if (n.getClassExpr() != null) {
			n.getClassExpr().accept(this, arg);
			printer.print(".");
		}
		printer.print("this");
	}

	public void visit(SuperExpr n, Object arg) {
		if (n.getClassExpr() != null) {
			n.getClassExpr().accept(this, arg);
			printer.print(".");
		}
		printer.print("super");
	}

	public void visit(MethodCallExpr n, Object arg) {
		//System.out.println("assign expr " + n + " on line " +  n.getBeginLine());


		currentMethodCall = n;
		if (n.getScope() != null) {
			n.getScope().accept(this, arg);
			printer.print(".");
		}
		printTypeArgs(n.getTypeArgs(), arg);
		printer.print(n.getName());
		printer.print("(");
		if (n.getArgs() != null) {
			for (Iterator<Expression> i = n.getArgs().iterator(); i.hasNext();) {
				Expression e = i.next();
				e.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}
		if(n.getYield() != null) {
			if(n.getArgs() != null) {
				printer.print(", ");
			}
			printer.printLn("new Runnable(){");
			printer.printLn("@Override");
			printer.printLn("public void run(){ ");
			printer.indent();
			for(Statement s : n.getYield().getStmts()) {
				printer.printLn(s.toString());
			}
			printer.unindent();
			printer.printLn("}");
			printer.printLn("}");
		}

		printer.print(")");
		currentMethodCall = null;
	}
	
	// this gets called form method declaration when its type "House"
	public void visit(ObjectCreationExpr n, Object arg) {
		
		if (n.getScope() != null) {
			n.getScope().accept(this, arg);
			printer.print(".");
		}

		printer.print("new ");

		printTypeArgs(n.getTypeArgs(), arg);
		n.getType().accept(this, arg);

		printer.print("(");
		if (n.getArgs() != null) {
			for (Iterator<Expression> i = n.getArgs().iterator(); i.hasNext();) {
				Expression e = i.next();
				e.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}
		printer.print(")");

		if (n.getAnonymousClassBody() != null) {
			printer.printLn(" {");
			printer.indent();
			printMembers(n.getAnonymousClassBody(), arg);
			printer.unindent();
			printer.print("}");
		}
	}

	public void visit(SuperMemberAccessExpr n, Object arg) {
		printer.print("super.");
		printer.print(n.getName());
	}

	public void visit(UnaryExpr n, Object arg) {
		switch (n.getOperator()) {
		case positive:
			printer.print("+");
			break;
		case negative:
			printer.print("-");
			break;
		case inverse:
			printer.print("~");
			break;
		case not:
			printer.print("!");
			break;
		case preIncrement:
			printer.print("++");
			break;
		case preDecrement:
			printer.print("--");
			break;
		}

		n.getExpr().accept(this, arg);

		switch (n.getOperator()) {
		case posIncrement:
			printer.print("++");
			break;
		case posDecrement:
			printer.print("--");
			break;
		}
	}

	public void visit(ConstructorDeclaration n, Object arg) {

		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		printMemberAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers());

		printTypeParameters(n.getTypeParameters(), arg);
		if (n.getTypeParameters() != null) {
			printer.print(" ");
		}
		printer.print(n.getName());

		printer.print("(");
		if (n.getParameters() != null) {
			for (Iterator<Parameter> i = n.getParameters().iterator(); i.hasNext();) {
				Parameter p = i.next();
				p.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}
		printer.print(")");

		if (n.getThrows() != null) {
			printer.print(" throws ");
			for (Iterator<NameExpr> i = n.getThrows().iterator(); i.hasNext();) {
				NameExpr name = i.next();
				name.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}
		printer.print(" ");
		n.getBlock().accept(this, arg);
	}
	
	// no need to define the method as it is already defined in the scope by the typing visitor
	public void visit(MethodDeclaration n, Object arg) {

		//System.out.println(n.getType() + " at line " + n.getBeginLine());

		// get the current scope of this node
		currentScope = n.getThisNodeScope();

		// this is the return type 
		Symbol symOfVariable = currentScope.resolve(n.getType().toString());


		/*
		 * THis checks that the "type" of the variable is not null
		 * and an instance of the type interface ( i.e its already a object thats been defined 
		 * or it is a primitive type. Since both class and built in symbol are of type interface
		 * 
		 * So it only goes ahead with adding the variable to the scope if it has a proper type 
		 */

		// this check needs to be done for every variable,parameter 
		if(symOfVariable == null){
			throw new A2SemanticsException(n.getType().toString() + " on line " + n.getType().getBeginLine() + " is not a defined type");
		}
		if(!(symOfVariable instanceof symtab.Type)){
			throw new A2SemanticsException(n.getType().toString() + " on line " + n.getType().getBeginLine() + " is not a valid type");
		}


		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		printMemberAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers());

		printTypeParameters(n.getTypeParameters(), arg);
		if (n.getTypeParameters() != null) {
			printer.print(" ");
		}

		n.getType().accept(this, arg);
		printer.print(" ");
		printer.print(n.getName());

		printer.print("(");
		if (n.getParameters() != null) {
			//System.out.println(n.getName());
			// if the statements contain yield, then put runnable in the params of that method
			if((n.getBody().getStmts() != null) && (n.getBody().getStmts().toString().contains("yield;"))) {
				printer.print("Runnable r,");

			}
			for (Iterator<Parameter> i = n.getParameters().iterator(); i.hasNext();) {
				Parameter p = i.next();
				p.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}else {
			printer.print("Runnable r");
		}
		printer.print(")");

		for (int i = 0; i < n.getArrayCount(); i++) {
			printer.print("[]");
		}

		if (n.getThrows() != null) {
			printer.print(" throws ");
			for (Iterator<NameExpr> i = n.getThrows().iterator(); i.hasNext();) {
				NameExpr name = i.next();
				name.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}
		if (n.getBody() == null) {
			printer.print(";");
		} else {
			printer.print(" ");
			n.getBody().accept(this, arg);
		}
	}

	// this is inside a method param
	public void visit(Parameter n, Object arg) {

		// this prints out the parameter 
		//System.out.println(n);

		// get the current scope of this node
		currentScope = n.getThisNodeScope();

		// this is the return type 
		Symbol symOfVariable = currentScope.resolve(n.getType().toString());


		/*
		 * THis checks that the "type" of the variable is not null
		 * and an instance of the type interface ( i.e its already a object thats been defined 
		 * or it is a primitive type. Since both class and built in symbol are of type interface
		 * 
		 * So it only goes ahead with adding the variable to the scope if it has a proper type 
		 */

		// this check needs to be done for every variable,parameter 
		if(symOfVariable == null){
			throw new A2SemanticsException(n.getType().toString() + " on line " + n.getType().getBeginLine() + " is not a defined type");
		}
		if(!(symOfVariable instanceof symtab.Type)){
			throw new A2SemanticsException(n.getType().toString() + " on line " + n.getType().getBeginLine() + " is not a valid type");
		}


		// now need to check that the parameter is not defined in its current scope ONLY. 
		// DO NOT cascade up the scope because we want to define it to the scope that the parameter is in only.
		// if its not then add it to the current scope
		
		String varName = n.getId().toString(); // gets the name of the variable
		
		// check if this variable already exists in the scope
		if (n.getThisNodeScope().resolveThisScopeOnly(varName) != null) {
			throw new A2SemanticsException("Sorry this variable " + varName + " has already been defined in this scope on line " + n.getBeginLine() + ". You cannot redefine it on line " + n.getBeginLine() + " and column " + n.getBeginColumn());
		}

		// so we know that the variable has not been defined. Hence define it and add to the current scope of this node

		Symbol variableSymbol = new VariableSymbol(varName,(symtab.Type)symOfVariable);

		n.getThisNodeScope().define(variableSymbol); // adds to the scope of the method that uses this parameter


		printAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers());

		n.getType().accept(this, arg);
		if (n.isVarArgs()) {
			printer.print("...");
		}
		printer.print(" ");
		n.getId().accept(this, arg);
	}

	public void visit(ExplicitConstructorInvocationStmt n, Object arg) {
		if (n.isThis()) {
			printTypeArgs(n.getTypeArgs(), arg);
			printer.print("this");
		} else {
			if (n.getExpr() != null) {
				n.getExpr().accept(this, arg);
				printer.print(".");
			}
			printTypeArgs(n.getTypeArgs(), arg);
			printer.print("super");
		}
		printer.print("(");
		if (n.getArgs() != null) {
			for (Iterator<Expression> i = n.getArgs().iterator(); i.hasNext();) {
				Expression e = i.next();
				e.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}
		printer.print(");");
	}

	// this method gets the variable declared INSIDE a method
	public void visit(VariableDeclarationExpr n, Object arg) {

		//System.out.println(n.toString());

		// get the current scope of this node
		currentScope = n.getThisNodeScope();

		// get symbol of the type on left side of declaration
		Symbol symOfVariable = currentScope.resolve(n.getType().toString());

		//System.out.println(n.getType());

		/*
		 * THis checks that the "type" of the variable is not null
		 * and an instance of the type interface ( i.e its already a object thats been defined 
		 * or it is a primitive type. Since both class and built in symbol are of type interface
		 * 
		 * So it only goes ahead with adding the variable to the scope if it has a proper type 
		 */

		// this check needs to be done for every variable,parameter 
		if(symOfVariable == null){
			throw new A2SemanticsException(n.getType().toString() + " on line " + n.getType().getBeginLine() + " is not a defined type");
		}
		if(!(symOfVariable instanceof symtab.Type)){
			throw new A2SemanticsException(n.getType().toString() + " on line " + n.getType().getBeginLine() + " is not a valid type");
		}

		// now check if that variable exists, and if so then throw semantic exception

		printAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers());

		n.getType().accept(this, arg);
		printer.print(" ");

		for (Iterator<VariableDeclarator> i = n.getVars().iterator(); i.hasNext();) {
			VariableDeclarator v = i.next();
			v.accept(this, arg);
			if (i.hasNext()) {
				printer.print(", ");
			}

			String varName = v.getId().toString(); // gets the name of the variable
			
			// check the scope above if the scope above is a local scope . i.e a method or a block scope that is enclosing it 
			if(currentScope.getEnclosingScope() instanceof symtab.LocalScope) {
				VariableSymbol variableSymbol = (symtab.VariableSymbol)currentScope.getEnclosingScope().resolve(varName);
				if(variableSymbol != null) {
					throw new A2SemanticsException("Sorry the variable " + varName + " on line " + v.getBeginLine() + " has already been defined in the local scope above"); 
				}
			}

			// check to see if already in the current scope
			if (currentScope.resolveThisScopeOnly(varName) != null) {
				throw new A2SemanticsException("Sorry the variable " + varName + " has already been defined in the method.  You cannot redefine it on line " + n.getBeginLine());
			}

			// so we know that the variable has not been defined. Hence define it and add to the current scope of this node
			VariableSymbol variableSymbol = new VariableSymbol(varName,(symtab.Type)symOfVariable);
			
			// set the line number of the declration symbol
			variableSymbol.setLineNumber(n.getBeginLine());

			currentScope.define(variableSymbol); // adds to the scope of the node
		}
	}

	public void visit(TypeDeclarationStmt n, Object arg) {
		//System.out.println(n);
		n.getTypeDeclaration().accept(this, arg);
	}

	public void visit(AssertStmt n, Object arg) {
		printer.print("assert ");
		n.getCheck().accept(this, arg);
		if (n.getMessage() != null) {
			printer.print(" : ");
			n.getMessage().accept(this, arg);
		}
		printer.print(";");
	}

	public void visit(BlockStmt n, Object arg) {
		
		//System.out.println(n + " on line " + n.getBeginLine());
		
		printer.printLn("{");
		if (n.getStmts() != null) {
			printer.indent();
			for (Statement s : n.getStmts()) {
				s.accept(this, arg);
				printer.printLn();
			}
			printer.unindent();
		}
		printer.print("}");

	}

	public void visit(LabeledStmt n, Object arg) {
		printer.print(n.getLabel());
		printer.print(": ");
		n.getStmt().accept(this, arg);
	}

	public void visit(EmptyStmt n, Object arg) {
		printer.print(";");
	}

	// This is called before the MethodCallExpr is called. So check RHS here.
	public void visit(ExpressionStmt n, Object arg) {
		//System.out.println(n);
		//System.out.println(n + " in  expr " + "at line " + n.getBeginLine());
		n.getExpression().accept(this, arg);
		printer.print(";");
	}

	public void visit(SwitchStmt n, Object arg) {
		printer.print("switch(");
		n.getSelector().accept(this, arg);
		printer.printLn(") {");
		if (n.getEntries() != null) {
			printer.indent();
			for (SwitchEntryStmt e : n.getEntries()) {
				e.accept(this, arg);
			}
			printer.unindent();
		}
		printer.print("}");

	}

	public void visit(SwitchEntryStmt n, Object arg) {
		if (n.getLabel() != null) {
			printer.print("case ");
			n.getLabel().accept(this, arg);
			printer.print(":");
		} else {
			printer.print("default:");
		}
		printer.printLn();
		printer.indent();
		if (n.getStmts() != null) {
			for (Statement s : n.getStmts()) {
				s.accept(this, arg);
				printer.printLn();
			}
		}
		printer.unindent();
	}

	public void visit(BreakStmt n, Object arg) {
		printer.print("break");
		if (n.getId() != null) {
			printer.print(" ");
			printer.print(n.getId());
		}
		printer.print(";");
	}

	public void visit(ReturnStmt n, Object arg) {
		printer.print("return");
		if (n.getExpr() != null) {
			printer.print(" ");
			n.getExpr().accept(this, arg);
		}
		printer.print(";");
	}

	public void visit(EnumDeclaration n, Object arg) {
		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		printMemberAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers());

		printer.print("enum ");
		printer.print(n.getName());

		if (n.getImplements() != null) {
			printer.print(" implements ");
			for (Iterator<ClassOrInterfaceType> i = n.getImplements().iterator(); i.hasNext();) {
				ClassOrInterfaceType c = i.next();
				c.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}

		printer.printLn(" {");
		printer.indent();
		if (n.getEntries() != null) {
			printer.printLn();
			for (Iterator<EnumConstantDeclaration> i = n.getEntries().iterator(); i.hasNext();) {
				EnumConstantDeclaration e = i.next();
				e.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}
		if (n.getMembers() != null) {
			printer.printLn(";");
			printMembers(n.getMembers(), arg);
		} else {
			if (n.getEntries() != null) {
				printer.printLn();
			}
		}
		printer.unindent();
		printer.print("}");
	}

	public void visit(EnumConstantDeclaration n, Object arg) {
		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		printMemberAnnotations(n.getAnnotations(), arg);
		printer.print(n.getName());

		if (n.getArgs() != null) {
			printer.print("(");
			for (Iterator<Expression> i = n.getArgs().iterator(); i.hasNext();) {
				Expression e = i.next();
				e.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
			printer.print(")");
		}

		if (n.getClassBody() != null) {
			printer.printLn(" {");
			printer.indent();
			printMembers(n.getClassBody(), arg);
			printer.unindent();
			printer.printLn("}");
		}
	}

	public void visit(EmptyMemberDeclaration n, Object arg) {
		//System.out.println(n + " in string expr " + "at line " + n.getBeginLine());
		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		printer.print(";");
	}

	public void visit(InitializerDeclaration n, Object arg) {
		//System.out.println(n + " " + n.getBeginLine());
		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		if (n.isStatic()) {
			printer.print("static ");
		}
		n.getBlock().accept(this, arg);
	}

	public void visit(IfStmt n, Object arg) {
		printer.print("if (");
		n.getCondition().accept(this, arg);
		printer.print(") ");
		n.getThenStmt().accept(this, arg);
		if (n.getElseStmt() != null) {
			printer.print(" else ");
			n.getElseStmt().accept(this, arg);
		}
	}

	public void visit(WhileStmt n, Object arg) {
		printer.print("while (");
		n.getCondition().accept(this, arg);
		printer.print(") ");
		n.getBody().accept(this, arg);
	}

	public void visit(ContinueStmt n, Object arg) {
		printer.print("continue");
		if (n.getId() != null) {
			printer.print(" ");
			printer.print(n.getId());
		}
		printer.print(";");
	}



	public void visit(DoStmt n, Object arg) {
		printer.print("do ");
		n.getBody().accept(this, arg);
		printer.print(" while (");
		n.getCondition().accept(this, arg);
		printer.print(");");
	}

	public void visit(ForeachStmt n, Object arg) {
		printer.print("for (");
		n.getVariable().accept(this, arg);
		printer.print(" : ");
		n.getIterable().accept(this, arg);
		printer.print(") ");
		n.getBody().accept(this, arg);
	}

	public void visit(ForStmt n, Object arg) {
		
		printer.print("for (");
		if (n.getInit() != null) {
			
			for (Iterator<Expression> i = n.getInit().iterator(); i.hasNext();) {
				Expression e = i.next();
				e.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}
		printer.print("; ");
		if (n.getCompare() != null) {
			n.getCompare().accept(this, arg);
		}
		printer.print("; ");
		if (n.getUpdate() != null) {
			for (Iterator<Expression> i = n.getUpdate().iterator(); i.hasNext();) {
				Expression e = i.next();
				e.accept(this, arg);
				if (i.hasNext()) {
					printer.print(", ");
				}
			}
		}
		printer.print(") ");
		n.getBody().accept(this, arg);
	}

	public void visit(ThrowStmt n, Object arg) {
		printer.print("throw ");
		n.getExpr().accept(this, arg);
		printer.print(";");
	}

	public void visit(SynchronizedStmt n, Object arg) {
		printer.print("synchronized (");
		n.getExpr().accept(this, arg);
		printer.print(") ");
		n.getBlock().accept(this, arg);
	}

	public void visit(TryStmt n, Object arg) {
		printer.print("try ");
		n.getTryBlock().accept(this, arg);
		if (n.getCatchs() != null) {
			for (CatchClause c : n.getCatchs()) {
				c.accept(this, arg);
			}
		}
		if (n.getFinallyBlock() != null) {
			printer.print(" finally ");
			n.getFinallyBlock().accept(this, arg);
		}
	}

	public void visit(CatchClause n, Object arg) {
		printer.print(" catch (");
		n.getExcept().accept(this, arg);
		printer.print(") ");
		n.getCatchBlock().accept(this, arg);

	}

	public void visit(AnnotationDeclaration n, Object arg) {
		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		printMemberAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers());

		printer.print("@interface ");
		printer.print(n.getName());
		printer.printLn(" {");
		printer.indent();
		if (n.getMembers() != null) {
			printMembers(n.getMembers(), arg);
		}
		printer.unindent();
		printer.print("}");
	}

	public void visit(AnnotationMemberDeclaration n, Object arg) {
		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		printMemberAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers());

		n.getType().accept(this, arg);
		printer.print(" ");
		printer.print(n.getName());
		printer.print("()");
		if (n.getDefaultValue() != null) {
			printer.print(" default ");
			n.getDefaultValue().accept(this, arg);
		}
		printer.print(";");
	}

	public void visit(MarkerAnnotationExpr n, Object arg) {
		printer.print("@");
		n.getName().accept(this, arg);
	}

	public void visit(SingleMemberAnnotationExpr n, Object arg) {
		printer.print("@");
		n.getName().accept(this, arg);
		printer.print("(");
		n.getMemberValue().accept(this, arg);
		printer.print(")");
	}

	public void visit(NormalAnnotationExpr n, Object arg) {
		printer.print("@");
		n.getName().accept(this, arg);
		printer.print("(");
		for (Iterator<MemberValuePair> i = n.getPairs().iterator(); i.hasNext();) {
			MemberValuePair m = i.next();
			m.accept(this, arg);
			if (i.hasNext()) {
				printer.print(", ");
			}
		}
		printer.print(")");
	}

	public void visit(MemberValuePair n, Object arg) {
		//System.out.println(n + " in string expr " + "at line " + n.getBeginLine());
		printer.print(n.getName());
		printer.print(" = ");
		n.getValue().accept(this, arg);
	}

	public void visit(LineComment n, Object arg) {
		printer.print("//");
		printer.printLn(n.getContent());
	}

	public void visit(BlockComment n, Object arg) {
		printer.print("/*");
		printer.print(n.getContent());
		printer.printLn("*/");
	}


	@Override
	public void visit(YieldStmt n, Object arg) {
		// TODO Auto-generated method stub
		printer.printLn("r.run();");
	}
}
