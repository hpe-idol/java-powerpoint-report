package com.autonomy.abc.selenium.menubar;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class TopNavBar extends TabBar {

	public TopNavBar(final WebDriver driver) {
		super(driver.findElement(By.cssSelector(".abc-top-navbar")), driver);
	}

	@Override
	public TopNavBarTab getTab(final String id) {
		return new TopNavBarTab(this, id);
	}

	@Override
	public void switchPage(final String tabId) {
		findElement(By.cssSelector(".fa-cog")).click();
		super.switchPage(tabId);
	}

	@Override
	public TopNavBarTab getSelectedTab() {
		final List<WebElement> activeTabs = $el().findElements(By.cssSelector("li.active"));

		if (activeTabs.size() != 1) {
			throw new IllegalStateException("Number of active tabs != 1");
		}

		return new TopNavBarTab(activeTabs.get(0), getDriver());
	}

}