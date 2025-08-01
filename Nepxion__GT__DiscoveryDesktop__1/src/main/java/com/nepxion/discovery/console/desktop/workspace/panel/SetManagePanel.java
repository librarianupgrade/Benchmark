package com.nepxion.discovery.console.desktop.workspace.panel;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.nepxion.discovery.console.desktop.common.icon.ConsoleIconFactory;
import com.nepxion.discovery.console.desktop.workspace.type.SetType;
import com.nepxion.discovery.console.desktop.workspace.type.TypeLocale;
import com.nepxion.swing.button.JBasicToggleButton;
import com.nepxion.swing.buttonbar.JBasicButtonBar;
import com.nepxion.swing.container.ContainerManager;
import com.nepxion.swing.scrollpane.JBasicScrollPane;
import com.nepxion.swing.style.button.lite.JGlassLiteButtonStyle;
import com.nepxion.swing.style.button.lite.LiteButtonUI;
import com.nepxion.swing.style.buttonbar.ButtonBarUI;

public class SetManagePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	protected static SetManagePanel setManagePanel;

	public static SetManagePanel getInstance() {
		if (setManagePanel == null) {
			setManagePanel = new SetManagePanel();
		}

		return setManagePanel;
	}

	private CacheSetPanel cacheSetPanel;
	private LayouterSetPanel layouterSetPanel;

	private SetManagePanel() {
		cacheSetPanel = new CacheSetPanel();
		layouterSetPanel = new LayouterSetPanel();

		List<SetPanel> setPanelList = new ArrayList<SetPanel>();
		setPanelList.add(cacheSetPanel);
		setPanelList.add(layouterSetPanel);

		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());

		JBasicButtonBar buttonBar = new JBasicButtonBar(JBasicButtonBar.VERTICAL);
		buttonBar.setUI(new ButtonBarUI(new LiteButtonUI(new JGlassLiteButtonStyle())));

		ButtonGroup buttonGroup = new ButtonGroup();

		int index = 0;
		for (SetType setType : SetType.values()) {
			JBasicToggleButton toggleButton = new JBasicToggleButton(TypeLocale.getDescription(setType),
					ConsoleIconFactory.getSwingIcon(setType.getIcon()), TypeLocale.getDescription(setType));
			toggleButton.setName(setType.toString());
			toggleButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (toggleButton.isSelected()) {
						container.removeAll();

						for (SetPanel setPanel : setPanelList) {
							if (setPanel.getSetType() == SetType.fromString(toggleButton.getName())) {
								container.add(setPanel, BorderLayout.CENTER);
								ContainerManager.update(container);

								break;
							}
						}
					}
				}
			});

			buttonBar.add(toggleButton);
			buttonGroup.add(toggleButton);

			if (index == 0) {
				toggleButton.setSelected(true);
			}

			index++;
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		buttonPanel.add(new JBasicScrollPane(buttonBar), BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerLocation(120);
		splitPane.setLeftComponent(buttonPanel);
		splitPane.setRightComponent(container);

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(600, 500));
		add(splitPane, BorderLayout.CENTER);
	}

	public CacheSetPanel getCacheSetPanel() {
		return cacheSetPanel;
	}

	public LayouterSetPanel getLayouterSetPanel() {
		return layouterSetPanel;
	}
}