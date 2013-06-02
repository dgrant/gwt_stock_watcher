package ca.davidgrant.stockwatcher.client;

public class StockPrice {
	private String symbol;
	private double change;
	private double price;

	public StockPrice() {
	}

	public StockPrice(String symbol, double price, double change) {
		this.symbol = symbol;
		this.price = price;
		this.change = change;
	}

	public String getSymbol() {
		return symbol;
	}

	public double getChange() {
		return change;
	}

	public double getPrice() {
		return price;
	}

	public double getChangePercent() {
		return 100.0 * this.change / this.price;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void setChange(double change) {
		this.change = change;
	}

	public void setPrice(double price) {
		this.price = price;
	}

}
