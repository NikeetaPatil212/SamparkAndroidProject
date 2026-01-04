package com.example.androidproject.utils;

import java.util.List;

public class NavMenuItem {

        public String title;
        public int icon;
        public boolean isExpandable;
        public boolean isExpanded;

        public NavMenuItem(String title, int icon) {
            this.title = title;
            this.icon = icon;
        }

        public NavMenuItem(String title, int icon, boolean isExpandable) {
            this.title = title;
            this.icon = icon;
            this.isExpandable = isExpandable;
        }
}
