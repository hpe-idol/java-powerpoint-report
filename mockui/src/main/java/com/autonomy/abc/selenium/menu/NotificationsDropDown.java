package com.autonomy.abc.selenium.menu;

import com.autonomy.abc.selenium.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class NotificationsDropDown extends AppElement {

    public NotificationsDropDown(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    //TODO Even this may need to be abstracted - affix-element for Angular half
    //      Although it won't matter if the right Notifications Dropdown is given?
    public WebElement notificationNumber(final int index) {
        return findElement(By.cssSelector("li li:nth-child(" + (index * 2 - 1) + ") a div"));
    }
}
