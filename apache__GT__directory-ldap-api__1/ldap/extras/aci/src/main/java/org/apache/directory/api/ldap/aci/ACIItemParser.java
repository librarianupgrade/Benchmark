/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    https://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */

package org.apache.directory.api.ldap.aci;

import java.io.StringReader;
import java.text.ParseException;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.normalizers.NameComponentNormalizer;
import org.apache.directory.api.util.StringConstants;

import antlr.RecognitionException;
import antlr.TokenStreamException;

/**
 * A reusable wrapper around the antlr generated parser for an ACIItem as
 * defined by X.501. This class enables the reuse of the antlr parser/lexer pair
 * without having to recreate them every time.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIItemParser {
	/** the antlr generated parser being wrapped */
	private ReusableAntlrACIItemParser parser;

	/** the antlr generated lexer being wrapped */
	private ReusableAntlrACIItemLexer lexer;

	/** The is normalizing flag. */
	private final boolean isNormalizing;

	/**
	 * Creates a ACIItem parser.
	 *
	 * @param schemaManager the schema manager
	 */
	public ACIItemParser(SchemaManager schemaManager) {
		this.lexer = new ReusableAntlrACIItemLexer(new StringReader(""));
		this.parser = new ReusableAntlrACIItemParser(lexer);

		// this method MUST be called while we cannot do
		// constructor overloading for antlr generated parser
		this.parser.init(schemaManager);

		this.isNormalizing = false;
	}

	/**
	 * Creates a normalizing ACIItem parser.
	 *
	 * @param normalizer the normalizer
	 * @param schemaManager the schema manager
	 */
	public ACIItemParser(NameComponentNormalizer normalizer, SchemaManager schemaManager) {
		this.lexer = new ReusableAntlrACIItemLexer(new StringReader(""));
		this.parser = new ReusableAntlrACIItemParser(lexer);

		this.parser.setNormalizer(normalizer);
		this.isNormalizing = true;

		// this method MUST be called while we cannot do
		// constructor overloading for antlr generated parser
		this.parser.init(schemaManager);
	}

	/**
	 * Initializes the plumbing by creating a pipe and coupling the parser/lexer
	 * pair with it. 
	 * 
	 * @param spec the specification to be parsed
	 */
	private synchronized void reset(String spec) {
		StringReader in = new StringReader(spec);
		this.lexer.prepareNextInput(in);
		this.parser.resetState();
	}

	/**
	 * Parses an ACIItem without exhausting the parser.
	 * 
	 * @param spec
	 *            the specification to be parsed
	 * @return the specification bean
	 * @throws ParseException
	 *             if there are any recognition errors (bad syntax)
	 */
	public synchronized ACIItem parse(String spec) throws ParseException {
		ACIItem aCIItem;

		if ((spec == null) || StringConstants.EMPTY.equals(spec.trim())) {
			return null;
		}

		// reset and initialize the parser / lexer pair
		reset(spec);

		try {
			aCIItem = this.parser.wrapperEntryPoint();
		} catch (TokenStreamException e) {
			throw new ParseException(I18n.err(I18n.ERR_07004_PARSER_FAILURE_ACI_ITEM, spec, e.getLocalizedMessage()),
					0);
		} catch (RecognitionException e) {
			throw new ParseException(I18n.err(I18n.ERR_07004_PARSER_FAILURE_ACI_ITEM, spec, e.getLocalizedMessage(),
					e.getLine(), e.getColumn()), e.getColumn());
		}

		return aCIItem;
	}

	/**
	 * Tests to see if this parser is normalizing.
	 * 
	 * @return true if it normalizes false otherwise
	 */
	public boolean isNormizing() {
		return this.isNormalizing;
	}
}
