/*
 * Breaker.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm,
 * Copyright (c) 2005 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package com.openhtmltopdf.layout;

import com.openhtmltopdf.css.constants.IdentValue;
import com.openhtmltopdf.css.style.CalculatedStyle;
import com.openhtmltopdf.extend.FSTextBreaker;
import com.openhtmltopdf.render.FSFont;

/**
 * A utility class that scans the text of a single inline box, looking for the
 * next break point.
 * @author Torbjoern Gannholm
 */
public class Breaker {

	public static void breakFirstLetter(LayoutContext c, LineBreakContext context, int avail, CalculatedStyle style) {
		FSFont font = style.getFSFont(c);
		context.setEnd(getFirstLetterEnd(context.getMaster(), context.getStart()));
		context.setWidth(c.getTextRenderer().getWidth(c.getFontContext(), font, context.getCalculatedSubstring()));

		if (context.getWidth() > avail) {
			context.setNeedsNewLine(true);
			context.setUnbreakable(true);
		}
	}

	private static int getFirstLetterEnd(String text, int start) {
		boolean letterFound = false;
		int end = text.length();
		int currentChar;
		for (int i = start; i < end;) {
			currentChar = text.codePointAt(i);
			if (!TextUtil.isFirstLetterSeparatorChar(currentChar)) {
				if (letterFound) {
					return i;
				} else {
					letterFound = true;
				}
			}
			i += Character.charCount(currentChar);
		}
		return end;
	}

	public static void breakText(LayoutContext c, LineBreakContext context, int avail, CalculatedStyle style) {
		FSFont font = style.getFSFont(c);
		IdentValue whitespace = style.getWhitespace();

		// ====== handle nowrap
		if (whitespace == IdentValue.NOWRAP) {
			context.setEnd(context.getLast());
			context.setWidth(c.getTextRenderer().getWidth(c.getFontContext(), font, context.getCalculatedSubstring()));
			return;
		}

		//check if we should break on the next newline
		if (whitespace == IdentValue.PRE || whitespace == IdentValue.PRE_WRAP || whitespace == IdentValue.PRE_LINE) {
			int n = context.getStartSubstring().indexOf(WhitespaceStripper.EOL);
			if (n > -1) {
				context.setEnd(context.getStart() + n + 1);
				context.setWidth(
						c.getTextRenderer().getWidth(c.getFontContext(), font, context.getCalculatedSubstring()));
				context.setNeedsNewLine(true);
				context.setEndsOnNL(true);
			} else if (whitespace == IdentValue.PRE) {
				context.setEnd(context.getLast());
				context.setWidth(
						c.getTextRenderer().getWidth(c.getFontContext(), font, context.getCalculatedSubstring()));
			}
		}

		//check if we may wrap
		if (whitespace == IdentValue.PRE || (context.isNeedsNewLine() && context.getWidth() <= avail)) {
			return;
		}

		context.setEndsOnNL(false);
		doBreakText(c, context, avail, style, false);
	}

	private static void doBreakText(LayoutContext c, LineBreakContext context, int avail, CalculatedStyle style,
			boolean tryToBreakAnywhere) {
		doBreakText(c, context, avail, style, STANDARD_CHARACTER_BREAKER, STANDARD_LINE_BREAKER, tryToBreakAnywhere);
	}

	public static void doBreakText(LayoutContext c, LineBreakContext context, int avail, CalculatedStyle style,
			TextBreakerSupplier characterBreaker, TextBreakerSupplier lineBreaker, boolean tryToBreakAnywhere) {

		FSFont font = style.getFSFont(c);
		String currentString = context.getStartSubstring();
		FSTextBreaker iterator = tryToBreakAnywhere ? characterBreaker.getBreaker(currentString, c.getSharedContext())
				: lineBreaker.getBreaker(currentString, c.getSharedContext());

		int left = 0;
		int right = iterator.next();
		int lastWrap = 0;
		int graphicsLength = 0;
		int lastGraphicsLength = 0;

		// FIXME: Should this be >= instead. See comment in BreakerTest.
		while (right > 0 && graphicsLength <= avail) {
			lastGraphicsLength = graphicsLength;
			graphicsLength += c.getTextRenderer().getWidth(c.getFontContext(), font,
					currentString.substring(left, right));
			lastWrap = left;
			left = right;
			right = iterator.next();
		}

		if (graphicsLength <= avail) {
			//try for the last bit too!
			lastWrap = left;
			lastGraphicsLength = graphicsLength;
			graphicsLength += c.getTextRenderer().getWidth(c.getFontContext(), font, currentString.substring(left));
		}

		if (graphicsLength <= avail) {
			context.setWidth(graphicsLength);
			context.setEnd(context.getMaster().length());
			//It fit!
			return;
		}

		context.setNeedsNewLine(true);
		if (lastWrap == 0 && style.getWordWrap() == IdentValue.BREAK_WORD) {
			if (!tryToBreakAnywhere) {
				doBreakText(c, context, avail, style, characterBreaker, lineBreaker, true);
				return;
			}
		}

		if (lastWrap != 0) {//found a place to wrap
			context.setEnd(context.getStart() + lastWrap);
			context.setWidth(lastGraphicsLength);
		} else {//unbreakable string
			if (left == 0) {
				left = currentString.length();
			}

			context.setEnd(context.getStart() + left);
			context.setUnbreakable(true);

			if (left == currentString.length()) {
				context.setWidth(
						c.getTextRenderer().getWidth(c.getFontContext(), font, context.getCalculatedSubstring()));
			} else {
				context.setWidth(graphicsLength);
			}
		}
	}

	public interface TextBreakerSupplier {
		public FSTextBreaker getBreaker(String str, SharedContext sharedContext);
	}

	private static class CharacterBreakerSupplier implements TextBreakerSupplier {
		@Override
		public FSTextBreaker getBreaker(String str, SharedContext sharedContext) {
			return getCharacterBreakStream(str, sharedContext);
		}
	}

	private static class LineBreakerSupplier implements TextBreakerSupplier {
		@Override
		public FSTextBreaker getBreaker(String str, SharedContext sharedContext) {
			return getLineBreakStream(str, sharedContext);
		}
	}

	public static final TextBreakerSupplier STANDARD_CHARACTER_BREAKER = new CharacterBreakerSupplier();
	public static final TextBreakerSupplier STANDARD_LINE_BREAKER = new LineBreakerSupplier();

	public static FSTextBreaker getCharacterBreakStream(String currentString, SharedContext sharedContext) {
		FSTextBreaker i = sharedContext.getCharacterBreaker();
		i.setText(currentString);
		return i;
	}

	public static FSTextBreaker getLineBreakStream(String s, SharedContext shared) {
		FSTextBreaker i = shared.getLineBreaker();
		i.setText(s);
		return i;
	}
}
