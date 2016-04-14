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
import symtab.ClassSymbol;
import symtab.ConstructorSymbol;
import symtab.GlobalScope;
import symtab.LocalScope;
import symtab.MethodSymbol;
import symtab.Scope;
import symtab.Symbol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Julio Vilmar Gesser
 */

/**
 * This class is used to just assign scopes to the nodes
 * @author anmoldesai
 *
 */
public final class TypingVisitor implements VoidVisitor<Object> {

	private final SourcePrinter printer = new SourcePrinter();

	private MethodCallExpr currentMethodCall = null;

	// create a global scope with the primitive, String
	GlobalScope globalScope = new GlobalScope();

	// assign the current scope as the global scope
	Scope currentScope = globalScope;


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
		
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

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
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printAnnotations(n.getAnnotations(), arg);
		printer.print("package ");
		n.getName().accept(this, arg);
		printer.printLn(";");
		printer.printLn();
	}

	public void visit(NameExpr n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printer.print(n.getName());
	}

	public void visit(QualifiedNameExpr n, Object arg) {
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		n.getQualifier().accept(this, arg);
		printer.print(".");
		printer.print(n.getName());
	}

	public void visit(ImportDeclaration n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
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

	// the class or interface is being declared ( i.e public class ...(){} )
	public void visit(ClassOrInterfaceDeclaration n, Object arg) {


		// This print checks that it is a class 
		//System.out.println(n.getName());

		String name = n.getName();

		// this makes a new symbol scope where the current scope is the enclosing scope
		ClassSymbol classSym = new ClassSymbol(name,currentScope);

		// set the class as a symbol in the current scope, which is the global scope
		if(currentScope.resolve(name) == null) {
			currentScope.define(classSym);
		}else {
			throw new A2SemanticsException("Sorry, something with the same name already exists in this scope, " + "The duplicate is at line " + n.getBeginLine());
		}
		

		// make the current scope the class symbol we made
		currentScope = classSym;

		// set the scope of this node to the scope we made as classSym
		n.setThisNodeScope(classSym);

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

		// pop the scope to the enclosing scope 
		currentScope = currentScope.getEnclosingScope();
	}

	public void visit(EmptyTypeDeclaration n, Object arg) {

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		printer.print(";");
	}

	public void visit(JavadocComment n, Object arg) {

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

		printer.print("/**");
		printer.print(n.getContent());
		printer.printLn("*/");
	}

	public void visit(ClassOrInterfaceType n, Object arg) {

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);


		if (n.getScope() != null) {
			n.getScope().accept(this, arg);
			printer.print(".");
		}
		printer.print(n.getName());
		printTypeArgs(n.getTypeArgs(), arg);
	}

	public void visit(TypeParameter n, Object arg) {

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

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

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

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

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

		n.getType().accept(this, arg);
		for (int i = 0; i < n.getArrayCount(); i++) {
			printer.print("[]");
		}
	}

	public void visit(WildcardType n, Object arg) {

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

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

	// this is the field declaration that is in a class but outside a method
	public void visit(FieldDeclaration n, Object arg) {

		//System.out.println(n.toString());

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		printMemberAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers());
		n.getType().accept(this, arg);

		printer.print(" ");
		for (Iterator<VariableDeclarator> i = n.getVariables().iterator(); i.hasNext();) {
			VariableDeclarator var = i.next();
			var.accept(this, arg);
			if (i.hasNext()) {
				printer.print(", ");
			}
		}

		printer.print(";");
	}

	// THis is visited by the ABOVE visit method. This happens when the variable is being declared
	public void visit(VariableDeclarator n, Object arg) {

		//System.out.println(n.toString()+ " on line " + n.getBeginLine());

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

		n.getId().accept(this, arg);
		if (n.getInit() != null) {
			printer.print(" = ");
			n.getInit().accept(this, arg);
		}
	}

	public void visit(VariableDeclaratorId n, Object arg) {

		//System.out.println(n.getName().toString()+ " on line " + n.getBeginLine());

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

		printer.print(n.getName());
		for (int i = 0; i < n.getArrayCount(); i++) {
			printer.print("[]");
		}
	}

	public void visit(ArrayInitializerExpr n, Object arg) {
		
		//System.out.println(n.getValues());

		//System.out.println(n.toString()+ " on line " + n.getBeginLine());

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

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

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

		n.getName().accept(this, arg);
		printer.print("[");
		n.getIndex().accept(this, arg);
		printer.print("]");
	}

	public void visit(ArrayCreationExpr n, Object arg) {
		
		//System.out.println(n.toString());

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

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

	// any time a assignment expression happens. e.g a = false. NOT DECLARATION
	public void visit(AssignExpr n, Object arg) {

		//System.out.println(n.toString() + " on line " + n.getBeginLine());

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

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
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

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

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

		printer.print("(");
		n.getType().accept(this, arg);
		printer.print(") ");
		n.getExpr().accept(this, arg);
	}

	public void visit(ClassExpr n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

		n.getType().accept(this, arg);
		printer.print(".class");
	}

	public void visit(ConditionalExpr n, Object arg) {

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);


		n.getCondition().accept(this, arg);
		printer.print(" ? ");
		n.getThenExpr().accept(this, arg);
		printer.print(" : ");
		n.getElseExpr().accept(this, arg);
	}

	public void visit(EnclosedExpr n, Object arg) {

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

		printer.print("(");
		n.getInner().accept(this, arg);
		printer.print(")");
	}

	// accessing a field
	public void visit(FieldAccessExpr n, Object arg) {

		//System.out.println(n.toString() + " on line " + n.getBeginLine());

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

		n.getScope().accept(this, arg);
		printer.print(".");
		printer.print(n.getField());
	}

	public void visit(InstanceOfExpr n, Object arg) {

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

		n.getExpr().accept(this, arg);
		printer.print(" instanceof ");
		n.getType().accept(this, arg);
	}

	public void visit(CharLiteralExpr n, Object arg) {

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

		printer.print("'");
		printer.print(n.getValue());
		printer.print("'");
	}

	public void visit(DoubleLiteralExpr n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		printer.print(n.getValue());
	}

	public void visit(IntegerLiteralExpr n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		printer.print(n.getValue());
	}

	public void visit(LongLiteralExpr n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		printer.print(n.getValue());
	}

	public void visit(IntegerLiteralMinValueExpr n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		printer.print(n.getValue());
	}

	public void visit(LongLiteralMinValueExpr n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		printer.print(n.getValue());
	}

	public void visit(StringLiteralExpr n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		printer.print("\"");
		printer.print(n.getValue());
		printer.print("\"");
	}

	public void visit(BooleanLiteralExpr n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		printer.print(n.getValue().toString());
	}

	public void visit(NullLiteralExpr n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		printer.print("null");
	}

	public void visit(ThisExpr n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		if (n.getClassExpr() != null) {
			n.getClassExpr().accept(this, arg);
			printer.print(".");
		}
		printer.print("this");
	}

	public void visit(SuperExpr n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		if (n.getClassExpr() != null) {
			n.getClassExpr().accept(this, arg);
			printer.print(".");
		}
		printer.print("super");
	}

	//  no new scope since its just a call
	public void visit(MethodCallExpr n, Object arg) {
		
		//System.out.println(n.getYield() + " on line " + n.getBeginLine());

		// just sets enclosing the current scope of this node
		n.setThisNodeScope(currentScope);
		
		if(n.getYield() != null) {
		n.getYield().accept(this, arg);
		}
		
		//if(n.getYield() != null) {
			//n.getYield().setIsBlock(true);
		//}
		//if(n.getYield() != null) {
			//System.out.println(n.getYield().getIsBlock() + " on line " + n.getBeginLine() + " typing visitor");
			//System.out.println((n.getYield() instanceof BlockStmt) + " on line " + n.getBeginLine() + " typing visitor");
		//}
		

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
		/*
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
		
		*/

		printer.print(")");
		currentMethodCall = null;
	}

	public void visit(ObjectCreationExpr n, Object arg) {

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

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

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

		printer.print("super.");
		printer.print(n.getName());
	}

	public void visit(UnaryExpr n, Object arg) {

		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);

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

		// gets the constructor
		//System.out.println(n.getTypeParameters());
		
		Symbol symOfVariable = currentScope.resolve(n .getName()); // find the symbol in the scopes above
		
		// create symbol with enclosing scope
		symtab.ConstructorSymbol constructorSym = new symtab.ConstructorSymbol(n.getName(),(symtab.Type)symOfVariable,currentScope);
		
		if(currentScope.resolveThisScopeOnly(n.getName()) == null) {
			currentScope.define(constructorSym);
		}else {
			throw new A2SemanticsException("Sorry, constructor with the same name already exists in this scope. " + " Duplicate is at line " + n.getBeginLine() );
		}
		
		// set the scope of this node to the current scope
		n.setThisNodeScope(constructorSym);

		// set the current scope as the method scope 
		currentScope = constructorSym;
		

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
		n.getBlock().accept(this, arg); // this accepts a block statement.
		
		currentScope = currentScope.getEnclosingScope();
	}

	public void visit(MethodDeclaration n, Object arg) {
		

		// get the symbol of the returning type from the current scope (which we put as enclosing scope in the constructor)
		Symbol symOfVariable = currentScope.resolve(n.getType().toString()); // this is passed in when creating the symbol 
 
		// create a new scope and cast the type of the type to symtab.Type and pass in enclosing scope
		symtab.MethodSymbol methodSym = new symtab.MethodSymbol(n.getName(), (symtab.Type)symOfVariable ,currentScope,n.getParameters());
		
		//System.out.println("name used was " + n.getName() + " on line " + n.getBeginLine());

		// add this to the current scope by first checking that a method with the same name does not already exist 
		// inside the current scope 
		
		// this only looks at the current scope because the scope above could have the method with the same name
		// i.e it could be a "outer" class.
		if(currentScope.resolveThisScopeOnly(n.getName()) == null) {
			currentScope.define(methodSym);
		}else {
			throw new A2SemanticsException("Sorry, method or class with the same name already exists in this scope. " + "The duplicate is at " + n.getBeginLine() );
		}
		//currentScope.define(methodSym);

		// just sets the current scope of this node to the method scope we made
		n.setThisNodeScope(methodSym);

		// set the current scope as the method scope 
		currentScope = methodSym;
		
		//System.out.println("the scope is method scope with " + n + " " + currentScope.toString());



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

		// pop the scope to the enclosing scope 
		currentScope = currentScope.getEnclosingScope();
	}

	public void visit(Parameter n, Object arg) {
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers());

		n.getType().accept(this, arg);
		if (n.isVarArgs()) {
			printer.print("...");
		}
		printer.print(" ");
		n.getId().accept(this, arg);
	}

	// stop the overloading of constructors
	public void visit(ExplicitConstructorInvocationStmt n, Object arg) {
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
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

	public void visit(VariableDeclarationExpr n, Object arg) {
		
		//System.out.println(n + " " + n.getBeginLine());
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
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
		}
	}

	public void visit(TypeDeclarationStmt n, Object arg) {
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		n.getTypeDeclaration().accept(this, arg);
	}

	public void visit(AssertStmt n, Object arg) {
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printer.print("assert ");
		n.getCheck().accept(this, arg);
		if (n.getMessage() != null) {
			printer.print(" : ");
			n.getMessage().accept(this, arg);
		}
		printer.print(";");
	}
	
	// This is a BLOCK that is used in if,while,for, CONSTRUCTOR etc
	public void visit(BlockStmt n, Object arg) {
		
		//System.out.println(n.getStmts() + "\n\n\n\n");
		
		//System.out.println(currentScope);
		
		// create new local scope symbol
		symtab.LocalScope localScope = new symtab.LocalScope(currentScope);
		
		// set currentscope as that of the local scope created
		currentScope = localScope;
		
		// set the scope of this node to the local scope created 
		n.setThisNodeScope(currentScope);
		
		// testing by ensuring that it is a block statement node
		n.setIsBlock(true);
	
		
		//System.out.println("the scope is block scope with " + n.getStmts());
		//System.out.println("enclosing scope is " + currentScope.getEnclosingScope().toString());
		
		
		printer.printLn("{");
		if (n.getStmts() != null) {
			printer.indent();
			for (Statement s : n.getStmts()) {
				//System.out.println(s + " in typing v");
				s.accept(this, arg);
				printer.printLn();
			}
			printer.unindent();
		}
		printer.print("}");
		
		// sets the current scope back to the enclosing scope
		currentScope = currentScope.getEnclosingScope();

	}

	public void visit(LabeledStmt n, Object arg) {
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printer.print(n.getLabel());
		printer.print(": ");
		n.getStmt().accept(this, arg);
	}

	public void visit(EmptyStmt n, Object arg) {
		
		// just sets the current scope of this 
		//n.setThisNodeScope(currentScope);
		
		printer.print(";");
	}

	public void visit(ExpressionStmt n, Object arg) {
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		n.getExpression().accept(this, arg);
		printer.print(";");
	}

	/**
	 * 
	 * The switch statement has curly braces. Hence it has a scope
	 * So set the local scope 
	 */
	public void visit(SwitchStmt n, Object arg) {
		
		symtab.LocalScope localScope = new symtab.LocalScope(currentScope);
		
		// cannot define this block statement scope in the current scope ?
		
		currentScope = localScope;
		
		// set the scope of this node to the local scope created 
		n.setThisNodeScope(localScope);
		
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
		
		// set the current scope back to the enclosing scope
		currentScope = currentScope.getEnclosingScope();

	}

	public void visit(SwitchEntryStmt n, Object arg) {
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
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
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printer.print("break");
		if (n.getId() != null) {
			printer.print(" ");
			printer.print(n.getId());
		}
		printer.print(";");
	}

	public void visit(ReturnStmt n, Object arg) {
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printer.print("return");
		if (n.getExpr() != null) {
			printer.print(" ");
			n.getExpr().accept(this, arg);
		}
		printer.print(";");
	}

	public void visit(EnumDeclaration n, Object arg) {
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
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
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
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
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		printer.print(";");
	}

	public void visit(InitializerDeclaration n, Object arg) {
		
		//System.out.println(n + " " + n.getBeginLine() + " began initializer declaration");
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		if (n.isStatic()) {
			printer.print("static ");
		}
		n.getBlock().accept(this, arg);
	}

	public void visit(IfStmt n, Object arg) {
		
		n.setThisNodeScope(currentScope);
		
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
		n.setThisNodeScope(currentScope);
		
		printer.print("while (");
		n.getCondition().accept(this, arg);
		printer.print(") ");
		n.getBody().accept(this, arg);
	}

	public void visit(ContinueStmt n, Object arg) {
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printer.print("continue");
		if (n.getId() != null) {
			printer.print(" ");
			printer.print(n.getId());
		}
		printer.print(";");
	}



	public void visit(DoStmt n, Object arg) {
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printer.print("do ");
		n.getBody().accept(this, arg);
		printer.print(" while (");
		n.getCondition().accept(this, arg);
		printer.print(");");
	}

	public void visit(ForeachStmt n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printer.print("for (");
		n.getVariable().accept(this, arg);
		printer.print(" : ");
		n.getIterable().accept(this, arg);
		printer.print(") ");
		n.getBody().accept(this, arg);
	}

	public void visit(ForStmt n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
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
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printer.print("throw ");
		n.getExpr().accept(this, arg);
		printer.print(";");
	}

	public void visit(SynchronizedStmt n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printer.print("synchronized (");
		n.getExpr().accept(this, arg);
		printer.print(") ");
		n.getBlock().accept(this, arg);
	}

	public void visit(TryStmt n, Object arg) {
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
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
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printer.print(" catch (");
		n.getExcept().accept(this, arg);
		printer.print(") ");
		n.getCatchBlock().accept(this, arg);

	}

	public void visit(AnnotationDeclaration n, Object arg) {
		
		
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
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
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
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
		printer.print(n.getName());
		printer.print(" = ");
		n.getValue().accept(this, arg);
	}

	public void visit(LineComment n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printer.print("//");
		printer.printLn(n.getContent());
	}

	public void visit(BlockComment n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		printer.print("/*");
		printer.print(n.getContent());
		printer.printLn("*/");
	}


	@Override
	public void visit(YieldStmt n, Object arg) {
		// just sets the current scope of this 
		n.setThisNodeScope(currentScope);
		
		// TODO Auto-generated method stub
		printer.printLn("r.run();");
	}
}
