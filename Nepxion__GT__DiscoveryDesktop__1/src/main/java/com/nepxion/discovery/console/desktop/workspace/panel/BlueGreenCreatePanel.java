package com.nepxion.discovery.console.desktop.workspace.panel;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import com.nepxion.discovery.common.entity.BlueGreenRouteType;
import com.nepxion.discovery.console.desktop.common.locale.ConsoleLocaleFactory;
import com.nepxion.discovery.console.desktop.common.util.ButtonUtil;
import com.nepxion.discovery.console.desktop.workspace.type.TypeLocale;
import com.nepxion.swing.label.JBasicLabel;
import com.nepxion.swing.layout.filed.FiledLayout;
import com.nepxion.swing.layout.table.TableLayout;
import com.nepxion.swing.radiobutton.JBasicRadioButton;

public class BlueGreenCreatePanel extends StrategyCreatePanel {
	private static final long serialVersionUID = 1L;

	protected ButtonGroup blueGreenRouteButtonGroup;
	protected JPanel blueGreenRoutePanel;

	public BlueGreenCreatePanel() {
		blueGreenRoutePanel = new JPanel();
		blueGreenRoutePanel.setLayout(new FiledLayout(FiledLayout.ROW, FiledLayout.FULL, 10));
		blueGreenRouteButtonGroup = new ButtonGroup();
		BlueGreenRouteType[] blueGreenRouteTypes = BlueGreenRouteType.values();
		for (int i = 0; i < blueGreenRouteTypes.length; i++) {
			BlueGreenRouteType blueGreenRouteType = blueGreenRouteTypes[i];

			JBasicRadioButton blueGreenRouteRadioButton = new JBasicRadioButton(
					TypeLocale.getDescription(blueGreenRouteType), TypeLocale.getDescription(blueGreenRouteType));
			blueGreenRouteRadioButton.setName(blueGreenRouteType.toString());
			blueGreenRoutePanel.add(blueGreenRouteRadioButton);
			blueGreenRouteButtonGroup.add(blueGreenRouteRadioButton);

			if (i == 0) {
				blueGreenRouteRadioButton.setSelected(true);
			}
		}

		add(new JBasicLabel(ConsoleLocaleFactory.getString("route_text")), "0, 10");
		add(blueGreenRoutePanel, "1, 10");
	}

	@Override
	public double[] getLayoutRow() {
		return new double[] { TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
				TableLayout.PREFERRED, 0, TableLayout.PREFERRED, TableLayout.PREFERRED, 0, TableLayout.PREFERRED,
				TableLayout.PREFERRED, TableLayout.PREFERRED };
	}

	public BlueGreenRouteType getBlueGreenRouteType() {
		String rationButtonName = ButtonUtil.getRationButtonName(blueGreenRouteButtonGroup);

		return BlueGreenRouteType.fromString(rationButtonName);
	}
}