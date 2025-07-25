package webfx.kit.mapper.peers.javafxgraphics.gwt.shared;

import com.google.gwt.core.client.JavaScriptObject;
import com.sun.javafx.cursor.CursorType;
import com.sun.javafx.event.EventUtil;
import elemental2.dom.*;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Effect;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.transform.Transform;
import webfx.kit.mapper.peers.javafxgraphics.NodePeer;
import webfx.kit.mapper.peers.javafxgraphics.SceneRequester;
import webfx.kit.mapper.peers.javafxgraphics.base.NodePeerBase;
import webfx.kit.mapper.peers.javafxgraphics.base.NodePeerImpl;
import webfx.kit.mapper.peers.javafxgraphics.base.NodePeerMixin;
import webfx.kit.mapper.peers.javafxgraphics.gwt.svg.SvgNodePeer;
import webfx.kit.mapper.peers.javafxgraphics.gwt.util.*;
import webfx.kit.mapper.peers.javafxgraphics.emul_coupling.LayoutMeasurable;
import webfx.platform.shared.util.Booleans;
import webfx.platform.shared.util.Strings;
import webfx.platform.shared.util.collection.Collections;

import java.util.List;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public abstract class HtmlSvgNodePeer<E extends Element, N extends Node, NB extends NodePeerBase<N, NB, NM>, NM extends NodePeerMixin<N, NB, NM>>
		extends NodePeerImpl<N, NB, NM> {

	private final E element;
	private Element container;
	private Element childrenContainer;
	protected DomType containerType;

	public HtmlSvgNodePeer(NB base, E element) {
		super(base);
		this.element = element;
		setContainer(element);
		setChildrenContainer(element);
	}

	public E getElement() {
		return element;
	}

	public void setContainer(Element container) {
		this.container = container;
		containerType = "SVG".equalsIgnoreCase(container.tagName) || this instanceof SvgNodePeer ? DomType.SVG
				: DomType.HTML;
		storePeerInElement(container);
	}

	protected void storePeerInElement(Object element) {
		HtmlUtil.setJsJavaObjectAttribute((JavaScriptObject) element, "nodePeer", this);
	}

	protected static NodePeer getPeerFromElement(Object element) {
		return element == null ? null
				: (NodePeer) HtmlUtil.getJsJavaObjectAttribute((JavaScriptObject) element, "nodePeer");
	}

	public static NodePeer getPeerFromElementOrParents(Element element) {
		NodePeer nodePeer = null;
		for (elemental2.dom.Node n = element; nodePeer == null && n != null; n = n.parentNode)
			nodePeer = getPeerFromElement(n);
		if (nodePeer != null) {
			Node node = nodePeer.getNode();
			while (node != null) {
				javafx.scene.Parent parent = node.getParent();
				if (parent == null && node.getScene().getRoot() != node)
					return getPeerFromElementOrParents(
							(Element) ((HtmlSvgNodePeer) node.getNodePeer()).element.parentNode);
				node = parent;
			}
		}
		return nodePeer;
	}

	public Element getContainer() {
		return container;
	}

	public Element getChildrenContainer() {
		return childrenContainer;
	}

	public void setChildrenContainer(Element childrenContainer) {
		this.childrenContainer = childrenContainer;
	}

	@Override
	public void bind(N node, SceneRequester sceneRequester) {
		super.bind(node, sceneRequester);
		installFocusListener();
		installKeyboardListeners();
	}

	/******************* Drag & drop support *********************/

	private EventListener dragStartListener;

	@Override
	public void updateOnDragDetected(EventHandler<? super javafx.scene.input.MouseEvent> eventHandler) {
		setElementAttribute("draggable", eventHandler == null ? null : "true");
		dragStartListener = updateHtmlEventListener(dragStartListener, "dragstart", eventHandler, evt -> {
			MouseEvent me = (MouseEvent) evt;
			DragboardDataTransferHolder.setDragboardDataTransfer(me.dataTransfer);
			javafx.scene.input.MouseEvent fxMouseEvent = FxEvents.toFxMouseEvent(me, "mousemove");
			eventHandler.handle(fxMouseEvent);
			if (fxMouseEvent.isConsumed())
				evt.stopPropagation();
			// If no drag done handler has been registered, registering a dummy one in order to detect when drag ends
			if (dragEndListener == null)
				updateOnDragDone(e -> {
				}); // This is to ensure that clearDndGesture() will be called in callFxDragEventHandler()
		});
	}

	private EventListener dragEnterListener;

	@Override
	public void updateOnDragEntered(EventHandler<? super DragEvent> eventHandler) {
		dragEnterListener = updateHtmlDragEventListener(dragEnterListener, "dragenter", DragEvent.DRAG_ENTERED,
				eventHandler);
	}

	private EventListener dragOverListener;

	@Override
	public void updateOnDragOver(EventHandler<? super DragEvent> eventHandler) {
		dragOverListener = updateHtmlEventListener(dragOverListener, "dragover", eventHandler,
				evt -> callFxDragEventHandler(evt, DragEvent.DRAG_OVER, dragEvent -> {
					eventHandler.handle(dragEvent);
					if (dragEvent.isAccepted())
						evt.preventDefault();
				}));
	}

	private EventListener dropListener;

	@Override
	public void updateOnDragDropped(EventHandler<? super DragEvent> eventHandler) {
		dropListener = updateHtmlEventListener(dropListener, "drop", eventHandler, evt -> {
			evt.preventDefault();
			callFxDragEventHandler(evt, DragEvent.DRAG_DROPPED, eventHandler);
			// Also simulating JavaFx behavior which calls drag exit after drop
			if (dragLeaveListener != null)
				dragLeaveListener.handleEvent(evt);
		});
	}

	private EventListener dragLeaveListener;

	@Override
	public void updateOnDragExited(EventHandler<? super DragEvent> eventHandler) {
		dragLeaveListener = updateHtmlDragEventListener(dragLeaveListener, "dragleave", DragEvent.DRAG_EXITED,
				eventHandler);
	}

	private EventListener dragEndListener;

	@Override
	public void updateOnDragDone(EventHandler<? super DragEvent> eventHandler) {
		dragEndListener = updateHtmlDragEventListener(dragEndListener, "dragend", DragEvent.DRAG_DONE, eventHandler);
	}

	private EventListener updateHtmlEventListener(EventListener previousListener, String type, Object eventHandler,
			EventListener newListener) {
		element.removeEventListener(type, previousListener);
		if (eventHandler == null)
			return null;
		element.addEventListener(type, newListener);
		return newListener;
	}

	private EventListener updateHtmlDragEventListener(EventListener previousListener, String type,
			EventType<DragEvent> dragEventType, EventHandler<? super DragEvent> eventHandler) {
		return updateHtmlEventListener(previousListener, type, eventHandler,
				evt -> callFxDragEventHandler(evt, dragEventType, eventHandler));
	}

	private void callFxDragEventHandler(Event evt, EventType<DragEvent> dragEventType,
			EventHandler<? super DragEvent> eventHandler) {
		MouseEvent me = (MouseEvent) evt;
		// HTML also triggers dragenter and dragleave events on children as opposed to JavaFx so we just ignore them
		if (!element.contains((elemental2.dom.Node) me.relatedTarget)) { // thanks to this condition
			// Otherwise we call the JavaFx handler
			DragboardDataTransferHolder.setDragboardDataTransfer(me.dataTransfer);
			DragEvent dragEvent = FxEvents.toDragEvent(me, dragEventType, getNode());
			eventHandler.handle(dragEvent);
			if (dragEvent.isConsumed())
				evt.stopPropagation();
			// Clearing DndGesture to ensure that if a new drag starts (especially from an external application), we don't keep wrong information (such as previous gestureSource)
			if (dragEventType == DragEvent.DRAG_DONE)
				getNode().getScene().clearDndGesture();
		}
	}

	/*********** End of "Drag & drop support" section ************/

	private boolean passOnToFx(javafx.event.Event fxEvent) {
		return isFxEventConsumed(EventUtil.fireEvent(getNode(), fxEvent));
	}

	private boolean isFxEventConsumed(javafx.event.Event fxEvent) {
		return fxEvent != null && fxEvent.isConsumed();
	}

	private void installFocusListener() {
		element.onfocus = e -> {
			passHtmlFocusEventOnToFx(e);
			return null;
		};
	}

	protected void passHtmlFocusEventOnToFx(Event e) {
		for (Node node = getNode(); node != null; node = node.getParent()) {
			if (node.isFocusTraversable()) {
				node.getScene().focusOwnerProperty().setValue(node);
				break;
			}
		}
	}

	private void installKeyboardListeners() {
		registerKeyboardListener("keydown");
		registerKeyboardListener("keyup");
		registerKeyboardListener("keypress");
	}

	private void registerKeyboardListener(String type) {
		element.addEventListener(type, e -> {
			boolean fxConsumed = passHtmlKeyEventOnToFx((KeyboardEvent) e, type);
			if (fxConsumed) {
				e.stopPropagation();
				e.preventDefault();
			}
		});
	}

	protected boolean passHtmlKeyEventOnToFx(KeyboardEvent e, String type) {
		return passOnToFx(toFxKeyEvent(e, type));
	}

	@Override
	public void requestFocus() {
		getFocusElement().focus();
	}

	protected Element getFocusElement() {
		return getElement();
	}

	@Override
	public void updateLayoutX(Number layoutX) {
		updateLocalToParentTransforms(getNodePeerBase().getNode().localToParentTransforms());
	}

	@Override
	public void updateLayoutY(Number layoutY) {
		updateLocalToParentTransforms(getNodePeerBase().getNode().localToParentTransforms());
	}

	@Override
	public void updateTransforms(List<Transform> transforms, ListChangeListener.Change<Transform> change) {
		updateLocalToParentTransforms(getNodePeerBase().getNode().localToParentTransforms());
	}

	@Override
	public boolean isTreeVisible() {
		if (container instanceof HTMLElement)
			return ((HTMLElement) container).offsetParent != null;
		return true;
	}

	protected boolean isStyleAttribute(String name) {
		if (containerType == DomType.HTML)
			switch (name) {
			case "pointer-events":
			case "visibility":
			case "opacity":
			case "clip-path":
			case "mix-blend-mode":
			case "filter":
			case "font-family":
			case "font-style":
			case "font-weight":
			case "font-size":
			case "transform":
				return true;
			}
		return false;
	}

	protected void setElementStyleAttribute(String name, Object value) {
		HtmlUtil.setStyleAttribute(container, name, value);
	}

	@Override
	public void updateMouseTransparent(Boolean mouseTransparent) {
		setElementAttribute("pointer-events", mouseTransparent ? "none" : null);
	}

	@Override
	public void updateId(String id) {
		setElementAttribute("id", id);
	}

	@Override
	public void updateVisible(Boolean visible) {
		setElementAttribute("visibility", visible ? null : "hidden");
	}

	@Override
	public void updateOpacity(Double opacity) {
		setElementAttribute("opacity", opacity == 1d ? null : opacity);
	}

	@Override
	public void updateDisabled(Boolean disabled) {
		setElementAttribute(getElement(), "disabled", Booleans.isTrue(disabled) ? "disabled" : null);
	}

	@Override
	public void updateClip(Node clip) {
		setElementAttribute("clip-path", toClipPath(clip));
	}

	@Override
	public void updateCursor(Cursor cursor) {
		setElementStyleAttribute("cursor", toCssCursor(cursor));
	}

	protected abstract String toClipPath(Node clip);

	@Override
	public void updateBlendMode(BlendMode blendMode) {
		setElementStyleAttribute("mix-blend-mode", toSvgBlendMode(blendMode));
	}

	@Override
	public void updateEffect(Effect effect) {
		setElementAttribute("filter", effect == null ? null : toFilter(effect));
	}

	protected abstract String toFilter(Effect effect);

	@Override
	public void updateLocalToParentTransforms(List<Transform> localToParentTransforms) {
		boolean isSvg = containerType == DomType.SVG;
		setElementAttribute("transform", isSvg ? SvgTransforms.toSvgTransforms(localToParentTransforms)
				: HtmlTransforms.toHtmlTransforms(localToParentTransforms));
	}

	@Override
	public void updateStyleClass(List<String> styleClass, ListChangeListener.Change<String> change) {
		if (change == null)
			addToElementClassList(styleClass);
		else
			while (change.next()) {
				if (change.wasRemoved())
					removeFromElementClassList(change.getRemoved());
				if (change.wasAdded())
					addToElementClassList(change.getAddedSubList());
			}
	}

	private void addToElementClassList(List<String> styleClass) {
		if (!Collections.isEmpty(styleClass))
			element.classList.add(Collections.toArray(styleClass, String[]::new));
	}

	private void removeFromElementClassList(List<String> styleClass) {
		if (!Collections.isEmpty(styleClass))
			element.classList.remove(Collections.toArray(styleClass, String[]::new));
	}

	private static KeyEvent toFxKeyEvent(KeyboardEvent e, String type) {
		KeyCode keyCode = toFxKeyCode(e.key); // e.key = physical key, e.code = logical key (ie taking into account selected system keyboard)
		EventType<KeyEvent> eventType;
		if (keyCode == KeyCode.ESCAPE)
			eventType = KeyEvent.KEY_PRESSED;
		else
			switch (type) {
			case "keydown":
				eventType = KeyEvent.KEY_TYPED;
				break;
			case "keyup":
				eventType = KeyEvent.KEY_RELEASED;
				break;
			default:
				eventType = KeyEvent.KEY_PRESSED;
			}
		return new KeyEvent(eventType, e.char_, e.keyIdentifier, keyCode, e.shiftKey, e.ctrlKey, e.altKey, e.metaKey);
	}

	private static KeyCode toFxKeyCode(String htmlKey) {
		if (htmlKey == null)
			return KeyCode.UNDEFINED;
		// See https://developer.mozilla.org/fr/docs/Web/API/KeyboardEvent/code
		switch (htmlKey) {
		case "Escape":
			return KeyCode.ESCAPE; // 0x0001
		case "Minus":
			return KeyCode.MINUS; // 0x000C
		case "Equal":
			return KeyCode.EQUALS; // 0x000D
		case "Backspace":
			return KeyCode.BACK_SPACE; // 0x000E
		case "Tab":
			return KeyCode.TAB; // 0x000F
		// KeyQ (0x0010) to KeyP (0x0019) -> default
		case "BracketLeft":
			return KeyCode.OPEN_BRACKET; // 0x001A
		case "BracketRight":
			return KeyCode.CLOSE_BRACKET; // 0x001B
		case "Enter":
			return KeyCode.ENTER; // 0x001C
		case "ControlLeft":
			return KeyCode.CONTROL; // 0x001D
		// KeyA (0x001E) to KeyL (0x0026) -> default
		case "Semicolon":
			return KeyCode.SEMICOLON; // 0x0027
		case "Quote":
			return KeyCode.QUOTE; // 0x0028
		case "Backquote":
			return KeyCode.BACK_QUOTE; // 0x0029
		case "ShiftLeft":
			return KeyCode.SHIFT; // 0x002A
		case "Backslash":
			return KeyCode.BACK_SLASH; // 0x002B
		// KeyZ (0x002C) to KeyM (0x0032) -> default
		case "Comma":
			return KeyCode.COMMA; // 0x0033
		case "Period":
			return KeyCode.PERIOD; // 0x0034
		case "Slash":
			return KeyCode.SLASH; // 0x0035
		case "ShiftRight":
			return KeyCode.SHIFT; // 0x0036
		case "NumpadMultiply":
			return KeyCode.MULTIPLY; // 0x0037
		case "AltLeft":
			return KeyCode.ALT; // 0x0038
		case "Space":
			return KeyCode.SPACE; // 0x0039
		case "CapsLock":
			return KeyCode.CAPS; // 0x003A
		// F1 (0x003B) to F10 (0x0044) -> default
		case "Pause":
			return KeyCode.PAUSE; // 0x0045
		case "ScrollLock":
			return KeyCode.SCROLL_LOCK; // 0x0046
		// Numpad7 (0x0047) to Numpad9 (0x0049) -> default
		case "NumpadSubtract":
			return KeyCode.SUBTRACT; // 0x004A
		// Numpad4 (0x004B) to Numpad6 (0x004D) -> default
		case "NumpadAdd":
			return KeyCode.ADD; // 0x004E
		// Numpad1 (0x004F) to Numpad0 (0x0052) -> default
		case "NumpadDecimal":
			return KeyCode.DECIMAL; // 0x0053
		case "PrintScreen":
			return KeyCode.PRINTSCREEN; // 0x0054
		case "IntlBackslash":
			return KeyCode.BACK_SLASH; // 0x0056
		// F11 (0x0057) to F12 (0x0058) -> default
		case "NumpadEqual":
			return KeyCode.EQUALS; // 0x0059
		// F13 (0x0057) to F23 (0x006E) -> default
		case "KanaMode":
			return KeyCode.KANA; // 0x0070
		case "Convert":
			return KeyCode.CONVERT; // 0x0079
		case "NonConvert":
			return KeyCode.NONCONVERT; // 0x007B
		case "NumpadComma":
			return KeyCode.COMMA; // 0x007E
		case "Paste":
			return KeyCode.PASTE; // 0xE00A
		case "MediaTrackPrevious":
			return KeyCode.TRACK_PREV; // 0xE010
		case "Cut":
			return KeyCode.CUT; // 0xE018
		case "Copy":
			return KeyCode.COPY; // 0xE018
		case "MediaTrackNext":
			return KeyCode.TRACK_NEXT; // 0xE019
		case "NumpadEnter":
			return KeyCode.ENTER; // 0xE01C
		case "ControlRight":
			return KeyCode.CONTROL; // 0xE01D
		case "VolumeMute":
		case "AudioVolumeMute":
			return KeyCode.MUTE; // 0xE020
		case "MediaPlayPause":
			return KeyCode.PAUSE; // 0xE022
		case "MediaStop":
			return KeyCode.STOP; // 0xE024
		case "Eject":
			return KeyCode.EJECT_TOGGLE; // 0xE02D
		case "VolumeDown":
		case "AudioVolumeDown":
			return KeyCode.VOLUME_DOWN; // 0xE02E
		case "VolumeUp":
		case "AudioVolumeUp":
			return KeyCode.VOLUME_UP; // 0xE030
		case "BrowserHome":
			return KeyCode.HOME; // 0xE032
		case "NumpadDivide":
			return KeyCode.DIVIDE; // 0xE035
		case "AltRight":
			return KeyCode.ALT_GRAPH; // 0xE038
		case "Help":
			return KeyCode.HELP; // 0xE03B
		case "NumLock":
			return KeyCode.NUM_LOCK; // 0xE045
		case "Home":
			return KeyCode.HOME; // 0xE047
		case "ArrowUp":
			return KeyCode.UP; // 0xE048
		case "PageUp":
			return KeyCode.PAGE_UP; // 0xE049
		case "ArrowLeft":
			return KeyCode.LEFT; // 0xE04B
		case "ArrowRight":
			return KeyCode.RIGHT; // 0xE04D
		case "End":
			return KeyCode.END; // 0xE04F
		case "ArrowDown":
			return KeyCode.DOWN; // 0xE050
		case "PageDown":
			return KeyCode.PAGE_DOWN; // 0xE051
		case "Insert":
			return KeyCode.INSERT; // 0xE052
		case "Delete":
			return KeyCode.DELETE; // 0xE053
		case "OSLeft":
		case "MetaLeft":
			return KeyCode.META; // 0xE05B
		case "OSRight":
		case "MetaRight":
			return KeyCode.META; // 0xE05C
		case "ContextMenu":
			return KeyCode.CONTEXT_MENU; // 0xE05D
		case "Power":
			return KeyCode.POWER; // 0xE05E
		default: {
			String fxKeyName = htmlKey;
			int length = htmlKey.length();
			if (length == 6 && htmlKey.startsWith("Digit")) // Digit0 to Digit9
				fxKeyName = String.valueOf(htmlKey.charAt(5)); // -> 0 to 9
			else if (length == 4 && htmlKey.startsWith("Key")) // KeyQ, etc...
				fxKeyName = String.valueOf(htmlKey.charAt(3)); // -> Q, etc...
			else if (htmlKey.startsWith("Numpad"))
				fxKeyName = Strings.replaceAll(htmlKey, "Numpad", "Numpad ");
			KeyCode keyCode = KeyCode.getKeyCode(fxKeyName);
			if (keyCode == null)
				keyCode = KeyCode.getKeyCode(fxKeyName.toUpperCase());
			if (keyCode == null)
				keyCode = KeyCode.UNDEFINED;
			return keyCode;
		}
		}
	}

	protected void setElementTextContent(String textContent) {
		String text = Strings.toSafeString(textContent);
		if (!Objects.equals(element.textContent, text)) {
			element.textContent = text; // Using a safe string to avoid "undefined" with IE
			clearLayoutCache();
		}
	}

	protected void clearLayoutCache() {
		if (this instanceof LayoutMeasurable)
			((LayoutMeasurable) this).clearCache();
	}

	/* String attributes */

	protected void setElementAttribute(String name, String value, String skipValue) {
		if (skipValue != null && Objects.equals(value, skipValue))
			value = null;
		setElementAttribute(name, value);
	}

	protected void setElementAttribute(String name, String value) {
		if (isStyleAttribute(name))
			setElementStyleAttribute(name, value);
		else
			setElementAttribute(container, name, value);
	}

	protected static void setElementAttribute(Element e, String name, String value) {
		if (value == null)
			e.removeAttribute(name);
		else
			e.setAttribute(name, value);
	}

	/* Double attributes */

	protected void setElementAttribute(String name, Number value, Number skipValue) {
		if (skipValue != null && Objects.equals(value, skipValue))
			value = null;
		setElementAttribute(name, value);
	}

	protected void setElementAttribute(String name, Number value) {
		if (container == element && isStyleAttribute(name))
			setElementStyleAttribute(name, value);
		else
			setElementAttribute(container, name, value);
	}

	private void setElementAttribute(Element e, String name, Number value) {
		if (value == null)
			e.removeAttribute(name);
		else
			e.setAttribute(name, value.doubleValue());
	}

	protected void setFontAttributes(Font font) {
		HtmlFonts.setHtmlFontStyleAttributes(font, container);
	}

	private static String toSvgBlendMode(BlendMode blendMode) {
		// JavaFx use the same names as SVG for ADD, MULTIPLY, SCREEN, OVERLAY, DARKEN, COLOR_DODGE, COLOR_BURN, HARD_LIGHT, SOFT_LIGHT, DIFFERENCE, EXCLUSION
		// SVG doesn't support (so far): SRC_OVER, SRC_ATOP, RED, GREEN, BLUE but we return them as is just in case it is supported in the future
		return blendMode == null ? null : enumNameToCss(blendMode.name());
	}

	private static String enumNameToCss(String enumName) {
		return enumName.toLowerCase().replace('_', '-');
	}

	private static String toCssCursor(Cursor cursor) {
		if (cursor == null)
			return null;
		CursorType cursorType = cursor.getCurrentFrame().getCursorType();
		switch (cursorType) {
		// Starting with differences of names between JavaFx and CSS:
		case HAND:
			return "pointer";
		case OPEN_HAND:
			return "grab";
		case CLOSED_HAND:
			return "grabbing";
		case H_RESIZE:
			return "ew-resize";
		case V_RESIZE:
			return "ns-resize";
		case IMAGE: // TODO: extract url from ImageCursorFrame
		case DISAPPEAR: // What cursor is that ???
		case NONE:
			return "none";
		// Then all other cursors have the same name: DEFAULT, CROSSHAIR, TEXT, WAIT, SW_RESIZE, SE_RESIZE, NW_RESIZE, NE_RESIZE, N_RESIZE, S_RESIZE, W_RESIZE, E_RESIZE, MOVE
		default:
			return enumNameToCss(cursorType.name());
		}
	}

	static String toPx(double position) {
		return toPixel(position) + "px";
	}

	static long toPixel(double position) {
		return Math.round(position);
	}

	public static HtmlSvgNodePeer toNodePeer(Node node, Scene scene) {
		node.setScene(scene);
		return (HtmlSvgNodePeer) node.getOrCreateAndBindNodePeer();
	}

	public static Element toContainerElement(Node node, Scene scene) {
		return node == null ? null : toNodePeer(node, scene).getContainer();
	}
}
