/**
 * 
 */
package posters.pageobjects.components;

import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.not;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.SelenideElement;
import com.xceptance.neodymium.util.Context;

import cucumber.api.java.en.Given;
import io.qameta.allure.Step;
import posters.pageobjects.pages.user.AccountOverviewPage;
import posters.pageobjects.pages.user.LoginPage;
import posters.pageobjects.pages.user.RegisterPage;

/**
 * @author pfotenhauer
 */
public class UserMenu extends AbstractComponent {

	private final SelenideElement userMenu = $("#userMenu");

	private final SelenideElement showUserMenu = $("#showUserMenu");

	public void isComponentAvailable() {
		showUserMenu.should(exist);
	}

	@Step("open user menu")
	public void openUserMenu() {
		// Click the mini cart icon
		showUserMenu.scrollTo().click();
		// Wait for mini cart to appear
		// Wait for the mini cart to show
		userMenu.waitUntil(visible, Context.get().configuration.timeout());
	}

	@Step("close user menu")
	public void closeUserMenu() {
		// Click the mini cart icon again
		showUserMenu.scrollTo().click();
		// Move the mouse out of the area
		$("#brand").hover();
		// Wait for mini cart to disappear
		// Wait for the mini cart to disappear
		userMenu.waitUntil(not(visible), Context.get().configuration.timeout());
	}

	@Step("open login page from user menu")
	public LoginPage openLogin() {
		openUserMenu();
		userMenu.find(".goToLogin").scrollTo().click();
		return new LoginPage();
	}

	@Step("open account page from user menu")
	public AccountOverviewPage openAccountOverview() {
		openUserMenu();
		userMenu.find(".goToAccountOverview").scrollTo().click();
		return new AccountOverviewPage();
	}

	@Step("open register page from user menu")
	public RegisterPage openRegister() {
		openUserMenu();
		userMenu.find(".goToRegistration").scrollTo().click();
		return new RegisterPage();
	}

	/**
	 * @param firstName
	 */
	@Step("validate that \"{firstName}\" is logged in")
	public void validateLoggedInName(String firstName) {
		// Click on the mini user menu symbol
		openUserMenu();
		// Asserts the Menu shows your first name.
		userMenu.find(".firstName").shouldHave(exactText(firstName));
		closeUserMenu();
		// Makes sure the mini menu element has the "logged" class active instead of the "not-logged" class.
		showUserMenu.find("span.glyphicon").shouldHave(cssClass("logged")).shouldHave(exactText(""));
	}

	@Given("^not logged in$")
	@Step("validate that nobody is looged in")
	public void validateNotLoggedIn() {
		userMenu.find(".goToLogin").should(exist);
	}

	@Step("validate that somebody is looged in")
	public boolean isLoggedIn() {
		return userMenu.find(".goToAccountOverview").exists();
	}
}
