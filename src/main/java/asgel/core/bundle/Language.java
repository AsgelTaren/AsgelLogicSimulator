package asgel.core.bundle;

public enum Language {

	ENGLISH("EN"), FRENCH("FR");

	private String symbol;

	private Language(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}
}
