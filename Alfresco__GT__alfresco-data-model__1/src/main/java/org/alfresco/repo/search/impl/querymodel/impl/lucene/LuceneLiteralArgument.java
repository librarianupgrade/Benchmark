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
package org.alfresco.repo.search.impl.querymodel.impl.lucene;

import java.io.Serializable;

import org.alfresco.repo.search.impl.querymodel.impl.BaseLiteralArgument;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 *
 */
public class LuceneLiteralArgument extends BaseLiteralArgument {

	/**
	 * @param name String
	 * @param type QName
	 * @param value Serializable
	 */
	public LuceneLiteralArgument(String name, QName type, Serializable value) {
		super(name, type, value);
	}

}
