package utils;

/**
 * Centralized color palette for the entire UI.
 * Provides hex values and CSS strings for consistent styling.
 */
public enum CustomColours{
	
    //Backgrounds.
    BG_DARK("#0d0d0d"),
    BG_SIDEBAR("#111827"),
    BG_CARD("#1a2238"),

    //Primary colors.
    BLUE_NAV("#1e3a5f"),
    BLUE_LIGHT("#2563eb"),
    TEAL("#00bcd4"),

    //Text.
    WHITE("#f0f4ff"),
    GREY("#8892a4"),

    //Borders.
    BORDER("#1e3a5f"),
    
     //Buttons
    BUTTON_BLUE("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;"),
    BUTTON_GREEN("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10;");
	
	private final String value;

	private CustomColours(String value) {
	     this.value = value;
	    }

	
	 /**
     * @return the hex or CSS string value of the color.
     */
	 public String getValue() {
	    return value;
	    }
    
}