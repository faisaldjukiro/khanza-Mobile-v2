package com.faisal.rsas.item;

public class MenuItem {
    private int iconResId;
    private String menuTitle;

    // Constructor
    public MenuItem(int iconResId, String menuTitle) {
        this.iconResId = iconResId;
        this.menuTitle = menuTitle;
    }

    // Getter for iconResId
    public int getIconResId() {
        return iconResId;
    }

    // Getter for menuTitle
    public String getMenuTitle() {
        return menuTitle;
    }

    // Optional: Setter for iconResId if you want to change it later
    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    // Optional: Setter for menuTitle if you want to change it later
    public void setMenuTitle(String menuTitle) {
        this.menuTitle = menuTitle;
    }

    // Optional: Override toString method for easy debugging
    @Override
    public String toString() {
        return "MenuItem{" +
                "iconResId=" + iconResId +
                ", menuTitle='" + menuTitle + '\'' +
                '}';
    }
}
