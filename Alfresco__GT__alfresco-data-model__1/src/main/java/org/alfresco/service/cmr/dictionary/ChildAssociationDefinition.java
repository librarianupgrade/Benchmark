/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.service.cmr.dictionary;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Read-only definition of a Child Association.
 * 
 * @author David Caruana
 *
 */
@AlfrescoPublicApi
public interface ChildAssociationDefinition extends AssociationDefinition {

	/**
	 * @return the required name of children (or null if none)
	 */
	public String getRequiredChildName();

	/**
	 * @return whether duplicate child names allowed within this association? 
	 */
	public boolean getDuplicateChildNamesAllowed();

	/**
	 * @return  whether timestamps should be propagated upwards along primary associations
	 */
	public boolean getPropagateTimestamps();
}
