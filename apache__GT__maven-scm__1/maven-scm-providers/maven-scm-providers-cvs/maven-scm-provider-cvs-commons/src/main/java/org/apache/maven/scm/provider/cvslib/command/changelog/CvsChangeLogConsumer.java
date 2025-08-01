package org.apache.maven.scm.provider.cvslib.command.changelog;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author Olivier Lamy
 *
 */
public class CvsChangeLogConsumer extends AbstractConsumer {
	private List<ChangeSet> entries = new ArrayList<ChangeSet>();

	// state machine constants for reading cvs output

	/**
	 * expecting file information
	 */
	private static final int GET_FILE = 1;

	/**
	 * expecting date
	 */
	private static final int GET_DATE = 2;

	/**
	 * expecting comments
	 */
	private static final int GET_COMMENT = 3;

	/**
	 * expecting revision
	 */
	private static final int GET_REVISION = 4;

	/**
	 * Marks start of file data
	 */
	private static final String START_FILE = "Working file: ";

	/**
	 * Marks end of file
	 */
	private static final String END_FILE = "==================================="
			+ "==========================================";

	/**
	 * Marks start of revision
	 */
	private static final String START_REVISION = "----------------------------";

	/**
	 * Marks revision data
	 */
	private static final String REVISION_TAG = "revision ";

	/**
	 * Marks date data
	 */
	private static final String DATE_TAG = "date: ";

	/**
	 * current status of the parser
	 */
	private int status = GET_FILE;

	/**
	 * the current log entry being processed by the parser
	 */
	private ChangeSet currentChange = null;

	/**
	 * the current file being processed by the parser
	 */
	private ChangeFile currentFile = null;

	private String userDatePattern;

	public CvsChangeLogConsumer(ScmLogger logger, String userDatePattern) {
		super(logger);

		this.userDatePattern = userDatePattern;
	}

	public List<ChangeSet> getModifications() {
		Collections.sort(entries, new Comparator<ChangeSet>() {
			public int compare(ChangeSet set1, ChangeSet set2) {
				return set1.getDate().compareTo(set2.getDate());
			}
		});
		List<ChangeSet> fixedModifications = new ArrayList<ChangeSet>();
		ChangeSet currentEntry = null;
		for (Iterator<ChangeSet> entryIterator = entries.iterator(); entryIterator.hasNext();) {
			ChangeSet entry = (ChangeSet) entryIterator.next();
			if (currentEntry == null) {
				currentEntry = entry;
			} else if (areEqual(currentEntry, entry)) {
				currentEntry.addFile((ChangeFile) entry.getFiles().get(0));
			} else {
				fixedModifications.add(currentEntry);
				currentEntry = entry;
			}
		}
		if (currentEntry != null) {
			fixedModifications.add(currentEntry);
		}
		return fixedModifications;
	}

	private boolean areEqual(ChangeSet set1, ChangeSet set2) {
		if (set1.getAuthor().equals(set2.getAuthor()) && set1.getComment().equals(set2.getComment())
				&& set1.getDate().equals(set2.getDate())) {
			return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	public void consumeLine(String line) {
		if (getLogger().isDebugEnabled()) {
			getLogger().debug(line);
		}
		try {
			switch (getStatus()) {
			case GET_FILE:
				processGetFile(line);
				break;
			case GET_REVISION:
				processGetRevision(line);
				break;
			case GET_DATE:
				processGetDate(line);
				break;
			case GET_COMMENT:
				processGetComment(line);
				break;
			default:
				throw new IllegalStateException("Unknown state: " + status);
			}
		} catch (Throwable ex) {
			if (getLogger().isWarnEnabled()) {
				getLogger().warn("Exception in the cvs changelog consumer.", ex);
			}
		}
	}

	/**
	 * Add a change log entry to the list (if it's not already there) with the
	 * given file.
	 *
	 * @param entry a {@link ChangeSet}to be added to the list if another
	 *              with the same key doesn't exist already. If the entry's author
	 *              is null, the entry wont be added
	 * @param file  a {@link ChangeFile}to be added to the entry
	 */
	private void addEntry(ChangeSet entry, ChangeFile file) {
		// do not add if entry is not populated
		if (entry.getAuthor() == null) {
			return;
		}

		entry.addFile(file);

		entries.add(entry);
	}

	/**
	 * Process the current input line in the Get File state.
	 *
	 * @param line a line of text from the cvs log output
	 */
	private void processGetFile(String line) {
		if (line.startsWith(START_FILE)) {
			setCurrentChange(new ChangeSet());
			setCurrentFile(new ChangeFile(line.substring(START_FILE.length(), line.length())));
			setStatus(GET_REVISION);
		}
	}

	/**
	 * Process the current input line in the Get Revision state.
	 *
	 * @param line a line of text from the cvs log output
	 */
	private void processGetRevision(String line) {
		if (line.startsWith(REVISION_TAG)) {
			getCurrentFile().setRevision(line.substring(REVISION_TAG.length()));
			setStatus(GET_DATE);
		} else if (line.startsWith(END_FILE)) {
			// If we encounter an end of file line, it means there
			// are no more revisions for the current file.
			// there could also be a file still being processed.
			setStatus(GET_FILE);
			addEntry(getCurrentChange(), getCurrentFile());
		}
	}

	/**
	 * Process the current input line in the Get Date state.
	 *
	 * @param line a line of text from the cvs log output
	 */
	private void processGetDate(String line) {
		if (line.startsWith(DATE_TAG)) {
			StringTokenizer tokenizer = new StringTokenizer(line, ";");
			// date: YYYY/mm/dd HH:mm:ss [Z]; author: name;...

			String datePart = tokenizer.nextToken().trim();
			String dateTime = datePart.substring("date: ".length());
			StringTokenizer dateTokenizer = new StringTokenizer(dateTime, " ");
			if (dateTokenizer.countTokens() == 2) {
				dateTime += " UTC";
			}
			getCurrentChange().setDate(dateTime, userDatePattern);

			String authorPart = tokenizer.nextToken().trim();
			String author = authorPart.substring("author: ".length());
			getCurrentChange().setAuthor(author);
			setStatus(GET_COMMENT);
		}
	}

	/**
	 * Process the current input line in the Get Comment state.
	 *
	 * @param line a line of text from the cvs log output
	 */
	private void processGetComment(String line) {
		if (line.startsWith(START_REVISION)) {
			// add entry, and set state to get revision
			addEntry(getCurrentChange(), getCurrentFile());
			// new change log entry
			setCurrentChange(new ChangeSet());
			// same file name, but different rev
			setCurrentFile(new ChangeFile(getCurrentFile().getName()));
			setStatus(GET_REVISION);
		} else if (line.startsWith(END_FILE)) {
			addEntry(getCurrentChange(), getCurrentFile());
			setStatus(GET_FILE);
		} else {
			// keep gathering comments
			getCurrentChange().setComment(getCurrentChange().getComment() + line + "\n");
		}
	}

	/**
	 * Getter for property currentFile.
	 *
	 * @return Value of property currentFile.
	 */
	private ChangeFile getCurrentFile() {
		return currentFile;
	}

	/**
	 * Setter for property currentFile.
	 *
	 * @param currentFile New value of property currentFile.
	 */
	private void setCurrentFile(ChangeFile currentFile) {
		this.currentFile = currentFile;
	}

	/**
	 * Getter for property currentChange.
	 *
	 * @return Value of property currentChange.
	 */
	private ChangeSet getCurrentChange() {
		return currentChange;
	}

	/**
	 * Setter for property currentChange.
	 *
	 * @param currentChange New value of property currentChange.
	 */
	private void setCurrentChange(ChangeSet currentChange) {
		this.currentChange = currentChange;
	}

	/**
	 * Getter for property status.
	 *
	 * @return Value of property status.
	 */
	private int getStatus() {
		return status;
	}

	/**
	 * Setter for property status.
	 *
	 * @param status New value of property status.
	 */
	private void setStatus(int status) {
		this.status = status;
	}
}
