package com.gengo.automation.tests.CoreTest;

import com.gengo.automation.global.AutomationBase;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.awt.*;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

/**
 * @case Right Panel Information - (Character to Character, Credits Payment, Business Tier, Without Glossary, With Validation)
 * @reference https://gengo.testrail.com/index.php?/cases/view/40345
 */
public class C40345_8_RightPanelInformation extends AutomationBase {

    private String parentWindow, orderNo, excerpt;
    private String[] unitCount,itemToTranslate, excerpts, translatedItem, jobNo = new String[3],
            highlightWarning = {"1"}, // texts that trigger warning validation
            highlightTag = {"{1}", "{/1}"}, // tags that are on the source text
            highlightTag2 = {"{1}", "{/1}"}, // tags that are on the source text
            highlightComment = {"[[[ comment here ]]]"}, // comments on the source text
            highlightComment2 = {"[[[ like this one test record inside ]]]"}, // comments on the source text
            resolveCaution = {"2"}; // the remaining number validation warning not resolved

    @BeforeClass
    public void initFields() throws IOException{
        excerpt = var.getExcerptNonEnglish(18);
        excerpts = new String[] {
                var.getExcerptNonEnglish(18),
                var.getExcerptNonEnglish(26),
                var.getExcerptNonEnglish(27)
        };
        itemToTranslate = new String[] {
                var.getItemToTranslate(24),
                var.getItemToTranslate(48),
                var.getItemToTranslate(49)
        };
        unitCount = new String[] {
                var.getUnitCountSource(24),
                var.getUnitCountSource(48),
                var.getUnitCountSource(49)
        };
        translatedItem = new String[] {
                var.getTranslatedItem(24),
                var.getTranslatedItem(48),
                var.getTranslatedItem(49)
        };
    }

    public C40345_8_RightPanelInformation() throws IOException {}

    @AfterClass
    public void afterRun() {
        Reporter.log("Done running '" + this.getClass().getSimpleName() + "'", true);
    }

    /**
     * placeAnOrder --- a method for checking the logging in of a customer account,
     * and placing an order with glossary chosen
     * */
    @Test
    public void placeAnOrder() {
        pluginPage.passThisPage();

        // Check if the prominent page elements can be seen on the Home Page
        assertTrue(homePage.checkHomePage());
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementNotFoundErrMsg());

        // Logging in a customer account
        homePage.clickSignInButton();
        loginPage.loginAccount(var.getCustomer(27), var.getDefaultPassword());

        // Check if the prominent page elements can be seen on the Customer Dashboard Page
        assertTrue(page.getCurrentUrl().equals(customerDashboardPage.CUSTOMER_DASHBOARD_URL),
                var.getWrongUrlErrMsg());
        assertTrue(customerDashboardPage.checkCustomerDashboard());

        // Click the icon for placing an order
        global.clickOrderTranslationIcon();

        // Place the text to be translated on the order form and check for the word/character count
        customerOrderFormPage.inputItemToTranslate(itemToTranslate, unitCount, itemToTranslate.length, false);

        // Check if source language is auto detected by the system
        assertTrue(page.getCurrentUrl().equals(customerOrderLanguagesPage.ORDERLANGUAGES_URL),
                var.getWrongUrlErrMsg());
        assertTrue(customerOrderLanguagesPage.isSourceAutoDetected(var.getJapaneseFrom()),
                var.getTextNotEqualErrMsg());

        // Set the target language to English
        customerOrderLanguagesPage.choooseLanguage(var.getChineseSimplifiedTo());
        customerOrderLanguagesPage.clickNextOptions();

        // Click the Business Tier Radio button
        customerCheckoutPage.businessTier(true);

        // Choose glossary file to be used
        customerCheckoutPage.addGlossary("ja_to_zh");

        customerCheckoutPage.clickOneTranslator();

        // Check the 'View Full Quote' page and the generated pdf File
        customerCheckoutPage.clickViewFullQuote();
        parentWindow = switcher.getWindowHandle();
        switcher.switchToPopUp();
        wait.impWait(3);
        assertTrue(page.getCurrentUrl().equals(customerOrderQuotePage.CUSTOMERORDERQUOTE_URL),
                var.getWrongUrlErrMsg());
        customerOrderQuotePage.typeAdress();
        assertTrue(customerOrderQuotePage.getAddressEmbedded().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());
        customerOrderQuotePage.downloadQuote();
        switcher.switchToParentWindow(parentWindow);

        // Place payment with Credits
        customerCheckoutPage.payWithCredits();

        // Retrieve the order number
        orderNo = customerOrderCompletePage.orderNumber();

        // Return to dashboard page
        customerOrderCompletePage.clickGoToDashboard();

        // Customer sign out
        global.nonAdminSignOut();

        // Check if the redirected page contains the prominent Home Page elements
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());
        assertTrue(homePage.checkHomePage());
    }

    /**
     * translatorFindJob --- a method wherein a translator signs in and looks for the recently
     * created job and opens the workbench
     * */
    @Test(dependsOnMethods = "placeAnOrder")
    public void translatorFindJob() {
        pluginPage.passThisPage();
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementNotFoundErrMsg());

        // Log in a translator account
        homePage.clickSignInButton();
        loginPage.loginAccount(var.getTranslator(26), var.getDefaultPassword());

        // Check if the page redirected to contains the elements that should be seen in a Translator Dashboard
        assertTrue(translatorDashboardPage.checkTranslatorDashboard());

        // Navigate to the Jobs Page and look for the recently created job
        translatorDashboardPage.clickJobsTab();
        translatorJobsPage.findJob(excerpt);
    }

    /**
     * translatorOpenJob --- a method which checks the layout of the workbench and checks the glossary section,
     * the highlighted texts and warnings
     * */
    @Test(dependsOnMethods = "translatorFindJob")
    public void translatorOpenJob() {
        // Close the workbench modal
        workbenchPage.closeWorkbenchModal();

        // Check if the tags are highlighted on the source text
        assertTrue(workbenchPage.checkHighlightTag(highlightTag));

        // Check if the Numbers are highlighted in the source text
        assertTrue(workbenchPage.checkHighlightWarning(highlightWarning));

        // Check if the triple bracket comments are highlighted
        assertTrue(workbenchPage.checkHighlightComment(highlightComment));
    }

    /**
     * translatorPickUpJob --- this method checks for the highlighted texts, the auto-appending function when
     * clicking highlighted matches, tags, and comments; this method also checks if the glossary section and the
     * issues section tracks the changes on the translation area
     * */
    @Test(dependsOnMethods = "translatorOpenJob")
    public void translatorPickUpJob() throws AWTException {
        // Translator picks up the job
        workbenchPage.startTranslateJob();
        wait.impWait(10);

        // Toggle the Issues Section
        workbenchPage.openErrorIssuesSection();

        // Add content to translation area to toggle highlighting of errors/matches
        workbenchPage.addTextSpecificArea(" ", excerpts[0]);

        // Check if the corresponding texts are highlighted
        assertTrue(workbenchPage.checkHighlightTagError(highlightTag));
        assertTrue(workbenchPage.checkHighlightWarning(highlightWarning));
        assertTrue(workbenchPage.checkHighlightCommentError(highlightComment));

        // Check if the issues section shows the validation errors
        assertTrue(workbenchPage.checkTagsIssue(highlightTag));
        assertTrue(workbenchPage.checkCommentIssue(highlightComment));
        assertTrue(workbenchPage.checkCautionIssue(highlightWarning));

        // Click the triple bracket texts to add them to the translation area
        workbenchPage.addCommentsToText(highlightComment);

        // Check if the issues section shows the validation errors
        workbenchPage.checkTagsIssue(highlightTag);
        workbenchPage.checkCautionIssue(highlightWarning);

        // Click the tags to add them to the translation text area
        workbenchPage.addTagsToText(highlightTag);

        // Add the remaining numbers to the translation area
        workbenchPage.addRemainingCautionIssues(resolveCaution);

        workbenchPage.moveToNextJobKey();

        // Add content to translation area to toggle highlighting of errors/matches
        workbenchPage.addTextSpecificArea(" ", excerpts[1]);

        // Toggle the Issues Section
        workbenchPage.openWarningErrorIssuesSection();   // Check if the corresponding texts are highlighted
        assertTrue(workbenchPage.checkHighlightTagError(highlightTag2));
        assertTrue(workbenchPage.checkHighlightCommentError(highlightComment2));

        // Check if the issues section shows the validation errors
        assertTrue(workbenchPage.checkTagsIssue(highlightTag2));
        assertTrue(workbenchPage.checkCommentIssue(highlightComment2));

        // Click the triple bracket texts to add them to the translation area
        workbenchPage.addCommentsToText(highlightComment2, excerpts[1]);

        // Check if the issues section shows the validation errors
        workbenchPage.checkTagsIssue(highlightTag2);

        // Click the tags to add them to the translation text area
        workbenchPage.addTagsToText(highlightTag2, excerpts[1]);

        workbenchPage.moveToNextJobKey();

        // Add content to translation area to toggle highlighting of errors/matches
        workbenchPage.addTextSpecificArea(" ", excerpts[2]);

        // Toggle the Issues Section
        workbenchPage.openWarningErrorIssuesSection();

        page.refresh();
    }

    /**
     * translatorAddTranslatedText --- this method adds the correct translation
     * text on the workbench and the job is submitted
     * */
    @Test(dependsOnMethods = "translatorPickUpJob")
    public void translatorAddTranslatedText() throws AWTException {
        // Text to be translated is added
        workbenchPage.addMultipleTranslatedText(translatedItem, excerpts, translatedItem.length);

        wait.impWait(30);
        page.refresh();

        // Translator submits job
        workbenchPage.submitJob();
        workbenchPage.submitModalOk();

        assertTrue(translatorDashboardPage.checkTranslatorDashboard());

        // Translator account is logged out
        global.nonAdminSignOut();
        assertTrue(homePage.checkHomePage());
    }

    /**
     * customerApprove --- this method has the customer approve the job
     * */
    @Test(dependsOnMethods = "translatorAddTranslatedText")
    public void customerApprove() {
        pluginPage.passThisPage();
        assertTrue(homePage.getSignIn().isDisplayed(),
                var.getElementNotFoundErrMsg());
        homePage.clickSignInButton();
        loginPage.loginAccount(var.getCustomer(27), var.getDefaultPassword());
        //global.selectCustomer();
        assertTrue(customerDashboardPage.checkCustomerDashboard());
        assertTrue(page.getCurrentUrl().equals(customerDashboardPage.CUSTOMER_DASHBOARD_URL),
                var.getWrongUrlErrMsg());
        // Loops through the jobs and approves them all
        for(int ctr = 0; ctr < excerpts.length; ctr++) {
            globalPage.goToOrdersPage();
            customerOrdersPage.clickReviewableOption();
            customerOrdersPage.findOrder(excerpts[ctr]);

            // Retrieve the job Number of the order
            jobNo[ctr] =customerOrderDetailsPage.getJobNumberReviewableJob();

            // Job is approved
            customerOrderDetailsPage.approveJob();
        }
        global.nonAdminSignOut();
        assertTrue(homePage.checkHomePage());
    }

    /**
     * checkEmail --- this method logs in the on the email of the customer
     * and checks if the emails for order received, job review, flag, and
     * approval are visible
     * */
    @Test(dependsOnMethods = "customerApprove")
    public void checkEmail() {
        page.launchUrl(var.getGmailUrl());
        assertTrue(gmailSignInEmailPage.getTxtBoxEmail().isDisplayed(),
                var.getElementIsNotDisplayedErrMsg());

        // Log in on Gmail account
        gmailSignInEmailPage.inputEmail(var.getGmailEmail());
        gmailSignInPasswordPage.inputPasswordAndSubmit(var.getGmailPassword());

        // Check if the emails for Order Received, Review, and Approval are received
        assertTrue(gmailInboxPage.checkOrderReceived(orderNo));
        for(String jobNumber : jobNo) {
            assertTrue(gmailInboxPage.checkJobForReview(jobNumber));
            assertTrue(gmailInboxPage.checkJobApproved(jobNumber));
        }
    }
}
