package symtab;

import java.util.HashMap;

public class LocalScope extends BaseScope {
	
	private HashMap<String,Symbol> symbols = new HashMap<String,Symbol>();
	
	public LocalScope(Scope enclosingScope) {
		this.enclosingScope = enclosingScope; // this sets the enclosing scope of the parent base class.
	}
	
	public String getScopeName() {
		return "Local Scope";
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
