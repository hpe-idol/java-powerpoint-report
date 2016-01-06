package com.autonomy.abc.promotions;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.Editable;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.element.PromotionsDetailTriggerForm;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.search.DocumentViewer;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.promotions.HSOPromotionService;
import com.autonomy.abc.selenium.promotions.StaticPromotion;
import com.autonomy.abc.selenium.util.Errors;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static com.autonomy.abc.matchers.ElementMatchers.hasTextThat;
import static com.autonomy.abc.matchers.PromotionsMatchers.promotionsList;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;

public class StaticPromotionsITCase extends HostedTestBase {

    private HSOPromotionsPage promotionsPage;
    private PromotionsDetailPage promotionsDetailPage;
    private SearchPage searchPage;
    private HSOPromotionService promotionService;
    private final String title = "title";
    private final String content = "content";
    private final String trigger = "dog";
    private final StaticPromotion promotion = new StaticPromotion(title, content, trigger);
    private PromotionsDetailTriggerForm triggerForm;

    public StaticPromotionsITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        assumeThat(type, is(ApplicationType.HOSTED));
    }

    public void goToDetails() {
        promotionsDetailPage = promotionService.goToDetails(trigger);
    }

    @Before
    public void setUp() {
        promotionService = getApplication().createPromotionService(getElementFactory());
        body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
        promotionsPage = getElementFactory().getPromotionsPage();
        promotionService.deleteAll();
        searchPage = promotionService.setUpStaticPromotion(promotion);
    }

    @Test
    public void testDeleteStaticPromotion() {
        promotionsPage = promotionService.goToPromotions();
        promotionsPage.promotionDeleteButton(trigger).click();
        final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
        verifyThat(deleteModal, containsText(trigger));
        WebElement cancelButton = deleteModal.findElement(By.className("btn-default"));
        verifyThat(cancelButton, hasTextThat(equalToIgnoringCase("Close")));
        cancelButton.click();

        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.stalenessOf(deleteModal));
        verifyThat("bottom right close button works", promotionsPage, promotionsList(hasItem(containsText(trigger))));

        promotionsPage.promotionDeleteButton(trigger).click();
        ModalView modalView2 = ModalView.getVisibleModalView(getDriver());
        modalView2.close();
        verifyThat("top right close button works", promotionsPage, promotionsList(hasItem(containsText(trigger))));

        promotionsPage.promotionDeleteButton(trigger).click();
        final ModalView thirdDeleteModal = ModalView.getVisibleModalView(getDriver());
        final WebElement deleteButton = thirdDeleteModal.findElement(By.cssSelector(".btn-danger"));
        verifyThat(deleteButton, hasTextThat(equalToIgnoringCase("Delete")));
        deleteButton.click();
        new WebDriverWait(getDriver(), 20).until(GritterNotice.notificationContaining(promotion.getDeleteNotification()));
        verifyThat(promotionsPage, promotionsList(not(hasItem(containsText(trigger)))));
    }

    @Test
    public void testEditStaticPromotion() {
        goToDetails();

        Editable editTitle = promotionsDetailPage.staticPromotedDocumentTitle();
        final Editable editContent = promotionsDetailPage.staticPromotedDocumentContent();
        verifyThat(editTitle.getValue(), is(title));
        verifyThat(editContent.getValue(), is(content));

        final String secondTitle = "SOMETHING ELSE";
        editTitle.setValueAndWait(secondTitle);
        verifyThat(editTitle.getValue(), is(secondTitle));
        verifyThat(editContent.getValue(), is(content));
        final String secondContent = "apple";
        editContent.setValueAndWait(secondContent);
        verifyThat(editContent.getValue(), is(secondContent));

        getDriver().navigate().refresh();
        promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
        verifyThat("new value stays after page refresh", promotionsDetailPage.staticPromotedDocumentContent().getValue(), is(secondContent));
    }

    @Test
    public void testStaticPromotionNotifications() {
        verifyNotification("create", promotion.getCreateNotification());

        promotionsDetailPage = promotionService.goToDetails(promotion);
        promotionsDetailPage.staticPromotedDocumentTitle().setValueAndWait("different");
        verifyNotification("edit", promotion.getEditNotification());

        promotionService.delete(promotion);
        verifyNotification("delete", promotion.getDeleteNotification());
    }

    private void verifyNotification(String notificationType, String notificationText) {
        WebElement notification = null;
        try {
            notification = new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationAppears());
        } catch (Exception e) {
            e.printStackTrace();
        }
        verifyThat(notificationType + " notification appeared", notification, not(nullValue()));
        verifyThat(notification, containsText(notificationText));
        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.stalenessOf(notification));
    }

    // TODO: this same test should apply for promotions, create promotions, keywords and create keywords?
    @Test
    public void testInvalidTriggers() {
        goToDetails();
        final String[] duplicateTriggers = {
                "dog",
                " dog",
                "dog ",
                " dog  ",
                "\"dog\""
        };
        final String[] quoteTriggers = {
                "\"bad",
                "bad\"",
                "b\"ad",
                "\"trigger with\" 3 quo\"tes"
        };
        final String[] commaTriggers = {
                "comma,",
                ",comma",
                "com,ma",
                ",,,,,,"
        };
        final String[] caseTriggers = {
                "Dog",
                "doG",
                "DOG"
        };

        triggerForm = promotionsDetailPage.getTriggerForm();
        assertThat(triggerForm.getNumberOfTriggers(), is(1));

        checkBadTriggers(duplicateTriggers, Errors.Term.DUPLICATE_EXISTING);
        checkBadTriggers(quoteTriggers, Errors.Term.QUOTES);
        checkBadTriggers(commaTriggers, Errors.Term.COMMAS);
        checkBadTriggers(caseTriggers, Errors.Term.CASE);

        triggerForm.typeTriggerWithoutSubmit("a");
        verifyThat("error message is cleared", triggerForm.getTriggerError(), isEmptyOrNullString());
        verifyThat(triggerForm.addButton(), not(disabled()));

        triggerForm.typeTriggerWithoutSubmit("    ");
        verifyThat("cannot add '     '", triggerForm.addButton(), disabled());
        triggerForm.typeTriggerWithoutSubmit("\t");
        verifyThat("cannot add '\\t'", triggerForm.addButton(), disabled());
        triggerForm.addTrigger("\"valid trigger\"");
        verifyThat("can add valid trigger", triggerForm.getNumberOfTriggers(), is(2));
    }

    private void checkBadTriggers(String[] triggers, String errorSubstring) {
        for (String trigger : triggers) {
            triggerForm.addTrigger(trigger);
            verifyThat("trigger '" + trigger + "' not added", triggerForm.getNumberOfTriggers(), is(1));
            verifyThat(triggerForm.getTriggerError(), containsString(errorSubstring));
            verifyThat(triggerForm.addButton(), disabled());
        }
    }

    @Test
    public void testPromotionViewable() {
        final String handle = getDriver().getWindowHandle();
        searchPage.getPromotedResult(1).click();
        DocumentViewer documentViewer = DocumentViewer.make(getDriver());
        verifyThat("document has a reference", documentViewer.getField("Reference"), not(isEmptyOrNullString()));

        getDriver().switchTo().frame(getDriver().findElement(By.tagName("iframe")));
        // these fail on Chrome - seems to be an issue with ChromeDriver
        verifyThat(getDriver().findElement(By.cssSelector("h1")), containsText(title));
        verifyThat(getDriver().findElement(By.cssSelector("p")), containsText(content));
        getDriver().switchTo().window(handle);
        documentViewer.close();
    }

    @Test
    public void testPromotionFilter() {
        goToDetails();
        final String newTitle = "aaa";
        final String newTrigger = "alternative";

        triggerForm = promotionsDetailPage.getTriggerForm();

        promotionsDetailPage.promotionTitle().setValueAndWait(newTitle);
        triggerForm.addTrigger(newTrigger);
        triggerForm.removeTrigger(trigger);
        verifyThat(triggerForm.getNumberOfTriggers(), is(1));
        promotionsPage = promotionService.goToPromotions();

        promotionsPage.selectPromotionsCategoryFilter("Spotlight");
        verifyThat(promotionsPage.getPromotionTitles(), empty());
        promotionsPage.selectPromotionsCategoryFilter("Static Promotion");
        verifyThat(promotionsPage.getPromotionTitles(), not(empty()));

        promotionsPage.promotionsSearchFilter().sendKeys(newTrigger);
        verifyThat(promotionsPage.getPromotionTitles(), not(empty()));

        promotionsPage.clearPromotionsSearchFilter();
        promotionsPage.selectPromotionsCategoryFilter("All Types");
        promotionsPage.promotionsSearchFilter().sendKeys(trigger);
        verifyThat(promotionsPage.getPromotionTitles(), empty());
    }
}
