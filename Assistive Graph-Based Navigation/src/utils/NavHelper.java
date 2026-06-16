package utils;

import javafx.scene.control.Button;

/**
 * Utility class for handling navigation button states.
 * Applies active/inactive styles using {@link UIStyles}.
 */
public class NavHelper {

	 /**
     * Sets the given button to active state.
     * @param btn the navigation button
     */
    public static void setActive(Button btn) {
        btn.setStyle(UIStyles.navActive());
    }

    /**
     * Sets the given button to inactive state.
     * @param btn the navigation button
     */
    public static void setInactive(Button btn) {
        btn.setStyle(UIStyles.navInactive());
    }
}