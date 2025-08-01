/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package domain.blog;

import java.io.Serializable;

public class Author implements Serializable {

	protected int id;
	protected String username;
	protected String password;
	protected String email;
	protected String bio;
	protected Section favouriteSection;

	public Author() {
		this(-1, null, null, null, null, null);
	}

	public Author(Integer id, String username, String password, String email, String bio, Section section) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.email = email;
		this.bio = bio;
		this.favouriteSection = section;
	}

	public Author(int id) {
		this(id, null, null, null, null, null);
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public void setFavouriteSection(Section favouriteSection) {
		this.favouriteSection = favouriteSection;
	}

	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}

	public String getBio() {
		return bio;
	}

	public Section getFavouriteSection() {
		return favouriteSection;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Author))
			return false;

		Author author = (Author) o;

		if (id != author.id)
			return false;
		if (bio != null ? !bio.equals(author.bio) : author.bio != null)
			return false;
		if (email != null ? !email.equals(author.email) : author.email != null)
			return false;
		if (password != null ? !password.equals(author.password) : author.password != null)
			return false;
		if (username != null ? !username.equals(author.username) : author.username != null)
			return false;
		if (favouriteSection != null ? !favouriteSection.equals(author.favouriteSection)
				: author.favouriteSection != null)
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = id;
		result = 31 * result + (username != null ? username.hashCode() : 0);
		result = 31 * result + (password != null ? password.hashCode() : 0);
		result = 31 * result + (email != null ? email.hashCode() : 0);
		result = 31 * result + (bio != null ? bio.hashCode() : 0);
		result = 31 * result + (favouriteSection != null ? favouriteSection.hashCode() : 0);
		return result;
	}

	public String toString() {
		return "Author : " + id + " : " + username + " : " + email;
	}
}