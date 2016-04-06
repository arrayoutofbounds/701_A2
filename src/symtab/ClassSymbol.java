package symtab;

import java.util.HashMap;

public class ClassSymbol extends ScopedSymbol implements Type {

	private HashMap<String,Symbol> symbols = new HashMap<String,Symbol>();
	
	private Scope inheritedScope = null;
	
	public ClassSymbol(String name) {
		super(name, null);
	}
	
	public ClassSymbol(String name,Scope enclosingScope) {
		super(name, null);
		this.enclosingScope = enclosingScope;
	}

	public ClassSymbol(String name,Scope enclosingScope,Scope inheritedScope) {
		super(name, null);
		this.enclosingScope = enclosingScope;
		this.inheritedScope = inheritedScope;
	}
	
	public Scope getInheritedScope() {
		return this.inheritedScope;
	}

	public String getScopeName() {
		return "Class Scope";
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

}
