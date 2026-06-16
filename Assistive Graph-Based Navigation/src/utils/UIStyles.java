package utils;

/**
 * Reusable UI styles to avoid duplication.
 */
public class UIStyles {
	
	
    // =========================
    // CARD / CONTAINERS
    // =========================
	 /**
     * @return CSS style string for standard card containers.
     */
    public static String card() {
        return "-fx-background-color:" + CustomColours.BG_CARD.getValue() + ";" +
               "-fx-border-color:" + CustomColours.BORDER.getValue() + ";" +
               "-fx-background-radius:10;" +
               "-fx-border-radius:10;";
    }

    
    /** Smaller card (used in image slots) */
    public static String smallCard() {
        return "-fx-background-color:" + CustomColours.BG_CARD.getValue() + ";" +
               "-fx-background-radius:8;" +
               "-fx-border-color:" + CustomColours.BORDER.getValue() + ";" +
               "-fx-border-radius:8;" +
               "-fx-border-width:1;";
    }

    /** Chip style (status boxes) */
    public static String chip() {
        return "-fx-background-color:" + CustomColours.BG_DARK.getValue() + ";" +
               "-fx-border-color:" + CustomColours.BORDER.getValue() + ";" +
               "-fx-border-radius:8;" +
               "-fx-background-radius:8;";
    }

    
    
    
    // =========================
    // NAVIGATION
    // =========================
    /**
     * Side bar container styling.
     * @return CSS style string for side bar container
     */
    public static String sidebar() {
        return "-fx-background-color:" + CustomColours.BG_SIDEBAR.getValue() + ";" +
               "-fx-border-color:" + CustomColours.BORDER.getValue() + ";" +
               "-fx-border-width:0 1 0 0;";
    }

    /**
     * Active navigation button (clicked)
     */
    public static String navActive() {
        return "-fx-background-color:" + CustomColours.BLUE_NAV.getValue() + ";" +
               "-fx-text-fill:" + CustomColours.TEAL.getValue() + ";" +
			   "-fx-cursor:hand;" +
               "-fx-border-width:0 0 0 3;" +
               "-fx-border-color:" + CustomColours.TEAL.getValue() + ";";
    }

    /**
     * Inactive navigation button (default state)
     */
    public static String navInactive() {
        return "-fx-background-color:transparent;" +
               "-fx-text-fill:" + CustomColours.WHITE.getValue() + ";"+
               "-fx-cursor:hand;" +
               "-fx-border-width:0;";
    }
    

    /**
     * Hover style for navigation buttons
     */
    public static String navHover() {
        return "-fx-background-color:#1e2d4a;" +
               "-fx-text-fill:" + CustomColours.WHITE.getValue() + ";" +
               "-fx-cursor:hand;" +
               "-fx-border-width:0;";
    }
    
    
    
     // =========================
     // INPUT COMPONENTS
     // =========================
	  /**
     * Style for database ComboBox.
     */
    public static String comboBox() {
        return "-fx-background-color:" + CustomColours.BG_CARD.getValue() + ";" +
               "-fx-text-fill:" + CustomColours.WHITE.getValue() + ";" +
               "-fx-border-color:" + CustomColours.BORDER.getValue() + ";" +
               "-fx-font-family:'Courier New';" +
               "-fx-font-size:11;";
    }
    
    
    // =========================
    // DIVIDERS
    // =========================

    /** Horizontal line divider */
    public static String divider() {
        return "-fx-background-color:" + CustomColours.BORDER.getValue() + ";";
    }
    

    /**
     * Image container (used in classification/similarity)
     */
    public static String imageHolder() {
        return  "-fx-background-color:" + CustomColours.BG_CARD.getValue() + ";" +
                "-fx-background-radius:10;" +
                "-fx-border-color:" + CustomColours.BORDER.getValue() + ";" +
                "-fx-border-radius:10;-fx-border-width:1;";
    }
    
    
    // =========================
    // SPECIAL COMPONENTS
    // =========================
    /**
     * Small pill/tag style (used on home screen)
     */
    public static String pill() {
        return "-fx-background-color:" + CustomColours.BG_CARD.getValue() + ";" +
               "-fx-border-color:" + CustomColours.TEAL.getValue() + "55;" +
               "-fx-border-radius:20;" +
               "-fx-background-radius:20;";
    }
    
    /**
     * Badge style (home screen top label).
     */
    public static String badge() {
        return "-fx-background-color:" + CustomColours.BG_CARD.getValue() + "88;" +
               "-fx-border-color:" + CustomColours.TEAL.getValue() + "66;" +
               "-fx-border-radius:20;" +
               "-fx-background-radius:20;";
    }

    /**
     * Log area styling
     */
    public static String logArea() {
        return "-fx-control-inner-background:#0d0d0d;" +
               "-fx-text-fill:" + CustomColours.TEAL.getValue() + ";" +
               "-fx-font-family:'Courier New';" +
               "-fx-font-size:11;" +
               "-fx-border-color:" + CustomColours.BORDER.getValue() + ";" +
               "-fx-border-width:1 0 0 0;";
    }

    
    

    // =========================
    // GENERAL BACKGROUNDS
    // =========================

    /** Main dark background used across screens */
    public static String mainBackground() {
        return "-fx-background-color:" + CustomColours.BG_DARK.getValue() + ";";
    }
    
    /**
     * ScrollPane styling
     */
    public static String scrollPane() {
        return "-fx-background:" + CustomColours.BG_DARK.getValue() + ";" +
               "-fx-background-color:" + CustomColours.BG_DARK.getValue() + ";";
    }

    /**
     * TableView styling for dark theme
     */
    public static String tableView() {
        return "-fx-background-color: transparent;" +
               "-fx-control-inner-background: #1a1a1a;" +
               "-fx-table-cell-border-color: #333333;";
    }
    }