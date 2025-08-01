/**
 * 
 */
package posters.cucumber.dataHelper;

import cucumber.api.java.en.Given;
import posters.dataobjects.User;

/**
 * @author pfotenhauer
 */
public class DataSetup {

	private GlobalStorage storage;

	public DataSetup(GlobalStorage storage) {
		// The storage is passed via dependency injection
		this.storage = storage;
	}

	@Given("^user setup: \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\"$")
	public void setUpUser(String firstName, String lastName, String eMail, String password) {
		// set up user for the clean up steps
		storage.user = new User(firstName, lastName, eMail, password);
	};
}
