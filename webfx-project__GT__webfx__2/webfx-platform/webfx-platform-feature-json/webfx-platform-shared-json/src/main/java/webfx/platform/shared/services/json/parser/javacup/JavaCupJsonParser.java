
//----------------------------------------------------
// The following code was generated by CUP v0.11b 20150930 (SVN rev 66)
//----------------------------------------------------

package webfx.platform.shared.services.json.parser.javacup;

import webfx.platform.shared.services.json.Json;
import webfx.platform.shared.services.json.WritableJsonArray;
import webfx.platform.shared.services.json.WritableJsonObject;
import webfx.platform.shared.util.Numbers;
import webfx.platform.shared.util.tuples.Pair;
import java_cup.runtime.XMLElement;

/** CUP v0.11b 20150930 (SVN rev 66) generated parser.
  */
@SuppressWarnings({ "rawtypes" })
public class JavaCupJsonParser extends java_cup.runtime.lr_parser {

	public final Class getSymbolContainer() {
		return JsonSymbols.class;
	}

	/** Default constructor. */
	@Deprecated
	public JavaCupJsonParser() {
		super();
	}

	/** Constructor which sets the default scanner. */
	@Deprecated
	public JavaCupJsonParser(java_cup.runtime.Scanner s) {
		super(s);
	}

	/** Constructor which sets the default scanner. */
	public JavaCupJsonParser(java_cup.runtime.Scanner s, java_cup.runtime.SymbolFactory sf) {
		super(s, sf);
	}

	/** Production table. */
	protected static final short _production_table[][] = unpackFromStrings(
			new String[] { "\000\024\000\002\002\004\000\002\002\003\000\002\002"
					+ "\003\000\002\002\003\000\002\003\003\000\002\004\003"
					+ "\000\002\004\004\000\002\004\003\000\002\004\003\000"
					+ "\002\004\003\000\002\005\005\000\002\005\004\000\002"
					+ "\006\005\000\002\006\003\000\002\007\005\000\002\007"
					+ "\004\000\002\010\005\000\002\010\003\000\002\011\005" + "\000\002\011\005" });

	/** Access to production table. */
	public short[][] production_table() {
		return _production_table;
	}

	/** Parse-action table. */
	protected static final short[][] _action_table = unpackFromStrings(
			new String[] { "\000\041\000\020\004\017\006\016\012\010\013\006\014"
					+ "\004\015\007\016\011\001\002\000\004\013\043\001\002"
					+ "\000\004\002\042\001\002\000\012\002\ufffc\005\ufffc\007"
					+ "\ufffc\011\ufffc\001\002\000\012\002\ufff9\005\ufff9\007\ufff9"
					+ "\011\ufff9\001\002\000\012\002\ufff8\005\ufff8\007\ufff8\011"
					+ "\ufff8\001\002\000\012\002\ufffa\005\ufffa\007\ufffa\011\ufffa"
					+ "\001\002\000\012\002\ufffd\005\ufffd\007\ufffd\011\ufffd\001"
					+ "\002\000\012\002\ufffe\005\ufffe\007\ufffe\011\ufffe\001\002"
					+ "\000\012\002\000\005\000\007\000\011\000\001\002\000"
					+ "\012\002\uffff\005\uffff\007\uffff\011\uffff\001\002\000\022"
					+ "\004\017\006\016\007\036\012\010\013\006\014\004\015"
					+ "\007\016\011\001\002\000\010\005\024\016\021\017\023"
					+ "\001\002\000\006\005\032\011\031\001\002\000\004\010"
					+ "\027\001\002\000\006\005\ufff0\011\ufff0\001\002\000\004"
					+ "\010\025\001\002\000\012\002\ufff2\005\ufff2\007\ufff2\011"
					+ "\ufff2\001\002\000\020\004\017\006\016\012\010\013\006"
					+ "\014\004\015\007\016\011\001\002\000\006\005\uffee\011"
					+ "\uffee\001\002\000\020\004\017\006\016\012\010\013\006"
					+ "\014\004\015\007\016\011\001\002\000\006\005\uffef\011"
					+ "\uffef\001\002\000\006\016\021\017\023\001\002\000\012"
					+ "\002\ufff3\005\ufff3\007\ufff3\011\ufff3\001\002\000\006\005"
					+ "\ufff1\011\ufff1\001\002\000\006\007\040\011\037\001\002"
					+ "\000\006\007\ufff4\011\ufff4\001\002\000\012\002\ufff6\005"
					+ "\ufff6\007\ufff6\011\ufff6\001\002\000\020\004\017\006\016"
					+ "\012\010\013\006\014\004\015\007\016\011\001\002\000"
					+ "\012\002\ufff7\005\ufff7\007\ufff7\011\ufff7\001\002\000\006"
					+ "\007\ufff5\011\ufff5\001\002\000\004\002\001\001\002\000"
					+ "\012\002\ufffb\005\ufffb\007\ufffb\011\ufffb\001\002" });

	/** Access to parse-action table. */
	public short[][] action_table() {
		return _action_table;
	}

	/** <code>reduce_goto</code> table. */
	protected static final short[][] _reduce_table = unpackFromStrings(
			new String[] { "\000\041\000\014\002\004\003\012\004\011\005\014\007"
					+ "\013\001\001\000\002\001\001\000\002\001\001\000\002"
					+ "\001\001\000\002\001\001\000\002\001\001\000\002\001"
					+ "\001\000\002\001\001\000\002\001\001\000\002\001\001"
					+ "\000\002\001\001\000\016\002\034\003\012\004\011\005"
					+ "\014\006\033\007\013\001\001\000\006\010\017\011\021"
					+ "\001\001\000\002\001\001\000\002\001\001\000\002\001"
					+ "\001\000\002\001\001\000\002\001\001\000\014\002\025"
					+ "\003\012\004\011\005\014\007\013\001\001\000\002\001"
					+ "\001\000\014\002\027\003\012\004\011\005\014\007\013"
					+ "\001\001\000\002\001\001\000\004\011\032\001\001\000"
					+ "\002\001\001\000\002\001\001\000\002\001\001\000\002"
					+ "\001\001\000\002\001\001\000\014\002\040\003\012\004"
					+ "\011\005\014\007\013\001\001\000\002\001\001\000\002"
					+ "\001\001\000\002\001\001\000\002\001\001" });

	/** Access to <code>reduce_goto</code> table. */
	public short[][] reduce_table() {
		return _reduce_table;
	}

	/** Instance of action encapsulation class. */
	protected CUP$JavaCupJsonParser$actions action_obj;

	/** Action encapsulation object initializer. */
	protected void init_actions() {
		action_obj = new CUP$JavaCupJsonParser$actions(this);
	}

	/** Invoke a user supplied parse action. */
	public java_cup.runtime.Symbol do_action(int act_num, java_cup.runtime.lr_parser parser, java.util.Stack stack,
			int top) throws java.lang.Exception {
		/* call code in generated class */
		return action_obj.CUP$JavaCupJsonParser$do_action(act_num, parser, stack, top);
	}

	/** Indicates start state. */
	public int start_state() {
		return 0;
	}

	/** Indicates start production. */
	public int start_production() {
		return 0;
	}

	/** <code>EOF</code> Symbol index. */
	public int EOF_sym() {
		return 0;
	}

	/** <code>error</code> Symbol index. */
	public int error_sym() {
		return 1;
	}

	/* Change the method report_error so it will display the line and
	   column of where the error occurred in the input as well as the
	   reason for the error which is passed into the method in the
	   String 'message'. */
	public void report_error(String message, Object info) {

		/* Create a StringBuffer called 'm' with the string 'Error' in it. */
		StringBuilder m = new StringBuilder("Error");

		/* Check if the information passed to the method is the same
		   type as the type java_cup.runtime.Symbol. */
		if (info instanceof java_cup.runtime.Symbol) {
			/* Declare a java_cup.runtime.Symbol object 's' with the
			   information in the object info that is being typecasted
			   as a java_cup.runtime.Symbol object. */
			java_cup.runtime.Symbol s = ((java_cup.runtime.Symbol) info);

			/* Check if the line number in the input is greater or
			   equal to zero. */
			if (s.left >= 0) {
				/* Add to the end of the StringBuffer error message
				   the line number of the error in the input. */
				m.append(" in line " + (s.left + 1));
				/* Check if the column number in the input is greater
				   or equal to zero. */
				if (s.right >= 0)
					/* Add to the end of the StringBuffer error message
					   the column number of the error in the input. */
					m.append(", column " + (s.right + 1));
			}
		}

		/* Add to the end of the StringBuffer error message created in
		   this method the message that was passed into this method. */
		m.append(" : " + message);

		/* Print the contents of the StringBuffer 'm', which contains
		   an error message, out on a line. */
		System.err.println(m);
	}

	/* Change the method report_fatal_error so when it reports a fatal
	   error it will display the line and column number of where the
	   fatal error occurred in the input as well as the reason for the
	   fatal error which is passed into the method in the object
	   'message' and then exit.*/
	public void report_fatal_error(String message, Object info) {
		report_error(message, info);
	}

	/** Cup generated class to encapsulate user supplied action code.*/
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	class CUP$JavaCupJsonParser$actions {
		private final JavaCupJsonParser parser;

		/** Constructor */
		CUP$JavaCupJsonParser$actions(JavaCupJsonParser parser) {
			this.parser = parser;
		}

		/** Method 0 with the actual generated action code for actions 0 to 300. */
		public final java_cup.runtime.Symbol CUP$JavaCupJsonParser$do_action_part00000000(
				int CUP$JavaCupJsonParser$act_num, java_cup.runtime.lr_parser CUP$JavaCupJsonParser$parser,
				java.util.Stack CUP$JavaCupJsonParser$stack, int CUP$JavaCupJsonParser$top) throws java.lang.Exception {
			/* Symbol object for return from actions */
			java_cup.runtime.Symbol CUP$JavaCupJsonParser$result;

			/* select the action based on the action number */
			switch (CUP$JavaCupJsonParser$act_num) {
			/*. . . . . . . . . . . . . . . . . . . .*/
			case 0: // $START ::= json_element EOF 
			{
				Object RESULT = null;
				int start_valleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 1)).left;
				int start_valright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 1)).right;
				Object start_val = (Object) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 1)).value;
				RESULT = start_val;
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("$START", 0,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
								.elementAt(CUP$JavaCupJsonParser$top - 1)),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				/* ACCEPT */
				CUP$JavaCupJsonParser$parser.done_parsing();
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 1: // json_element ::= json_object 
			{
				Object RESULT = null;
				int oleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).left;
				int oright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).right;
				WritableJsonObject o = (WritableJsonObject) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.peek()).value;
				RESULT = o;
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_element", 0,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 2: // json_element ::= json_array 
			{
				Object RESULT = null;
				int aleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).left;
				int aright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).right;
				WritableJsonArray a = (WritableJsonArray) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.peek()).value;
				RESULT = a;
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_element", 0,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 3: // json_element ::= json_native_scalar 
			{
				Object RESULT = null;
				int sleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).left;
				int sright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).right;
				Object s = (Object) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).value;
				RESULT = s;
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_element", 0,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 4: // json_native_scalar ::= json_java_scalar 
			{
				Object RESULT = null;
				int sleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).left;
				int sright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).right;
				Object s = (Object) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).value;
				RESULT = Json.javaToNativeScalar(s);
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_native_scalar", 1,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 5: // json_java_scalar ::= NUMBER 
			{
				Object RESULT = null;
				int nleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).left;
				int nright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).right;
				Object n = (Object) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).value;
				RESULT = n;
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_java_scalar", 2,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 6: // json_java_scalar ::= MINUS NUMBER 
			{
				Object RESULT = null;
				int nleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).left;
				int nright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).right;
				Object n = (Object) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).value;
				RESULT = Numbers.negate(n);
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_java_scalar", 2,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
								.elementAt(CUP$JavaCupJsonParser$top - 1)),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 7: // json_java_scalar ::= STRING 
			{
				Object RESULT = null;
				int sleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).left;
				int sright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).right;
				String s = (String) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).value;
				RESULT = s;
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_java_scalar", 2,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 8: // json_java_scalar ::= BOOLEAN 
			{
				Object RESULT = null;
				int bleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).left;
				int bright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).right;
				Boolean b = (Boolean) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).value;
				RESULT = b;
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_java_scalar", 2,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 9: // json_java_scalar ::= NULL 
			{
				Object RESULT = null;
				RESULT = null;
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_java_scalar", 2,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 10: // json_array ::= LBRACK json_array_body RBRACK 
			{
				WritableJsonArray RESULT = null;
				int aleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 1)).left;
				int aright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 1)).right;
				WritableJsonArray a = (WritableJsonArray) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 1)).value;
				RESULT = a;
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_array", 3,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
								.elementAt(CUP$JavaCupJsonParser$top - 2)),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 11: // json_array ::= LBRACK RBRACK 
			{
				WritableJsonArray RESULT = null;
				RESULT = Json.createArray();
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_array", 3,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
								.elementAt(CUP$JavaCupJsonParser$top - 1)),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 12: // json_array_body ::= json_array_body COMMA json_element 
			{
				WritableJsonArray RESULT = null;
				int aleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 2)).left;
				int aright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 2)).right;
				WritableJsonArray a = (WritableJsonArray) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 2)).value;
				int eleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).left;
				int eright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).right;
				Object e = (Object) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).value;
				RESULT = a.push(e);
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_array_body", 4,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
								.elementAt(CUP$JavaCupJsonParser$top - 2)),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 13: // json_array_body ::= json_element 
			{
				WritableJsonArray RESULT = null;
				int eleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).left;
				int eright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).right;
				Object e = (Object) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).value;
				RESULT = Json.createArray().push(e);
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_array_body", 4,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 14: // json_object ::= LCURLY json_object_body RCURLY 
			{
				WritableJsonObject RESULT = null;
				int oleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 1)).left;
				int oright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 1)).right;
				WritableJsonObject o = (WritableJsonObject) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 1)).value;
				RESULT = o;
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_object", 5,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
								.elementAt(CUP$JavaCupJsonParser$top - 2)),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 15: // json_object ::= LCURLY RCURLY 
			{
				WritableJsonObject RESULT = null;
				RESULT = Json.createObject();
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_object", 5,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
								.elementAt(CUP$JavaCupJsonParser$top - 1)),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 16: // json_object_body ::= json_object_body COMMA json_object_entry 
			{
				WritableJsonObject RESULT = null;
				int oleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 2)).left;
				int oright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 2)).right;
				WritableJsonObject o = (WritableJsonObject) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 2)).value;
				int eleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).left;
				int eright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).right;
				Pair<String, Object> e = (Pair<String, Object>) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.peek()).value;
				RESULT = o.set(e.get1(), e.get2());
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_object_body", 6,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
								.elementAt(CUP$JavaCupJsonParser$top - 2)),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 17: // json_object_body ::= json_object_entry 
			{
				WritableJsonObject RESULT = null;
				int eleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).left;
				int eright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).right;
				Pair<String, Object> e = (Pair<String, Object>) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.peek()).value;
				RESULT = Json.createObject().set(e.get1(), e.get2());
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_object_body", 6,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 18: // json_object_entry ::= STRING COLON json_element 
			{
				Pair<String, Object> RESULT = null;
				int idleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 2)).left;
				int idright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 2)).right;
				String id = (String) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 2)).value;
				int eleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).left;
				int eright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).right;
				Object e = (Object) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).value;
				RESULT = new Pair(id, e);
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_object_entry", 7,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
								.elementAt(CUP$JavaCupJsonParser$top - 2)),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/*. . . . . . . . . . . . . . . . . . . .*/
			case 19: // json_object_entry ::= IDENTIFIER COLON json_element 
			{
				Pair<String, Object> RESULT = null;
				int idleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 2)).left;
				int idright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 2)).right;
				String id = (String) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
						.elementAt(CUP$JavaCupJsonParser$top - 2)).value;
				int eleft = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).left;
				int eright = ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).right;
				Object e = (Object) ((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()).value;
				RESULT = new Pair(id, e);
				CUP$JavaCupJsonParser$result = parser.getSymbolFactory().newSymbol("json_object_entry", 7,
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack
								.elementAt(CUP$JavaCupJsonParser$top - 2)),
						((java_cup.runtime.Symbol) CUP$JavaCupJsonParser$stack.peek()), RESULT);
			}
				return CUP$JavaCupJsonParser$result;

			/* . . . . . .*/
			default:
				throw new Exception(
						"Invalid action number " + CUP$JavaCupJsonParser$act_num + "found in internal parse table");

			}
		} /* end of method */

		/** Method splitting the generated action code into several parts. */
		public final java_cup.runtime.Symbol CUP$JavaCupJsonParser$do_action(int CUP$JavaCupJsonParser$act_num,
				java_cup.runtime.lr_parser CUP$JavaCupJsonParser$parser, java.util.Stack CUP$JavaCupJsonParser$stack,
				int CUP$JavaCupJsonParser$top) throws java.lang.Exception {
			return CUP$JavaCupJsonParser$do_action_part00000000(CUP$JavaCupJsonParser$act_num,
					CUP$JavaCupJsonParser$parser, CUP$JavaCupJsonParser$stack, CUP$JavaCupJsonParser$top);
		}
	}

}
