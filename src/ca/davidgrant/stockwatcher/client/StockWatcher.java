package ca.davidgrant.stockwatcher.client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class StockWatcher implements EntryPoint {

	private static final int REFRESH_INTERVAL = 5000; // ms
	private VerticalPanel mainPanel = new VerticalPanel();
	private FlexTable stocksFlexTable = new FlexTable();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private TextBox newSymbolTextBox = new TextBox();
	private Button addStockButton = new Button("Add");
	private Label lastUpdatedLabel = new Label();
	private ArrayList<String> stocks = new ArrayList<String>();
	private Timer refreshTimer;

	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {
		// Create table for stock data.
		stocksFlexTable.setText(0, 0, "Stock");
		stocksFlexTable.setText(0, 1, "Price");
		stocksFlexTable.setText(0, 2, "Change");
		stocksFlexTable.setText(0, 3, "Remove");
		
		// Add styles to elements in the stock list table.
	    stocksFlexTable.setCellPadding(6);
		
		// Add styles to elements in the stock list table.
	    stocksFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
	    stocksFlexTable.addStyleName("watchList");
	    stocksFlexTable.getCellFormatter().addStyleName(0, 0, "watchListNameColumn");
	    stocksFlexTable.getCellFormatter().addStyleName(0, 1, "watchListNumericColumn");
	    stocksFlexTable.getCellFormatter().addStyleName(0, 2, "watchListNumericColumn");
	    stocksFlexTable.getCellFormatter().addStyleName(0, 3, "watchListRemoveColumn");

		// Assemble Add Stock panel.
		addPanel.add(newSymbolTextBox);
		addPanel.add(addStockButton);
		addPanel.addStyleName("addPanel");

		// Assemble Main panel.
		mainPanel.add(stocksFlexTable);
		mainPanel.add(addPanel);
		mainPanel.add(lastUpdatedLabel);

		// Associate the Main panel with the HTML host page.
		RootPanel.get("stockList").add(mainPanel);

		// Move cursor focus to the input box.
		newSymbolTextBox.setFocus(true);

		// ======================================
		// EVENT HANDLERS

		// Listen for mouse events on the Add button.
		addStockButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addStock();
			}
		});

		// Listen for keyboard events in the input box.
		newSymbolTextBox.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					addStock();
				}

			}
		});

		// ======================================
		// TIMERS
		refreshTimer = new Timer() {
			@Override
			public void run() {
				refreshWatchList();
			}
		};

		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
		refreshWatchList(); // run once

	}

	private void refreshWatchList() {
		if (stocks.isEmpty()) {
			return;
		}
		
		final double MAX_PRICE = 100.0; // $100.00
		final double MAX_PRICE_CHANGE = 0.02; // +/- 2%

		StockPrice[] prices = new StockPrice[stocks.size()];
		for (int i = 0; i < stocks.size(); i++) {
			double price = Random.nextDouble() * MAX_PRICE;
			double change = price * MAX_PRICE_CHANGE * (Random.nextDouble() * 2.0 - 1.0);

			prices[i] = new StockPrice(stocks.get(i), price, change);
		}

		updateTable(prices);

	}

	private void updateTable(StockPrice[] prices) {
		for (StockPrice price : prices) {
			updateTable(price);
		}

		// Display timestamp showing last refresh
		lastUpdatedLabel.setText("Last update: " + DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
		
	}

	private void updateTable(StockPrice price) {
		if (!stocks.contains(price.getSymbol())) {
			return;
		}

		int row = stocks.indexOf(price.getSymbol()) + 1;
		// Format the data in the Price and Change fields.
		String priceText = NumberFormat.getFormat("#,##0.00").format(price.getPrice());
		NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
		String changeText = changeFormat.format(price.getChange());
		String changePercentText = changeFormat.format(price.getChangePercent());

		// Populate the Price and Change fields with new data.
		stocksFlexTable.setText(row, 1, priceText);
		Label changeWidget = (Label)stocksFlexTable.getWidget(row, 2);
		changeWidget.setText(changeText + " (" + changePercentText + "%)");

		String changeStyleName = "noChange";
		if (price.getChangePercent() < 0.1f) {
			changeStyleName = "negativeChange";
		} else if (price.getChangePercent() > 0.1f) {
			changeStyleName = "positiveChange";
		}
		changeWidget.setStyleName(changeStyleName);

	}

	/**
	 * Add stock to FlexTable. Executed when the user clicks the addStockButton
	 * or presses enter in the newSymbolTextBox.
	 */

	private void addStock() {
		final String symbol = newSymbolTextBox.getText().toUpperCase().trim();
		newSymbolTextBox.setFocus(true);

		// Stock code must be between 1 and 10 chars that are numbers, letters,
		// or dots.
		if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
			Window.alert("'" + symbol + "' is not a valid symbol.");
			newSymbolTextBox.selectAll();
			return;
		}

		newSymbolTextBox.setText("");

		// Don't add the stock if it's already in the table.
		if (stocks.contains(symbol)) {
			return;
		}

		// Add the stock to the table.
		int row = stocksFlexTable.getRowCount();
		stocks.add(symbol);
		stocksFlexTable.setText(row, 0, symbol);
		stocksFlexTable.setWidget(row, 2, new Label());
		stocksFlexTable.getCellFormatter().addStyleName(row, 0, "watchListNameColumn");
		stocksFlexTable.getCellFormatter().addStyleName(row, 1, "watchListNumericColumn");
	    stocksFlexTable.getCellFormatter().addStyleName(row, 2, "watchListNumericColumn");
	    stocksFlexTable.getCellFormatter().addStyleName(row, 3, "watchListRemoveColumn");

		// Add a button to remove this stock from the table.
		Button removeStockButton = new Button("X");
		removeStockButton.addStyleDependentName("remove");
		removeStockButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int removedIndex = stocks.indexOf(symbol);
				stocks.remove(symbol);
				stocksFlexTable.removeRow(removedIndex + 1);
			}
		});
		stocksFlexTable.setWidget(row, 3, removeStockButton);

		refreshWatchList();
		refreshTimer.cancel();
		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
		

	}

}