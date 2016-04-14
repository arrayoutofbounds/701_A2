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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Julio Vilmar Gesser
 */

public final class ResolvingVisitor implements VoidVisitor<Object> {

	private final SourcePrinter printer = new SourcePrinter();

	private MethodCallExpr currentMethodCall = null;

	private MethodDeclaration currentMethodDeclaration = null;

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

		// set the current scope to that of the declaration node
		currentScope = n.getThisNodeScope();

		// get symbol of the type on LHS
		Symbol symOfVariable = currentScope.resolve(n.getType().toString());

		// get the type of the lhs
		symtab.Type lhs = (symtab.Type)symOfVariable;


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

			Expression rhs = var.getInit();

			// this gets the type, like int, boolean, foo() etc
			symtab.Type typeOfVariableRhs = getTypeOfExpression(currentScope,rhs);


			if (typeOfVariableRhs != null) {

				//System.out.println(typeOfVariableRhs.getName().toString());
				//System.out.println(((symtab.Type)symOfVariable).getName());

				if(!(typeOfVariableRhs.getName().toString().equals(((symtab.Type)symOfVariable).getName()))) {
					throw new A2SemanticsException("Sorry, " +  "cannot assign " + typeOfVariableRhs.getName().toString() + " to " +  ((symtab.Type)symOfVariable).getName() + " on line " + n.getBeginLine() + " as the types are different");
				}

			}else {

			}

			if (i.hasNext()) {
				printer.print(", ");
			}
		}

		printer.print(";");
	}

	public void visit(VariableDeclarator n, Object arg) {

		n.getId().accept(this, arg);
		if (n.getInit() != null) {
			printer.print(" = ");
			currentScope = n.getThisNodeScope();

			Expression rhs = n.getInit();

			//System.out.println(n.getId());

			// this gets the type, like int, boolean, foo() etc

			////symtab.Type typeOfVariableRhs = getTypeOfExpression(currentScope,rhs);

			// if the expression is null then declaration is not valid. Assuming "=" is already printed.

			////if(typeOfVariableRhs == null){
			////throw new A2SemanticsException("Sorry, the expression " + rhs +  " on right hand side " + " on line " + n.getBeginLine() + " has not been declared");
			////}


			// if types are correct then go ahead
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
		boolean isMethod = false;
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

				// method call expr
				// get the method in the scope and if its not in the scope then throw error
				else if(init.getClass() == MethodCallExpr.class) {
					String find = "";

					if(init.toString().indexOf("(") > -1) {
						find = init.toString().substring(0, init.toString().indexOf("("));
						//System.out.println(find);
						sym = currentScope.resolve(find);
						if(sym == null) {
							throw new A2SemanticsException("Sorry the method " + init.toString() + " you have called on " + init.getBeginLine() + " has not been declared");
						}
						isMethod = true;
					}else {
						isMethod = false;
						throw new A2SemanticsException("Sorry the word " + init.toString() + " you have called on " + init.getBeginLine() + " is not a valid method call");
					}

				}else if(init.getClass() == FieldAccessExpr.class) {
					//System.out.println(init.toString().split(".")[0]);
					//sym = currentScope.resolve(init.toString());
				}else if(init.getClass() == ObjectCreationExpr.class) {
					// this is when its a object being created
					//System.out.println(init.toString());
					//System.out.println(init.toString().split("new")[1]);

					int i = init.toString().split("new")[1].indexOf("(");
					//System.out.println(init.toString().split("new")[1].substring(0, i).trim());

					sym = currentScope.resolve(init.toString().split("new")[1].substring(0, i).trim());
				}else if(init.getClass() == BinaryExpr.class) {

				}

				else{
					System.out.println("Add " + init.getClass() + " to getTypeofExpression helper method");
				}

				// if its a method then set the type as that of the method symbol retreived
				if(isMethod) {
					type = (symtab.Type) sym.getType();
				}else {
					type = (symtab.Type)sym; 
				}


				//System.out.println(type +  " " + init.getBeginLine());
			}
		}
		return type;
	}

	public void visit(AssignExpr n, Object arg) {

		//System.out.println(n + " on line " + n.getBeginLine());


		// get scope of the node
		currentScope = n.getThisNodeScope();

		// get the lhs and rhs of the assignment
		Expression lhs = n.getTarget();
		Expression rhs = n.getValue();

		// this gets the type, like int, boolean, foo etc
		symtab.Type typeOfVariableLhs = getTypeOfExpression(currentScope,lhs);
		symtab.Type typeOfVariableRhs = getTypeOfExpression(currentScope,rhs);

		if(typeOfVariableRhs == null){
			throw new A2SemanticsException("Sorry, RHS cannot be null on line " + rhs.getBeginLine());
		}
		if(typeOfVariableRhs != typeOfVariableLhs){
			throw new A2SemanticsException("Cannot convert from " + typeOfVariableRhs.getName() + " to " + typeOfVariableLhs.getName() + " on line " + n.getBeginLine());
		}

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
		printer.print("(");
		n.getInner().accept(this, arg);
		printer.print(")");
	}

	public void visit(FieldAccessExpr n, Object arg) {
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


	private void checkNumberOfArgs(Scope currentScope, MethodCallExpr methodCallNode) {

		String name = methodCallNode.getName();
		MethodSymbol s = (MethodSymbol) currentScope.resolve(name);
		if (s == null) {
			throw new A2SemanticsException("Sorry, could not find method call " + methodCallNode.getName() + " on line " + methodCallNode.getBeginLine());
		}
		// now get the params and body 
		List<Parameter> params = s.getParams();
		BlockStmt body = s.getBody();

		// this prints out the params 
		//System.out.println(p);


		if(params != null) {

			if(params.size() != methodCallNode.getArgs().size()) {
				throw new A2SemanticsException("Sorry, you seem to have different arguments on line " + methodCallNode.getBeginLine() + " when you called method compared to the parameters of that method declaration");
			}

			//for(Expression e : methodCallNode.getArgs()) {
			//System.out.println(e.toString());
			//}
		}else {
			if(methodCallNode.getArgs() != null) {
				throw new A2SemanticsException("Sorry, you seem to have arguments on line " + methodCallNode.getBeginLine() + " when you called method whereas the method declaration has no parameters");
			}
		}


	}

	/*
	 * This checks and ensures that the type of the arugments called and declared in the method are the same
	 */
	private void checkArgsType(Scope currentScope,MethodCallExpr methodCallNode) {

		String name = methodCallNode.getName();
		// find the method declared in the scope
		MethodSymbol s = (MethodSymbol) currentScope.resolve(name);
		if (s == null) {
			throw new A2SemanticsException("Sorry, could not find method call " + methodCallNode.getName() + " on line " + methodCallNode.getBeginLine());
		}

		int listLength = 0;
		if(s.getParams() != null) {
			listLength = s.getParams().size();
		}else {
			if(methodCallNode.getArgs() != null) {
				throw new A2SemanticsException("Sorry the method declaration on has no parameters, hence the method call on line " + methodCallNode.getBeginLine() + " should have no params");
			}
		}

		List<Parameter> params = s.getParams();
		List<Expression> args = methodCallNode.getArgs();

		//System.out.println(params.get(0).getType().toString());


		String paramType;
		String argType;

		// for each parameter, gets its type and check to see if they are the same
		for(int i=0;i<listLength;i++) {
			// get from method declaration
			paramType = params.get(i).getType().toString();

			// get type from method call argument
			symtab.Type typeOfArg = getTypeOfExpression(currentScope,args.get(i));
			argType = typeOfArg.getName();
			//System.out.println(argType);

			if(!(paramType.equals(argType))) {
				throw new A2SemanticsException("Sorry, the method call argument of " + '"' + args.get(i) + '"'  + " on line " + args.get(i).getBeginLine() + " does not match its coressponding type in the method declaration at line " + params.get(i).getBeginLine() );
			}
		}
	}

	// Make sure that when method is called, the number of arguments and their types match up
	public void visit(MethodCallExpr n, Object arg) {
		
		/*
		if(n.getYield() != null) {
			System.out.println(n.getYield().getIsBlock() + " on line " + n.getBeginLine() + " resolving visitor");
			System.out.println((n.getYield() instanceof BlockStmt) + " on line " + n.getBeginLine() + " resolving visitor");
		}
		*/

		//if(n.getYield() != null) {
		//System.out.println(n.getYield());
		//}

		currentScope = n.getThisNodeScope();
		
		if(n.getYield() != null) {
		n.getYield().accept(this, arg);
		}

		//System.out.println(n.getArgs() + " called on " + n.getBeginLine());
		currentMethodCall = n;


		if (n.getScope() != null) {
			n.getScope().accept(this, arg);
			printer.print(".");
		}
		printTypeArgs(n.getTypeArgs(), arg);
		printer.print(n.getName());
		printer.print("(");
		if (n.getArgs() != null) {


			if(n.getName().equals("println")) {
				List<Expression> args = n.getArgs();
				symtab.Type typeOfArg = getTypeOfExpression(currentScope,args.get(0));
				symtab.Type stringType = (symtab.Type)currentScope.resolve("String");

				if(typeOfArg != stringType) {
					throw new A2SemanticsException("Println cannot accept a argument that is not string");
				}
			}else {
				// check number of arguments. If it passses then method call has correct number of arguments
				checkNumberOfArgs(currentScope,n);

				// ensure the type of the args is correct compared with method declaration
				checkArgsType(currentScope,n);	
			}

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

	public void visit(ObjectCreationExpr n, Object arg) {

		// set current scope as that of object node
		currentScope = n.getThisNodeScope();

		// get the symbol of the class of the object being created. i.e House
		Symbol symOfVariable = currentScope.resolve(n.getType().toString());

		/*
		 * THis checks that the "type" of the variable is not null
		 * and an instance of the type interface ( i.e its already a object thats been defined 
		 * or it is a primitive type. Since both class and built in symbol are of type interface
		 * 
		 * So it only goes ahead with adding the variable to the scope if it has a proper type 
		 */

		// this check needs to be done for every variable,parameter 
		// the houes class symbol must exist
		if(symOfVariable == null){
			throw new A2SemanticsException(n.getType().toString() + " on line " + n.getType().getBeginLine() + " is not a defined type");
		}
		// the house class symbol must be a class or a built in type 
		if(!(symOfVariable instanceof symtab.Type)){
			throw new A2SemanticsException(n.getType().toString() + " on line " + n.getType().getBeginLine() + " is not a valid type");
		}


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

	public void visit(MethodDeclaration n, Object arg) {

		// check that if node is a yield then it must have a yield sta

		// check if the body has a return statement
		// get the value its returning
		// ensure that the return type of method is the same as that of the actual value being returned.

		Statement returnStatement = null;
		int returnStatementLine = 0;

		currentMethodDeclaration = n;

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
		currentMethodDeclaration = null;
	}

	public void visit(Parameter n, Object arg) {
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



	public void visit(VariableDeclarationExpr n, Object arg) {

		// set current scope to that of this node
		currentScope = n.getThisNodeScope();

		// get the symbol of the type of the declaration
		Symbol symOfVariable = currentScope.resolve(n.getType().toString());


		// get the type of the lhs of the declaration. i.e int a  the type is int
		symtab.Type lhs = (symtab.Type)symOfVariable;


		printAnnotations(n.getAnnotations(), arg);
		printModifiers(n.getModifiers());

		n.getType().accept(this, arg);
		printer.print(" ");

		for (Iterator<VariableDeclarator> i = n.getVars().iterator(); i.hasNext();) {
			VariableDeclarator v = i.next();

			// get the expression on the rhs
			Expression rhs = v.getInit();

			//System.out.println(v.getInit());

			if(rhs != null) {


				// if there is a variable inside this scope only. i.e the method only since variables outside will
				// be caught with the typing visitor
				// if there is a symbol that exists for the rhs of the expression then enter if statement
				if(currentScope.resolveThisScopeOnly(v.getInit().toString()) != null) {
					symtab.VariableSymbol variable = null;
					try {
						// get the variable that exists for rhs of expression in the scope
						variable = (symtab.VariableSymbol)currentScope.resolveThisScopeOnly(v.getInit().toString());
					}catch(Exception e) {
						System.out.println("There is not a variable found");
					}

					// if variable is found and not null then check its line number with the declaraion being made 
					// if the one being declared uses the rhs and if rhs expression variable is declared later then fail
					// as forward referencing not allowed
					if(variable != null) {
						if(variable.getLineNumber() > n.getBeginLine()) {
							throw new A2SemanticsException("Sorry, " + v.getInit() + " is declared after " + v.getId() + ". Forward referencing not allowed");
						}
					}
				}

				// this gets the type, like int, boolean, foo() etc
				symtab.Type typeOfVariableRhs = getTypeOfExpression(currentScope,rhs);

				// ensure that theses 2 types are existant
				//System.out.println((symtab.Type)symOfVariable);
				//System.out.println(typeOfVariableRhs);

				if (typeOfVariableRhs != null) {
					//System.out.println(typeOfVariableRhs.getName().toString());
					//System.out.println(((symtab.Type)symOfVariable).getName());

					// if the type lhs and rhs do not match
					if(!(typeOfVariableRhs.getName().toString().equals(((symtab.Type)symOfVariable).getName()))) {
						throw new A2SemanticsException("Sorry, " +  "cannot assign " + typeOfVariableRhs.getName().toString() + " to " +  ((symtab.Type)symOfVariable).getName() + " on line " + n.getBeginLine() + " as the types are different");
					}

				}

			}


			v.accept(this, arg);
			if (i.hasNext()) {
				printer.print(", ");
			}
		}
	}

	public void visit(TypeDeclarationStmt n, Object arg) {
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

	public void visit(ExpressionStmt n, Object arg) {
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

	// checks and ensures that the return type is correct in type and is allowed
	public void visit(ReturnStmt n, Object arg) {

		currentScope = n.getThisNodeScope();

		// only look if there is a method declaration going on
		if(currentMethodDeclaration != null) {

			// get symbol of current method declaration
			Symbol symOfMethod = currentScope.resolve(currentMethodDeclaration.getType().toString());

			// get the type of the method being declared
			symtab.Type methodType = (symtab.Type)symOfMethod;

			// if the type of the method ( what it is returning) is not null
			if(currentMethodDeclaration.getType() != null) {
				// if the return type is "void"
				if(currentMethodDeclaration.getType().toString().equals("void")) {
					// if there is a return statement then throw an error as void means nothing can be returned
					if(n != null) {
						throw new A2SemanticsException("Sorry, cannot return anything on line " + n.getBeginLine() + " as return type of method is void");
					}
				}

				//System.out.println(currentMethodDeclaration.getType());
			}else {
				throw new A2SemanticsException("Sorry, must atleast return void. Cannot write method without any return type");
			}

			// get return statement
			Expression r = n.getExpr(); 

			// if there is a return expression 
			if(r != null) {
				// this gets the type, like int, boolean, foo etc
				symtab.Type typeOfReturn = getTypeOfExpression(currentScope,r);

				// cannot return null if the method is not void as checked above
				if(typeOfReturn == null){
					throw new A2SemanticsException("Sorry, cannot return null from a return statement");
				}
				// if the return type of method is not that of the return statement then throw an error
				if(typeOfReturn!=methodType) {
					throw new A2SemanticsException("Sorry, the return type of your method " + currentMethodDeclaration.getName() + " at line " + currentMethodDeclaration.getBeginLine() + " does not match the return type " +typeOfReturn.getName() + " on line " + r.getBeginLine() );
				}

			}

		}



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
		if (n.getJavaDoc() != null) {
			n.getJavaDoc().accept(this, arg);
		}
		printer.print(";");
	}

	public void visit(InitializerDeclaration n, Object arg) {
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
