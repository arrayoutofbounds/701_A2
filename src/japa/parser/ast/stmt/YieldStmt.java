package japa.parser.ast.stmt;

import japa.parser.ast.visitor.GenericVisitor;
import japa.parser.ast.visitor.VoidVisitor;

public class YieldStmt extends Statement {
	 private final String id;

	    public YieldStmt(int line, int column, String id) {
	        super(line, column);
	        this.id = id;
	    }

	    public String getId() {
	        return id;
	    }

	    @Override
	    public <A> void accept(VoidVisitor<A> v, A arg) {
	        v.visit(this, arg);
	    }

	    @Override
	    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
	        return v.visit(this, arg);
	    }

}
