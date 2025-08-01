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
package org.alfresco.repo.search.impl.querymodel.impl;

import java.io.Serializable;

import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author andyh
 */
public class BasePropertyArgument extends BaseDynamicArgument implements PropertyArgument {
	private String propertyName;

	private String selector;

	/**
	 * @param name String
	 * @param queryable boolean
	 * @param orderable boolean
	 * @param selector String
	 * @param propertyName String
	 */
	public BasePropertyArgument(String name, boolean queryable, boolean orderable, String selector,
			String propertyName) {
		super(name, queryable, orderable);
		this.selector = selector;
		this.propertyName = propertyName;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.search.impl.querymodel.PropertyArgument#getSelector()
	 */
	public String getSelector() {
		return selector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.search.impl.querymodel.PropertyArgument#getPropertyName()
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.search.impl.querymodel.Argument#getValue()
	 */
	public Serializable getValue(FunctionEvaluationContext context) {
		NodeRef nodeRef = context.getNodeRefs().get(getSelector());
		return context.getProperty(nodeRef, getPropertyName());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasePropertyArgument[");
		builder.append("name=").append(getName()).append(", ");
		builder.append("selector=").append(getSelector()).append(", ");
		builder.append("propertName=").append(getPropertyName()).append(", ");
		builder.append("]");
		return builder.toString();
	}

}
