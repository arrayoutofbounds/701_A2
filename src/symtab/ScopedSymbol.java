package symtab;

public class ScopedSymbol extends Symbol implements Scope {
	
	protected Scope enclosingScope = null;
	
	public ScopedSymbol(String name, Type type) {
		super(name, type);
	}

	@Override
	public String getScopeName() {
		return null;
	}

	@Override
	public Scope getEnclosingScope() {
		return this.enclosingScope;
	}

	@Override
	public void define(Symbol symbol) {
		
	}

	@Override
	public Symbol resolve(String name) {
		return null;
	}

	@Override
	public Symbol resolveThisScopeOnly(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
