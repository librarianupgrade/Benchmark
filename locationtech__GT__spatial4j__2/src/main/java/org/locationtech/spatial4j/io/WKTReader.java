/*******************************************************************************
 * Copyright (c) 2015 ElasticSearch and MITRE, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0 which
 * accompanies this distribution and is available at
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/

// A derivative of commit 14bc4dee08355048d6a94e33834b919a3999a06e
//  at https://github.com/chrismale/elasticsearch

package org.locationtech.spatial4j.io;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.context.SpatialContextFactory;
import org.locationtech.spatial4j.exception.InvalidShapeException;
import org.locationtech.spatial4j.shape.Shape;
import org.locationtech.spatial4j.shape.ShapeFactory;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

/**
 * An extensible parser for <a href="http://en.wikipedia.org/wiki/Well-known_text"> Well Known Text
 * (WKT)</a>. The shapes supported by this class are:
 * <ul>
 * <li>POINT</li>
 * <li>MULTIPOINT</li>
 * <li>ENVELOPE (strictly isn't WKT but is defined by OGC's <a
 * href="http://docs.geoserver.org/stable/en/user/tutorials/cql/cql_tutorial.html">Common Query
 * Language (CQL)</a>)</li>
 * <li>LINESTRING</li>
 * <li>MULTILINESTRING</li>
 * <LI>POLYGON</LI>
 * <LI>MULTIPOLYGON</LI>
 * <li>GEOMETRYCOLLECTION</li>
 * <li>BUFFER (non-standard Spatial4j operation)</li>
 * </ul>
 * 'EMPTY' is supported. Specifying 'Z', 'M', or any other dimensionality in the WKT is effectively
 * ignored. Thus, you can specify any number of numbers in the coordinate points but only the first
 * two take effect. The javadocs for the <code>parse___Shape</code> methods further describe these
 * shapes, or you
 *
 * <p>
 * Most users of this class will call just one method: {@link #parse(String)}, or
 * {@link #parseIfSupported(String)} to not fail if it isn't parse-able.
 *
 * <p>
 * To support more shapes, extend this class and override
 * {@link #parseShapeByType(WKTReader.State, String)}. It's also possible to delegate to a WKTParser
 * by also delegating {@link #newState(String)}.
 *
 * <p>
 * Note, instances of this base class are threadsafe.
 */
public class WKTReader implements ShapeReader {
	protected final SpatialContext ctx;
	protected final ShapeFactory shapeFactory;

	// TODO support SRID: "SRID=4326;POINT(1,2)

	/**
	 * This constructor is required by
	 * {@link org.locationtech.spatial4j.context.SpatialContextFactory#makeFormats(SpatialContext)}.
	 */
	public WKTReader(SpatialContext ctx, SpatialContextFactory factory) {
		this.ctx = ctx;
		this.shapeFactory = ctx.getShapeFactory();
	}

	/**
	 * Parses the wktString, returning the defined Shape.
	 *
	 * @return Non-null Shape defined in the String
	 * @throws ParseException Thrown if there is an error in the Shape definition
	 */
	public Shape parse(String wktString) throws ParseException, InvalidShapeException {
		Shape shape = parseIfSupported(wktString);// sets rawString & offset
		if (shape != null)
			return shape;
		String shortenedString = (wktString.length() <= 128 ? wktString : wktString.substring(0, 128 - 3) + "...");
		throw new ParseException("Unknown Shape definition [" + shortenedString + "]", 0);
	}

	/**
	 * Parses the wktString, returning the defined Shape. If it can't because the shape name is
	 * unknown or an empty or blank string was passed, then it returns null. If the WKT starts with a
	 * supported shape but contains an inner unsupported shape then it will result in a
	 * {@link ParseException}.
	 *
	 * @param wktString non-null, can be empty or have surrounding whitespace
	 * @return Shape, null if unknown / unsupported shape.
	 * @throws ParseException Thrown if there is an error in the Shape definition
	 */
	public Shape parseIfSupported(String wktString) throws ParseException, InvalidShapeException {
		State state = newState(wktString);
		state.nextIfWhitespace();// leading
		if (state.eof())
			return null;
		// shape types must start with a letter
		if (!Character.isLetter(state.rawString.charAt(state.offset)))
			return null;
		String shapeType = state.nextWord();
		Shape result = null;
		try {
			result = parseShapeByType(state, shapeType);
		} catch (ParseException | InvalidShapeException e) {
			throw e;
		} catch (IllegalArgumentException e) { // NOTE: JTS Throws IAE for bad WKT
			throw new InvalidShapeException(e.getMessage(), e);
		} catch (Exception e) {
			ParseException pe = new ParseException(e.toString(), state.offset);
			pe.initCause(e);
			throw pe;
		}
		if (result != null && !state.eof())
			throw new ParseException("end of shape expected", state.offset);
		return result;
	}

	/**
	 * (internal) Creates a new State with the given String. It's only called by
	 * {@link #parseIfSupported(String)}. This is an extension point for subclassing.
	 */
	protected State newState(String wktString) {
		// NOTE: if we wanted to re-use old States to reduce object allocation, we might do that
		// here. But in the scheme of things, it doesn't seem worth the bother as it complicates the
		// thread-safety story of the API for too little of a gain.
		return new State(wktString);
	}

	/**
	 * (internal) Parses the remainder of a shape definition following the shape's name given as
	 * {@code shapeType} already consumed via {@link State#nextWord()}. If it's able to parse the
	 * shape, {@link WKTReader.State#offset} should be advanced beyond it (e.g. to the ',' or ')' or
	 * EOF in general). The default implementation checks the name against some predefined names and
	 * calls corresponding parse methods to handle the rest. Overriding this method is an excellent
	 * extension point for additional shape types. Or, use this class by delegation to this method.
	 * <p>
	 * When writing a parse method that reacts to a specific shape type, remember to handle the
	 * dimension and EMPTY token via
	 * {@link org.locationtech.spatial4j.io.WKTReader.State#nextIfEmptyAndSkipZM()}.
	 *
	 * @param state
	 * @param shapeType Non-Null string; could have mixed case. The first character is a letter.
	 * @return The shape or null if not supported / unknown.
	 */
	protected Shape parseShapeByType(State state, String shapeType) throws ParseException {
		assert Character.isLetter(shapeType.charAt(0)) : "Shape must start with letter: " + shapeType;

		if (shapeType.equalsIgnoreCase("POINT")) {
			return parsePointShape(state);
		} else if (shapeType.equalsIgnoreCase("MULTIPOINT")) {
			return parseMultiPointShape(state);
		} else if (shapeType.equalsIgnoreCase("ENVELOPE")) {
			return parseEnvelopeShape(state);
		} else if (shapeType.equalsIgnoreCase("LINESTRING")) {
			return parseLineStringShape(state);
		} else if (shapeType.equalsIgnoreCase("POLYGON")) {
			return parsePolygonShape(state);
		} else if (shapeType.equalsIgnoreCase("GEOMETRYCOLLECTION")) {
			return parseGeometryCollectionShape(state);
		} else if (shapeType.equalsIgnoreCase("MULTILINESTRING")) {
			return parseMultiLineStringShape(state);
		} else if (shapeType.equalsIgnoreCase("MULTIPOLYGON")) {
			return parseMulitPolygonShape(state);
		}
		// extension
		if (shapeType.equalsIgnoreCase("BUFFER")) {
			return parseBufferShape(state);
		}

		// HEY! Update class Javadocs if add more shapes
		return null;
	}

	/**
	 * Parses the BUFFER operation applied to a parsed shape.
	 * 
	 * <pre>
	 *   '(' shape ',' number ')'
	 * </pre>
	 * 
	 * Whereas 'number' is the distance to buffer the shape by.
	 */
	protected Shape parseBufferShape(State state) throws ParseException {
		state.nextExpect('(');
		Shape shape = shape(state);
		state.nextExpect(',');
		double distance = shapeFactory.normDist(state.nextDouble());
		state.nextExpect(')');
		return shape.getBuffered(distance, ctx);
	}

	/**
	 * Parses a POINT shape from the raw string.
	 * 
	 * <pre>
	 *   '(' coordinate ')'
	 * </pre>
	 *
	 * @see #point(State, ShapeFactory.PointsBuilder)
	 */
	protected Shape parsePointShape(State state) throws ParseException {
		if (state.nextIfEmptyAndSkipZM())
			return shapeFactory.pointXY(Double.NaN, Double.NaN);
		state.nextExpect('(');
		OnePointsBuilder onePointsBuilder = new OnePointsBuilder(shapeFactory);
		point(state, onePointsBuilder);
		state.nextExpect(')');
		return onePointsBuilder.getPoint();
	}

	/**
	 * Parses a MULTIPOINT shape from the raw string -- a collection of points.
	 * 
	 * <pre>
	 *   '(' coordinate (',' coordinate )* ')'
	 * </pre>
	 * 
	 * Furthermore, coordinate can optionally be wrapped in parenthesis.
	 *
	 * @see #point(State, ShapeFactory.PointsBuilder)
	 */
	protected Shape parseMultiPointShape(State state) throws ParseException {
		ShapeFactory.MultiPointBuilder builder = shapeFactory.multiPoint();
		if (state.nextIfEmptyAndSkipZM())
			return builder.build();
		state.nextExpect('(');
		do {
			boolean openParen = state.nextIf('(');
			point(state, builder);
			if (openParen)
				state.nextExpect(')');
		} while (state.nextIf(','));
		state.nextExpect(')');
		return builder.build();
	}

	/**
	 * Parses an ENVELOPE (aka Rectangle) shape from the raw string. The values are normalized.
	 * <p>
	 * Source: OGC "Catalogue Services Specification", the "CQL" (Common Query Language) sub-spec.
	 * <em>Note the inconsistent order of the min &amp; max values between x &amp; y!</em>
	 * 
	 * <pre>
	 *   '(' x1 ',' x2 ',' y2 ',' y1 ')'
	 * </pre>
	 */
	protected Shape parseEnvelopeShape(State state) throws ParseException {
		// FYI no dimension or EMPTY
		state.nextExpect('(');
		double x1 = state.nextDouble();
		state.nextExpect(',');
		double x2 = state.nextDouble();
		state.nextExpect(',');
		double y2 = state.nextDouble();
		state.nextExpect(',');
		double y1 = state.nextDouble();
		state.nextExpect(')');
		return shapeFactory.rect(shapeFactory.normX(x1), shapeFactory.normX(x2), shapeFactory.normY(y1),
				shapeFactory.normY(y2));
	}

	/**
	 * Parses a LINESTRING shape from the raw string -- an ordered sequence of points.
	 * 
	 * <pre>
	 * coordinateSequence
	 * </pre>
	 *
	 * @see #pointList(State, ShapeFactory.PointsBuilder)
	 */
	protected Shape parseLineStringShape(State state) throws ParseException {
		ShapeFactory.LineStringBuilder lineStringBuilder = shapeFactory.lineString();
		if (state.nextIfEmptyAndSkipZM())
			return lineStringBuilder.build();
		return pointList(state, lineStringBuilder).build();
	}

	/**
	 * Parses a MULTILINESTRING shape from the raw string -- a collection of line strings.
	 * 
	 * <pre>
	 *   '(' coordinateSequence (',' coordinateSequence )* ')'
	 * </pre>
	 *
	 * @see #parseLineStringShape(org.locationtech.spatial4j.io.WKTReader.State)
	 */
	protected Shape parseMultiLineStringShape(State state) throws ParseException {
		ShapeFactory.MultiLineStringBuilder multiLineStringBuilder = shapeFactory.multiLineString();
		if (!state.nextIfEmptyAndSkipZM()) {
			state.nextExpect('(');
			do {
				multiLineStringBuilder.add(pointList(state, multiLineStringBuilder.lineString()));
			} while (state.nextIf(','));
			state.nextExpect(')');
		}
		return multiLineStringBuilder.build();
	}

	/**
	 * Parses a POLYGON shape from the raw string. It might return a
	 * {@link org.locationtech.spatial4j.shape.Rectangle} if the polygon is one.
	 *
	 * <pre>
	 * coordinateSequenceList
	 * </pre>
	 */
	protected Shape parsePolygonShape(WKTReader.State state) throws ParseException {
		ShapeFactory.PolygonBuilder polygonBuilder = shapeFactory.polygon();
		if (!state.nextIfEmptyAndSkipZM()) {
			polygonBuilder = polygon(state, polygonBuilder);
		}
		return polygonBuilder.buildOrRect();
	}

	/**
	 * Parses a MULTIPOLYGON shape from the raw string.
	 *
	 * <pre>
	 *   '(' polygon (',' polygon )* ')'
	 * </pre>
	 */
	protected Shape parseMulitPolygonShape(WKTReader.State state) throws ParseException {
		ShapeFactory.MultiPolygonBuilder multiPolygonBuilder = shapeFactory.multiPolygon();
		if (!state.nextIfEmptyAndSkipZM()) {
			state.nextExpect('(');
			do {
				multiPolygonBuilder.add(polygon(state, multiPolygonBuilder.polygon()));
			} while (state.nextIf(','));
			state.nextExpect(')');
		}
		return multiPolygonBuilder.build();
	}

	/**
	 * Parses a GEOMETRYCOLLECTION shape from the raw string.
	 * 
	 * <pre>
	 *   '(' shape (',' shape )* ')'
	 * </pre>
	 */
	protected Shape parseGeometryCollectionShape(State state) throws ParseException {
		ShapeFactory.MultiShapeBuilder<Shape> multiShapeBuilder = shapeFactory.multiShape(Shape.class);
		if (state.nextIfEmptyAndSkipZM())
			return multiShapeBuilder.build();
		state.nextExpect('(');
		do {
			multiShapeBuilder.add(shape(state));
		} while (state.nextIf(','));
		state.nextExpect(')');
		return multiShapeBuilder.build();
	}

	/**
	 * Reads a shape from the current position, starting with the name of the shape. It calls
	 * {@link #parseShapeByType(org.locationtech.spatial4j.io.WKTReader.State, String)} and throws an
	 * exception if the shape wasn't supported.
	 */
	protected Shape shape(State state) throws ParseException {
		String type = state.nextWord();
		Shape shape = parseShapeByType(state, type);
		if (shape == null)
			throw new ParseException("Shape of type " + type + " is unknown", state.offset);
		return shape;
	}

	/**
	 * Reads a list of Points (AKA CoordinateSequence) from the current position.
	 * 
	 * <pre>
	 *   '(' coordinate (',' coordinate )* ')'
	 * </pre>
	 *
	 * @see #point(State, ShapeFactory.PointsBuilder)
	 */
	protected <B extends ShapeFactory.PointsBuilder> B pointList(State state, B pointsBuilder) throws ParseException {
		state.nextExpect('(');
		do {
			point(state, pointsBuilder);
		} while (state.nextIf(','));
		state.nextExpect(')');
		return pointsBuilder;
	}

	/**
	 * Reads a raw Point (AKA Coordinate) from the current position. Only the first 2 numbers are
	 * used. The values are normalized.
	 * 
	 * <pre>
	 *   number number number*
	 * </pre>
	 */
	protected ShapeFactory.PointsBuilder point(State state, ShapeFactory.PointsBuilder pointsBuilder)
			throws ParseException {
		double x = state.nextDouble();
		double y = state.nextDouble();
		state.skipNextDoubles();//TODO capture to call pointXYZ
		pointsBuilder.pointXY(shapeFactory.normX(x), shapeFactory.normY(y));
		return pointsBuilder;
	}

	/**
	 * Reads a polygon
	 */
	protected ShapeFactory.PolygonBuilder polygon(WKTReader.State state, ShapeFactory.PolygonBuilder polygonBuilder)
			throws ParseException {
		state.nextExpect('(');
		pointList(state, polygonBuilder); // outer ring
		while (state.nextIf(',')) {
			ShapeFactory.PolygonBuilder.HoleBuilder holeBuilder = polygonBuilder.hole();
			pointList(state, holeBuilder);
			holeBuilder.endHole();
		}
		state.nextExpect(')');
		return polygonBuilder;
	}

	/** The parse state. */
	public class State {
		/** Set in {@link #parseIfSupported(String)}. */
		public String rawString;
		/** Offset of the next char in {@link #rawString} to be read. */
		public int offset;
		/** Dimensionality specifier (e.g. 'Z', or 'M') following a shape type name. */
		public String dimension;

		public State(String rawString) {
			this.rawString = rawString;
		}

		public SpatialContext getCtx() {
			return ctx;
		}

		public WKTReader getParser() {
			return WKTReader.this;
		}

		/**
		 * Reads the word starting at the current character position. The word terminates once
		 * {@link Character#isJavaIdentifierPart(char)} returns false (or EOF). {@link #offset} is
		 * advanced past whitespace.
		 *
		 * @return Non-null non-empty String.
		 */
		public String nextWord() throws ParseException {
			int startOffset = offset;
			while (offset < rawString.length() && Character.isJavaIdentifierPart(rawString.charAt(offset))) {
				offset++;
			}
			if (startOffset == offset)
				throw new ParseException("Word expected", startOffset);
			String result = rawString.substring(startOffset, offset);
			nextIfWhitespace();
			return result;
		}

		/**
		 * Skips over a dimensionality token (e.g. 'Z' or 'M') if found, storing in {@link #dimension},
		 * and then looks for EMPTY, consuming that and whitespace.
		 * 
		 * <pre>
		 *   dimensionToken? 'EMPTY'?
		 * </pre>
		 * 
		 * @return True if EMPTY was found.
		 */
		public boolean nextIfEmptyAndSkipZM() throws ParseException {
			if (eof())
				return false;
			char c = rawString.charAt(offset);
			if (c == '(' || !Character.isJavaIdentifierPart(c))
				return false;
			String word = nextWord();
			if (word.equalsIgnoreCase("EMPTY"))
				return true;
			// we figure this word is Z or ZM or some other dimensionality signifier. We skip it.
			this.dimension = word;

			if (eof())
				return false;
			c = rawString.charAt(offset);
			if (c == '(' || !Character.isJavaIdentifierPart(c))
				return false;
			word = nextWord();
			if (word.equalsIgnoreCase("EMPTY"))
				return true;
			throw new ParseException("Expected EMPTY because found dimension; but got [" + word + "]", offset);
		}

		/**
		 * Reads in a double from the String. Parses digits with an optional decimal, sign, or exponent.
		 * NaN and Infinity are not supported. {@link #offset} is advanced past whitespace.
		 *
		 * @return Double value
		 */
		public double nextDouble() throws ParseException {
			int startOffset = offset;
			skipDouble();
			if (startOffset == offset)
				throw new ParseException("Expected a number", offset);
			double result;
			try {
				result = Double.parseDouble(rawString.substring(startOffset, offset));
			} catch (Exception e) {
				throw new ParseException(e.toString(), offset);
			}
			nextIfWhitespace();
			return result;
		}

		/** Advances offset forward until it points to a character that isn't part of a number. */
		public void skipDouble() {
			int startOffset = offset;
			for (; offset < rawString.length(); offset++) {
				char c = rawString.charAt(offset);
				if (!(Character.isDigit(c) || c == '.' || c == '-' || c == '+')) {
					// 'e' is okay as long as it isn't first
					if (offset != startOffset && (c == 'e' || c == 'E'))
						continue;
					break;
				}
			}
		}

		/** Advances past as many doubles as there are, with intervening whitespace. */
		public void skipNextDoubles() {
			while (!eof()) {
				int startOffset = offset;
				skipDouble();
				if (startOffset == offset)
					return;
				nextIfWhitespace();
			}
		}

		/**
		 * Verifies that the current character is of the expected value. If the character is the
		 * expected value, then it is consumed and {@link #offset} is advanced past whitespace.
		 *
		 * @param expected The expected char.
		 */
		public void nextExpect(char expected) throws ParseException {
			if (eof())
				throw new ParseException("Expected [" + expected + "] found EOF", offset);
			char c = rawString.charAt(offset);
			if (c != expected)
				throw new ParseException("Expected [" + expected + "] found [" + c + "]", offset);
			offset++;
			nextIfWhitespace();
		}

		/** If the string is consumed, i.e. at end-of-file. */
		public final boolean eof() {
			return offset >= rawString.length();
		}

		/**
		 * If the current character is {@code expected}, then offset is advanced after it and any
		 * subsequent whitespace. Otherwise, false is returned.
		 *
		 * @param expected The expected char
		 * @return true if consumed
		 */
		public boolean nextIf(char expected) {
			if (!eof() && rawString.charAt(offset) == expected) {
				offset++;
				nextIfWhitespace();
				return true;
			}
			return false;
		}

		/**
		 * Moves offset to next non-whitespace character. Doesn't move if the offset is already at
		 * non-whitespace. <em>There is very little reason for subclasses to call this because
		 * most other parsing methods call it.</em>
		 */
		public void nextIfWhitespace() {
			for (; offset < rawString.length(); offset++) {
				if (!Character.isWhitespace(rawString.charAt(offset))) {
					return;
				}
			}
		}

		/**
		 * Returns the next chunk of text till the next ',' or ')' (non-inclusive) or EOF. If a '(' is
		 * encountered, then it looks past its matching ')', taking care to handle nested matching
		 * parenthesis too. It's designed to be of use to subclasses that wish to get the entire
		 * subshape at the current position as a string so that it might be passed to other software
		 * that will parse it.
		 * <p>
		 * Example:
		 * 
		 * <pre>
		 * OUTER(INNER(3, 5))
		 * </pre>
		 * 
		 * If this is called when offset is at the first character, then it will return this whole
		 * string. If called at the "I" then it will return "INNER(3, 5)". If called at "3", then it
		 * will return "3". In all cases, offset will be positioned at the next position following the
		 * returned substring.
		 *
		 * @return non-null substring.
		 */
		public String nextSubShapeString() throws ParseException {
			int startOffset = offset;
			int parenStack = 0;// how many parenthesis levels are we in?
			for (; offset < rawString.length(); offset++) {
				char c = rawString.charAt(offset);
				if (c == ',') {
					if (parenStack == 0)
						break;
				} else if (c == ')') {
					if (parenStack == 0)
						break;
					parenStack--;
				} else if (c == '(') {
					parenStack++;
				}
			}
			if (parenStack != 0)
				throw new ParseException("Unbalanced parenthesis", startOffset);
			return rawString.substring(startOffset, offset);
		}

	}// class State

	@Override
	public String getFormatName() {
		return ShapeIO.WKT;
	}

	static String readString(Reader reader) throws IOException {
		char[] arr = new char[1024];
		StringBuilder buffer = new StringBuilder();
		int numCharsRead;
		while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
			buffer.append(arr, 0, numCharsRead);
		}
		return buffer.toString();
	}

	@Override
	public Shape read(Reader reader) throws IOException, ParseException {
		return parse(readString(reader));
	}

	@Override
	public Shape read(Object value) throws IOException, ParseException, InvalidShapeException {
		return parse(value.toString());
	}

	@Override
	public Shape readIfSupported(Object value) throws InvalidShapeException {
		try {
			return parseIfSupported(value.toString());
		} catch (ParseException e) {
		}
		return null;
	}
}
