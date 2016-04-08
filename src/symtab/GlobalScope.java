package symtab;

import java.util.HashMap;

public class GlobalScope extends BaseScope {
	
	private HashMap<String,Symbol> symbols = new HashMap<String,Symbol>();
	
	public GlobalScope(){
		//TODO add all primitives
		define(new BuiltInTypeSymbol("int"));
		define(new BuiltInTypeSymbol("float"));
		define(new BuiltInTypeSymbol("long"));
		define(new BuiltInTypeSymbol("byte"));
		define(new BuiltInTypeSymbol("short"));
		define(new BuiltInTypeSymbol("double"));
		define(new BuiltInTypeSymbol("boolean"));
		define(new BuiltInTypeSymbol("char"));
		define(new ClassSymbol("void"));
		//TODO add String. This goes into the overloaded constructor where a enclosing scope is not given
		define(new ClassSymbol("String"));
	}
	
	public String getScopeName() {
		return "Global Scope";
	}

	public void define(Symbol symbol) {
		symbols.put(symbol.getName(), symbol);
	}

	public Symbol resolve(String name) {
		return symbols.get(name);
	}


	
}
