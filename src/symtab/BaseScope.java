package symtab;

public class BaseScope implements Scope  {

	protected Scope enclosingScope = null;
	
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
