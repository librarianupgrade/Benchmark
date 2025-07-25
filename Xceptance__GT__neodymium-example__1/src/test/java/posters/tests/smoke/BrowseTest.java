/**
 * 
 */
package posters.tests.smoke;

import org.junit.Test;

import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.junit4.Tag;
import posters.flows.OpenHomePageFlow;
import posters.pageobjects.pages.browsing.CategoryPage;
import posters.pageobjects.pages.browsing.HomePage;
import posters.pageobjects.pages.browsing.ProductdetailPage;
import posters.tests.AbstractTest;

/**
 * @author pfotenhauer
 */
@Owner("Tim Brown")
@Severity(SeverityLevel.NORMAL)
@Tag("smoke")
public class BrowseTest extends AbstractTest {
	@Test
	public void testBrowsing() {
		// Goto homepage
		HomePage homePage = OpenHomePageFlow.flow();
		homePage.validate();

		// Goto category
		final String categoryName = homePage.topNav.getSubCategoryNameByPosition(1, 1);
		CategoryPage categoryPage = homePage.topNav.clickSubCategoryByPosition(1, 1);
		categoryPage.validate(categoryName);

		// Goto product page
		final String productName = categoryPage.getProductNameByPosition(1, 1);
		ProductdetailPage productPage = categoryPage.clickProductByPosition(1, 1);
		productPage.validate(productName);

		// Goto category
		final String categoryName2 = productPage.topNav.getSubCategoryNameByPosition(2, 2);
		categoryPage = productPage.topNav.clickSubCategoryByPosition(2, 2);
		categoryPage.validate(categoryName2);

		// Goto product page
		final String productName2 = categoryPage.getProductNameByPosition(2, 2);
		productPage = categoryPage.clickProductByPosition(2, 2);
		productPage.validate(productName2);

		// Goto category
		final String categoryName3 = productPage.topNav.getSubCategoryNameByPosition(2, 3);
		categoryPage = productPage.topNav.clickSubCategoryByPosition(2, 3);
		categoryPage.validate(categoryName3);

		// Goto product page
		final String productName3 = categoryPage.getProductNameByPosition(2, 3);
		productPage = categoryPage.clickProductByPosition(2, 3);
		productPage.validate(productName3);
	}
}
