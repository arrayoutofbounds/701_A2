package symtab;

public interface Scope {
	public String getScopeName();
	public Scope getEnclosingScope();
	public void define(Symbol symbol);
	public Symbol resolve(String name);
	public Symbol resolveThisScopeOnly(String name);
}
