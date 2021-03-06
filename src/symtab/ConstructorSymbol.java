package symtab;

import java.util.HashMap;

public class ConstructorSymbol extends ScopedSymbol implements Type {

	private HashMap<String,Symbol> symbols = new HashMap<String,Symbol>();
	
	public ConstructorSymbol(String name, Type type) {
		super(name, type);
	}

	public ConstructorSymbol(String name, Type type,Scope enclosingScope) {
		super(name, type);
		this.enclosingScope = enclosingScope;
	}
	
	public String getScopeName() {
		return "Constructor Scope";
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
