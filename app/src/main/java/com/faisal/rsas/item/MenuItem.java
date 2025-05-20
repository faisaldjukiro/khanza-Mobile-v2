package com.faisal.rsas.item;

public class MenuItem {
    private int iconResId;
    private String menuTitle;

    public MenuItem(int iconResId, String menuTitle) {
        this.iconResId = iconResId;
        this.menuTitle = menuTitle;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public void setMenuTitle(String menuTitle) {
        this.menuTitle = menuTitle;
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "iconResId=" + iconResId +
                ", menuTitle='" + menuTitle + '\'' +
                '}';
    }
}
