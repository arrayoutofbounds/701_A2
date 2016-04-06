package symtab;

import java.util.HashMap;

public class MethodSymbol extends ScopedSymbol {

	private HashMap<String,Symbol> symbols = new HashMap<String,Symbol>();
	
	public MethodSymbol(String name, Type type) {
		super(name, type);
	}

	public MethodSymbol(String name, Type type,Scope enclosingScope) {
		super(name, type);
		this.enclosingScope = enclosingScope;
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


}
