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
package org.alfresco.opencmis.dictionary;

import java.util.Collection;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISUtils;
import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;

public class FolderTypeDefintionWrapper extends ShadowTypeDefinitionWrapper {
	private static final long serialVersionUID = 1L;

	private FolderTypeDefinitionImpl typeDef;
	private FolderTypeDefinitionImpl typeDefInclProperties;
	private DictionaryService dictionaryService;

	public FolderTypeDefintionWrapper(CMISMapping cmisMapping, PropertyAccessorMapping accessorMapping,
			PropertyLuceneBuilderMapping luceneBuilderMapping, String typeId, DictionaryService dictionaryService,
			ClassDefinition cmisClassDef) {
		this.dictionaryService = dictionaryService;
		alfrescoName = cmisClassDef.getName();
		alfrescoClass = cmisMapping.getAlfrescoClass(alfrescoName);

		typeDef = new FolderTypeDefinitionImpl();

		typeDef.setBaseTypeId(BaseTypeId.CMIS_FOLDER);
		typeDef.setId(typeId);
		typeDef.setLocalName(alfrescoName.getLocalName());
		typeDef.setLocalNamespace(alfrescoName.getNamespaceURI());

		boolean isSystemFolder = false;
		if (BaseTypeId.CMIS_FOLDER.value().equals(typeId)) {
			typeDef.setQueryName(ISO9075.encodeSQL(typeId));
			typeDef.setParentTypeId(null);
		} else {
			typeDef.setQueryName(ISO9075.encodeSQL(cmisMapping.buildPrefixEncodedString(alfrescoName)));
			QName parentQName = cmisMapping.getCmisType(cmisClassDef.getParentName());
			if (cmisMapping.isValidCmisFolder(parentQName)) {
				typeDef.setParentTypeId(cmisMapping.getCmisTypeId(BaseTypeId.CMIS_FOLDER, parentQName));
			}

			if (alfrescoName.equals(ContentModel.TYPE_SYSTEM_FOLDER)
					|| cmisMapping.getDictionaryService().isSubClass(alfrescoName, ContentModel.TYPE_SYSTEM_FOLDER)) {
				isSystemFolder = true;
			}
		}

		typeDef.setDisplayName(null);
		typeDef.setDescription(null);

		typeDef.setIsCreatable(!isSystemFolder);
		typeDef.setIsQueryable(true);
		typeDef.setIsFulltextIndexed(true);
		typeDef.setIsControllablePolicy(false);
		typeDef.setIsControllableAcl(true);
		typeDef.setIsIncludedInSupertypeQuery(cmisClassDef.getIncludedInSuperTypeQuery());
		typeDef.setIsFileable(true);

		typeDefInclProperties = CMISUtils.copy(typeDef);
		setTypeDefinition(typeDef, typeDefInclProperties);

		createOwningPropertyDefinitions(cmisMapping, accessorMapping, luceneBuilderMapping, dictionaryService,
				cmisClassDef);
		createActionEvaluators(accessorMapping, BaseTypeId.CMIS_FOLDER);
	}

	@Override
	public void updateDefinition(DictionaryService dictionaryService) {
		TypeDefinition typeDef = dictionaryService.getType(alfrescoName);

		if (typeDef != null) {
			setTypeDefDisplayName(typeDef.getTitle(dictionaryService));
			setTypeDefDescription(typeDef.getDescription(dictionaryService));
		} else {
			super.updateDefinition(dictionaryService);
		}

		updateTypeDefInclProperties();
	}

	@Override
	public PropertyDefinitionWrapper getPropertyById(String propertyId) {
		updateProperty(dictionaryService, propertiesById.get(propertyId));
		return propertiesById.get(propertyId);
	}

	@Override
	public Collection<PropertyDefinitionWrapper> getProperties() {
		updateProperties(dictionaryService);
		return propertiesById.values();
	}

	@Override
	public Collection<PropertyDefinitionWrapper> getProperties(boolean update) {
		if (update) {
			return getProperties();
		} else {
			return propertiesById.values();
		}
	}
}
