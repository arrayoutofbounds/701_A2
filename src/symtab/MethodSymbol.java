package symtab;

import java.util.HashMap;
import java.util.List;

import japa.parser.ast.body.Parameter;
import japa.parser.ast.stmt.BlockStmt;

public class MethodSymbol extends ScopedSymbol {

	private HashMap<String,Symbol> symbols = new HashMap<String,Symbol>();
	private List<Parameter> params;
	private BlockStmt statement;
	
	
	public MethodSymbol(String name, Type type) {
		super(name, type);
	}

	public MethodSymbol(String name, Type type,Scope enclosingScope) {
		super(name, type);
		this.enclosingScope = enclosingScope;
	}
	
	public MethodSymbol(String name, Type type,Scope enclosingScope,List<Parameter> params,BlockStmt statement) {
		super(name, type);
		this.enclosingScope = enclosingScope;
		this.params = params;
		this.statement = statement;
	}
	
	public List<Parameter> getParams() {
		return this.params;
	}
	
	public BlockStmt getBody() {
		return this.statement;
	}
	
	public String getScopeName() {
		return "Method Scope";
	}

	public void define(Symbol symbol) {
		symbols.put(symbol.getName(), symbol);
	}

	public Symbol resolve(String name) {
		Symbol s =  symbols.get(name);
		if (s != null) {
			return s;
		}
		if(this.enclosingScope != null) {
			return enclosingScope.resolve(name);
		}
		return null;
	}
	
	public Symbol resolveThisScopeOnly(String name) {
		return symbols.get(name);
	}


}
