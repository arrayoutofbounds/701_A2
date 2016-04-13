package symtab;

public class VariableSymbol extends Symbol {
	
	private int lineNumber;

	public VariableSymbol(String name, Type type) {
		super(name, type);
	}
	
	public void setLineNumber(int line) {
		this.lineNumber = line;
	}
	
	public int getLineNumber() {
		return this.lineNumber;
	}

}
