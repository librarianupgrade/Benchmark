package com.nepxion.discovery.console.desktop.workspace;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import twaver.AlarmSeverity;
import twaver.Element;
import twaver.Generator;
import twaver.TWaverConst;
import twaver.network.ui.ElementUI;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nepxion.cots.twaver.element.TElementManager;
import com.nepxion.cots.twaver.element.TLink;
import com.nepxion.cots.twaver.element.TNode;
import com.nepxion.cots.twaver.icon.TIconFactory;
import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.common.entity.InspectorEntity;
import com.nepxion.discovery.common.entity.InstanceEntityWrapper;
import com.nepxion.discovery.common.entity.PortalType;
import com.nepxion.discovery.common.entity.ServiceType;
import com.nepxion.discovery.common.exception.DiscoveryException;
import com.nepxion.discovery.common.util.StringUtil;
import com.nepxion.discovery.console.cache.ConsoleCache;
import com.nepxion.discovery.console.controller.ConsoleController;
import com.nepxion.discovery.console.desktop.common.icon.ConsoleIconFactory;
import com.nepxion.discovery.console.desktop.common.locale.ConsoleLocaleFactory;
import com.nepxion.discovery.console.desktop.common.util.ButtonUtil;
import com.nepxion.discovery.console.desktop.common.util.ComboBoxUtil;
import com.nepxion.discovery.console.desktop.common.util.DimensionUtil;
import com.nepxion.discovery.console.desktop.workspace.panel.InspectorConditionPanel;
import com.nepxion.discovery.console.desktop.workspace.panel.InspectorConfirmPanel;
import com.nepxion.discovery.console.desktop.workspace.panel.InspectorParameterPanel;
import com.nepxion.discovery.console.desktop.workspace.panel.MultiPreviewPanel;
import com.nepxion.discovery.console.desktop.workspace.topology.LinkUI;
import com.nepxion.discovery.console.desktop.workspace.topology.NodeImageType;
import com.nepxion.discovery.console.desktop.workspace.topology.NodeSizeType;
import com.nepxion.discovery.console.desktop.workspace.topology.NodeUI;
import com.nepxion.discovery.console.desktop.workspace.type.DimensionType;
import com.nepxion.discovery.console.desktop.workspace.type.FeatureType;
import com.nepxion.discovery.console.desktop.workspace.type.ProtocolType;
import com.nepxion.discovery.console.desktop.workspace.type.TypeLocale;
import com.nepxion.discovery.console.entity.Instance;
import com.nepxion.swing.action.JSecurityAction;
import com.nepxion.swing.button.ButtonManager;
import com.nepxion.swing.combobox.JBasicComboBox;
import com.nepxion.swing.element.ElementNode;
import com.nepxion.swing.handle.HandleManager;
import com.nepxion.swing.label.JBasicLabel;
import com.nepxion.swing.layout.table.TableLayout;
import com.nepxion.swing.locale.SwingLocale;
import com.nepxion.swing.optionpane.JBasicOptionPane;
import com.nepxion.swing.shrinkbar.JShrinkShortcut;
import com.nepxion.swing.textfield.JBasicTextField;

public class InspectorTopology extends AbstractTopology {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(InspectorTopology.class);

	protected NodeUI gatewayNodeUI = new NodeUI(NodeImageType.GATEWAY_BLUE, NodeSizeType.LARGE, true);
	protected NodeUI serviceNodeUI = new NodeUI(NodeImageType.SERVICE_BLUE, NodeSizeType.MIDDLE, true);
	protected Color linkUI = LinkUI.BLUE;

	protected JBasicComboBox portalComboBox;
	protected JBasicComboBox protocolComboBox;
	protected JBasicComboBox serviceIdComboBox;
	protected JBasicComboBox instanceComboBox;
	protected InspectorParameterPanel parameterPanel;

	protected InspectorConditionPanel conditionPanel;
	protected MultiPreviewPanel multiPreviewPanel;

	protected JBasicComboBox dimensionComboBox;
	protected JBasicComboBox timesComboBox;
	protected JBasicComboBox concurrencyComboBox;
	protected JProgressBar successfulProgressBar;
	protected JProgressBar failureProgressBar;
	protected JBasicTextField spentTextField;

	protected InspectorConfirmPanel confirmPanel;

	protected ExecutorService executorService;
	protected long currentTime;

	protected Pattern pattern = Pattern.compile("\\[\\S+\\]");

	public InspectorTopology() {
		initializeToolBar();
		initializeOperationBar();
	}

	@Override
	public void initializeTopology() {
		super.initializeTopology();

		graph.setAlarmLabelGenerator(new Generator() {
			public Object generate(Object ui) {
				ElementUI elementUI = (ElementUI) ui;
				Element element = elementUI.getElement();
				if (element instanceof TLink) {
					int alarmCount = element.getAlarmState().getAlarmCount();

					return "<html>" + ConsoleLocaleFactory.getString("times") + "=<font color=red>" + alarmCount
							+ "</font></html>";
				}

				return null;
			}
		});

		background.setTitle(TypeLocale.getDescription(FeatureType.INSPECTOR));
	}

	public void initializeToolBar() {
		JToolBar toolBar = getGraph().getToolbar();
		toolBar.addSeparator();
		toolBar.add(ButtonUtil.createButton(createStartAction()));
		toolBar.add(ButtonUtil.createButton(createStopAction()));
		toolBar.addSeparator();
		toolBar.add(ButtonUtil.createButton(createRefreshServiceListAction()));
		toolBar.add(ButtonUtil.createButton(createViewAction()));
		toolBar.add(ButtonUtil.createButton(createSetAction()));
		toolBar.addSeparator();
		toolBar.add(ButtonUtil.createButton(createLayoutAction()));

		ButtonManager.updateUI(toolBar);
	}

	@Override
	public void initializeOperationBar() {
		JShrinkShortcut portalShrinkShortcut = new JShrinkShortcut();
		portalShrinkShortcut.setTitle(ConsoleLocaleFactory.getString("inspector_portal"));
		portalShrinkShortcut.setIcon(ConsoleIconFactory.getSwingIcon("stereo/paste_16.png"));
		portalShrinkShortcut.setToolTipText(ConsoleLocaleFactory.getString("inspector_portal"));

		List<ElementNode> portalElementNodes = new ArrayList<ElementNode>();
		PortalType[] portalTypes = PortalType.values();
		for (int i = 0; i < portalTypes.length; i++) {
			PortalType portalType = portalTypes[i];
			portalElementNodes.add(new ElementNode(portalType.toString(), TypeLocale.getDescription(portalType), null,
					TypeLocale.getDescription(portalType), portalType));
		}

		portalComboBox = new JBasicComboBox(portalElementNodes.toArray());
		portalComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (portalComboBox.getSelectedItem() != e.getItem()) {
					setServiceIds();
					setInstances();
				}
			}
		});

		protocolComboBox = new JBasicComboBox(ProtocolType.values());

		serviceIdComboBox = new JBasicComboBox();
		serviceIdComboBox.setEditable(true);
		serviceIdComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (serviceIdComboBox.getSelectedItem() != e.getItem()) {
					setInstances();
				}
			}
		});
		ComboBoxUtil.installlAutoCompletion(serviceIdComboBox);

		instanceComboBox = new JBasicComboBox();
		instanceComboBox.setEditable(true);
		ComboBoxUtil.installlAutoCompletion(instanceComboBox);

		setServiceIds();
		setInstances();

		double[][] portalSize = {
				{ TableLayout.PREFERRED, TableLayout.FILL, 5, TableLayout.PREFERRED, TableLayout.FILL },
				{ TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED } };

		TableLayout portalTableLayout = new TableLayout(portalSize);
		portalTableLayout.setHGap(0);
		portalTableLayout.setVGap(5);

		JPanel portalPanel = new JPanel();
		portalPanel.setLayout(portalTableLayout);
		portalPanel.add(DimensionUtil.addWidth(new JBasicLabel(ConsoleLocaleFactory.getString("type")), 5), "0, 0");
		portalPanel.add(portalComboBox, "1, 0");
		portalPanel.add(DimensionUtil.addWidth(new JBasicLabel(ConsoleLocaleFactory.getString("protocol")), 5), "3, 0");
		portalPanel.add(protocolComboBox, "4, 0");
		portalPanel.add(DimensionUtil.addWidth(new JBasicLabel(ConsoleLocaleFactory.getString("service")), 5), "0, 1");
		portalPanel.add(serviceIdComboBox, "1, 1, 4, 1");
		portalPanel.add(DimensionUtil.addWidth(new JBasicLabel(ConsoleLocaleFactory.getString("instance")), 5), "0, 2");
		portalPanel.add(instanceComboBox, "1, 2, 4, 2");

		JShrinkShortcut parameterShrinkShortcut = new JShrinkShortcut();
		parameterShrinkShortcut.setTitle(ConsoleLocaleFactory.getString("inspector_parameter"));
		parameterShrinkShortcut.setIcon(ConsoleIconFactory.getSwingIcon("stereo/paste_16.png"));
		parameterShrinkShortcut.setToolTipText(ConsoleLocaleFactory.getString("inspector_parameter"));

		parameterPanel = new InspectorParameterPanel();

		JShrinkShortcut conditionShrinkShortcut = new JShrinkShortcut();
		conditionShrinkShortcut.setTitle(ConsoleLocaleFactory.getString("inspector_link"));
		conditionShrinkShortcut.setIcon(ConsoleIconFactory.getSwingIcon("stereo/paste_16.png"));
		conditionShrinkShortcut.setToolTipText(ConsoleLocaleFactory.getString("inspector_link"));

		conditionPanel = new InspectorConditionPanel();

		JShrinkShortcut executorShrinkShortcut = new JShrinkShortcut();
		executorShrinkShortcut.setTitle(ConsoleLocaleFactory.getString("inspector_executor"));
		executorShrinkShortcut.setIcon(ConsoleIconFactory.getSwingIcon("stereo/paste_16.png"));
		executorShrinkShortcut.setToolTipText(ConsoleLocaleFactory.getString("inspector_executor"));

		List<ElementNode> dimensionElementNodes = new ArrayList<ElementNode>();
		DimensionType[] dimensionTypes = DimensionType.values();
		for (int i = 0; i < dimensionTypes.length; i++) {
			DimensionType dimensionType = dimensionTypes[i];
			dimensionElementNodes
					.add(new ElementNode(dimensionType.toString(), TypeLocale.getDescription(dimensionType), null,
							TypeLocale.getDescription(dimensionType), dimensionType));
		}

		dimensionComboBox = new JBasicComboBox(dimensionElementNodes.toArray());

		Integer[] times = new Integer[] { 1, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000 };
		timesComboBox = new JBasicComboBox(times);
		timesComboBox.setSelectedIndex(2);

		Integer[] concurrency = new Integer[] { 10, 20, 30, 50, 80, 100, 120, 150, 180, 200, 300, 500 };
		concurrencyComboBox = new JBasicComboBox(concurrency);

		successfulProgressBar = new JProgressBar();
		successfulProgressBar.setStringPainted(true);

		failureProgressBar = new JProgressBar();
		failureProgressBar.setStringPainted(true);

		spentTextField = new JBasicTextField("0");
		spentTextField.setEditable(false);

		double[][] executorSize = { { TableLayout.PREFERRED, TableLayout.FILL, 5, TableLayout.PREFERRED,
				TableLayout.FILL, 5, TableLayout.PREFERRED, TableLayout.FILL },
				{ TableLayout.PREFERRED, TableLayout.PREFERRED } };

		TableLayout executorTableLayout = new TableLayout(executorSize);
		executorTableLayout.setHGap(0);
		executorTableLayout.setVGap(5);

		JPanel executorPanel = new JPanel();
		executorPanel.setLayout(executorTableLayout);
		executorPanel.add(DimensionUtil.addWidth(new JBasicLabel(ConsoleLocaleFactory.getString("dimension")), 5),
				"0, 0");
		executorPanel.add(dimensionComboBox, "1, 0");
		executorPanel.add(DimensionUtil.addWidth(new JBasicLabel(ConsoleLocaleFactory.getString("times")), 5), "3, 0");
		executorPanel.add(timesComboBox, "4, 0");
		executorPanel.add(DimensionUtil.addWidth(new JBasicLabel(ConsoleLocaleFactory.getString("concurrency")), 5),
				"6, 0");
		executorPanel.add(concurrencyComboBox, "7, 0");
		executorPanel.add(DimensionUtil.addWidth(new JBasicLabel(ConsoleLocaleFactory.getString("successful")), 5),
				"0, 1");
		executorPanel.add(DimensionUtil.addHeight(successfulProgressBar, 6), "1, 1");
		executorPanel.add(DimensionUtil.addWidth(new JBasicLabel(ConsoleLocaleFactory.getString("failure")), 5),
				"3, 1");
		executorPanel.add(DimensionUtil.addHeight(failureProgressBar, 6), "4, 1");
		executorPanel.add(DimensionUtil.addWidth(new JBasicLabel(ConsoleLocaleFactory.getString("spent")), 5), "6, 1");
		executorPanel.add(spentTextField, "7, 1");

		double[][] size = { { TableLayout.FILL },
				{ TableLayout.PREFERRED, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.PREFERRED, 10,
						TableLayout.PREFERRED, TableLayout.PREFERRED, 10, TableLayout.PREFERRED,
						TableLayout.PREFERRED } };

		TableLayout tableLayout = new TableLayout(size);
		tableLayout.setHGap(0);
		tableLayout.setVGap(5);

		operationBar.setLayout(tableLayout);
		operationBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		operationBar.add(portalShrinkShortcut, "0, 0");
		operationBar.add(portalPanel, "0, 1");
		operationBar.add(parameterShrinkShortcut, "0, 3");
		operationBar.add(parameterPanel, "0, 4");
		operationBar.add(conditionShrinkShortcut, "0, 6");
		operationBar.add(conditionPanel, "0, 7");
		operationBar.add(executorShrinkShortcut, "0, 9");
		operationBar.add(executorPanel, "0, 10");
	}

	public void setTitle(DimensionType dimensionType, int linkCount) {
		background.setTitle(TypeLocale.getDescription(FeatureType.INSPECTOR) + " | "
				+ ConsoleLocaleFactory.getString(dimensionType.toString() + "_dimension_full") + " | " + linkCount + " "
				+ ConsoleLocaleFactory.getString("link_count"));
	}

	public void setServiceIds() {
		List<String> serviceIds = null;

		ElementNode portalElementNode = (ElementNode) portalComboBox.getSelectedItem();
		PortalType portalType = (PortalType) portalElementNode.getUserObject();
		switch (portalType) {
		case GATEWAY:
			serviceIds = getServiceIds(true);
			break;
		case SERVICE:
			serviceIds = getServiceIds(false);
			break;
		}

		if (serviceIds != null) {
			ComboBoxUtil.setSortableModel(serviceIdComboBox, serviceIds);
		}
	}

	public void setInstances() {
		String serviceId = ComboBoxUtil.getSelectedValue(serviceIdComboBox);

		List<Instance> instances = getInstances(serviceId);
		if (instances != null) {
			List<String> addresses = new ArrayList<String>();
			for (Instance instance : instances) {
				addresses.add(instance.getHost() + ":" + instance.getPort());
			}
			ComboBoxUtil.setSortableModel(instanceComboBox, addresses);
		} else {
			ComboBoxUtil.setSortableModel(instanceComboBox, new ArrayList<String>());
		}
	}

	public List<Map<String, String>> convertToMetadatas(InspectorEntity inspectorEntity) {
		if (inspectorEntity == null) {
			throw new DiscoveryException("Inspector Entity is null");
		}

		String result = inspectorEntity.getResult();
		if (StringUtils.isEmpty(result)) {
			throw new DiscoveryException("Inspector Result is null or empty");
		}

		List<Map<String, String>> metadataList = new ArrayList<Map<String, String>>();

		List<String> expressionList = StringUtil.splitToList(result, " -> ");
		for (String expression : expressionList) {
			expression = StringUtils.replace(expression, "][", "] [");
			Matcher matcher = pattern.matcher(expression);

			Map<String, String> metadataMap = new LinkedHashMap<String, String>();
			while (matcher.find()) {
				String group = matcher.group();
				String value = StringUtils.substringBetween(group, "[", "]");
				String[] expressionArray = StringUtils.split(value, "=");
				metadataMap.put(expressionArray[0], expressionArray[1]);
			}

			metadataList.add(metadataMap);
		}

		return metadataList;
	}

	public TNode addNode(TNode previousNode, DimensionType dimensionType, Map<String, String> metadataMap,
			NodeUI nodeUI, int times) {
		String dimensionKey = dimensionType.getKey();
		String dimensionValue = dimensionType.getValue();

		String serviceId = metadataMap.get("ID");
		String metadata = metadataMap.get(dimensionKey);
		String nodeName = ButtonManager.getHtmlText(serviceId + "\n" + dimensionValue + "=" + metadata);

		TNode node = TElementManager.getNode(dataBox, nodeName);
		if (node == null) {
			node = addNode(nodeName, nodeUI);
			setNodeLabelPosition(node, TWaverConst.POSITION_RIGHT);

			node.setUserObject(metadataMap);
			node.setToolTipText(nodeName);
		}

		if (previousNode != null) {
			TLink link = addLink(previousNode, node, linkUI);
			link.putLabelPosition(TWaverConst.POSITION_HOTSPOT);
			link.getAlarmState().addNewAlarm(AlarmSeverity.WARNING);
			link.putAlarmBalloonOutlineColor(LinkUI.GRAY);

			int counts = 1;
			Object userObject = link.getUserObject();
			if (userObject != null) {
				counts = (int) userObject + 1;
			}

			DecimalFormat format = new DecimalFormat("0.0000");
			double percent = Double.valueOf(format.format((double) counts * 100 / times));

			String text = ConsoleLocaleFactory.getString("percent") + "=" + percent + "%";
			link.setName(text);
			link.setToolTipText(text);
			link.setUserObject(counts);
		}

		return node;
	}

	public class InspectorResult {
		protected List<Map<String, String>> metadatas;
		protected Exception exception;

		public List<Map<String, String>> getMetadatas() {
			return metadatas;
		}

		public void setMetadatas(List<Map<String, String>> metadatas) {
			this.metadatas = metadatas;
		}

		public Exception getException() {
			return exception;
		}

		public void setException(Exception exception) {
			this.exception = exception;
		}
	}

	public class InspectorSwingWorker extends SwingWorker<InspectorResult, Void> {
		protected String url;
		protected Map<String, String> headerMap;
		protected Map<String, String> parameterMap;
		protected Map<String, String> cookieMap;
		protected DimensionType dimensionType;
		protected InspectorEntity inspectorEntity;
		protected int times;

		@Override
		protected InspectorResult doInBackground() throws Exception {
			InspectorResult inspectorResult = new InspectorResult();

			try {
				InspectorEntity resultInspectorEntity = ConsoleController.inspect(url, headerMap, parameterMap,
						cookieMap, inspectorEntity);

				List<Map<String, String>> metadatas = convertToMetadatas(resultInspectorEntity);

				inspectorResult.setMetadatas(metadatas);
			} catch (Exception e) {
				inspectorResult.setException(e);
			}

			return inspectorResult;
		}

		@Override
		protected void done() {
			InspectorResult inspectorResult = null;
			try {
				inspectorResult = get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

			if (inspectorResult.getException() != null) {
				updateFailureProgress();
			} else {
				List<Map<String, String>> metadatas = inspectorResult.getMetadatas();

				TNode node = null;
				for (Map<String, String> metadataMap : metadatas) {
					String serviceType = metadataMap.get("T");

					NodeUI nodeUI = ServiceType.fromString(serviceType) == ServiceType.GATEWAY ? gatewayNodeUI
							: serviceNodeUI;
					node = addNode(node, dimensionType, metadataMap, nodeUI, times);
				}

				updateSuccessfulProgress();
			}

			updateSpent();
		}

		public synchronized void updateSuccessfulProgress() {
			int progress = successfulProgressBar.getModel().getValue() + 1;
			successfulProgressBar.getModel().setValue(progress);
		}

		public synchronized void updateFailureProgress() {
			int progress = failureProgressBar.getModel().getValue() + 1;
			failureProgressBar.getModel().setValue(progress);
		}

		public synchronized void updateSpent() {
			long spentTime = System.currentTimeMillis() - currentTime;
			spentTextField.setText(String.valueOf(spentTime));
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Map<String, String> getHeaderMap() {
			return headerMap;
		}

		public void setHeaderMap(Map<String, String> headerMap) {
			this.headerMap = headerMap;
		}

		public Map<String, String> getParameterMap() {
			return parameterMap;
		}

		public void setParameterMap(Map<String, String> parameterMap) {
			this.parameterMap = parameterMap;
		}

		public Map<String, String> getCookieMap() {
			return cookieMap;
		}

		public void setCookieMap(Map<String, String> cookieMap) {
			this.cookieMap = cookieMap;
		}

		public DimensionType getDimensionType() {
			return dimensionType;
		}

		public void setDimensionType(DimensionType dimensionType) {
			this.dimensionType = dimensionType;
		}

		public InspectorEntity getInspectorEntity() {
			return inspectorEntity;
		}

		public void setInspectorEntity(InspectorEntity inspectorEntity) {
			this.inspectorEntity = inspectorEntity;
		}

		public int getTimes() {
			return times;
		}

		public void setTimes(int times) {
			this.times = times;
		}
	}

	public void start() {
		String url = ComboBoxUtil.getSelectedValue(instanceComboBox);
		if (StringUtils.isBlank(url)) {
			JBasicOptionPane.showMessageDialog(HandleManager.getFrame(this),
					ConsoleLocaleFactory.getString("address_not_null"), SwingLocale.getString("warning"),
					JBasicOptionPane.WARNING_MESSAGE);

			return;
		}

		if (conditionPanel.isServiceIdInvalid()) {
			JBasicOptionPane.showMessageDialog(HandleManager.getFrame(this),
					ConsoleLocaleFactory.getString("service_id_not_null"), SwingLocale.getString("warning"),
					JBasicOptionPane.WARNING_MESSAGE);

			return;
		}

		ElementNode portalElementNode = (ElementNode) portalComboBox.getSelectedItem();
		PortalType portalType = (PortalType) portalElementNode.getUserObject();

		ProtocolType protocolType = (ProtocolType) protocolComboBox.getSelectedItem();

		Map<String, String> headerMap = parameterPanel.getHeaderMap();
		Map<String, String> parameterMap = parameterPanel.getParameterMap();
		Map<String, String> cookieMap = parameterPanel.getCookieMap();

		ElementNode dimensionElementNode = (ElementNode) dimensionComboBox.getSelectedItem();
		DimensionType dimensionType = (DimensionType) dimensionElementNode.getUserObject();

		List<String> allServiceIds = conditionPanel.getServiceIds(true);
		List<String> serviceIds = null;

		if (portalType == PortalType.GATEWAY) {
			String firstServiceId = conditionPanel.getFirstServiceId();

			url = protocolType + url + "/" + firstServiceId + getContextPath(firstServiceId)
					+ DiscoveryConstant.INSPECTOR_ENDPOINT_URL;

			serviceIds = conditionPanel.getServiceIds(false);
		} else {
			String serviceId = ComboBoxUtil.getSelectedValue(serviceIdComboBox);

			url = protocolType + url + getContextPath(serviceId) + DiscoveryConstant.INSPECTOR_ENDPOINT_URL;

			serviceIds = allServiceIds;
		}

		if (confirmPanel == null) {
			confirmPanel = new InspectorConfirmPanel();
		}

		confirmPanel.setUrl(url);
		confirmPanel.setServiceIds(allServiceIds);

		int selectedOption = JBasicOptionPane.showOptionDialog(HandleManager.getFrame(this), confirmPanel,
				ConsoleLocaleFactory.getString("start_inspector_tooltip"), JBasicOptionPane.DEFAULT_OPTION,
				JBasicOptionPane.PLAIN_MESSAGE, ConsoleIconFactory.getSwingIcon("banner/net.png"),
				new Object[] { SwingLocale.getString("confirm"), SwingLocale.getString("cancel") }, null, true);
		if (selectedOption != 0) {
			return;
		}

		url = confirmPanel.getUrl();

		LOG.info("Inspection URL : {}", url);
		LOG.info("Inspection Headers : {}", headerMap);
		LOG.info("Inspection Parameters : {}", parameterMap);
		LOG.info("Inspection Cookies : {}", cookieMap);
		LOG.info("Inspection Services : {}", allServiceIds);

		setTitle(dimensionType, allServiceIds.size() + 1);
		dataBox.clear();

		InspectorEntity inspectorEntity = new InspectorEntity();
		inspectorEntity.setServiceIdList(serviceIds);

		int times = (Integer) timesComboBox.getSelectedItem();
		int concurrency = (Integer) concurrencyComboBox.getSelectedItem();

		DefaultBoundedRangeModel successfulBoundedRangeModel = new DefaultBoundedRangeModel(0, 1, 0, times);
		successfulProgressBar.setModel(successfulBoundedRangeModel);

		DefaultBoundedRangeModel failureBoundedRangeModel = new DefaultBoundedRangeModel(0, 1, 0, times);
		failureProgressBar.setModel(failureBoundedRangeModel);

		spentTextField.setText("0");

		currentTime = System.currentTimeMillis();

		executorService = Executors.newFixedThreadPool(concurrency);
		for (int i = 0; i < times; i++) {
			InspectorSwingWorker inspectorSwingWorker = new InspectorSwingWorker();
			inspectorSwingWorker.setUrl(url);
			inspectorSwingWorker.setHeaderMap(headerMap);
			inspectorSwingWorker.setParameterMap(parameterMap);
			inspectorSwingWorker.setCookieMap(cookieMap);
			inspectorSwingWorker.setDimensionType(dimensionType);
			inspectorSwingWorker.setInspectorEntity(inspectorEntity);
			inspectorSwingWorker.setTimes(times);

			executorService.execute(inspectorSwingWorker);
		}
	}

	public String getContextPath(String serviceId) {
		try {
			Instance instance = ConsoleController.getInstanceList(serviceId).get(0);

			return InstanceEntityWrapper.getFormatContextPath(instance);
		} catch (Exception e) {
			return "/";
		}
	}

	public void stop() {
		if (executorService == null) {
			return;
		}

		int selectedValue = JBasicOptionPane.showConfirmDialog(HandleManager.getFrame(this),
				ConsoleLocaleFactory.getString("stop_confirm"), SwingLocale.getString("confirm"),
				JBasicOptionPane.YES_NO_OPTION);
		if (selectedValue != JBasicOptionPane.OK_OPTION) {
			return;
		}

		executorService.shutdownNow();
	}

	public JSecurityAction createStartAction() {
		JSecurityAction action = new JSecurityAction(ConsoleLocaleFactory.getString("start_text"),
				TIconFactory.getContextIcon("run.png"), ConsoleLocaleFactory.getString("start_inspector_tooltip")) {
			private static final long serialVersionUID = 1L;

			public void execute(ActionEvent e) {
				start();
			}
		};

		return action;
	}

	public JSecurityAction createStopAction() {
		JSecurityAction action = new JSecurityAction(ConsoleLocaleFactory.getString("stop_text"),
				TIconFactory.getContextIcon("stop.png"), ConsoleLocaleFactory.getString("stop_inspector_tooltip")) {
			private static final long serialVersionUID = 1L;

			public void execute(ActionEvent e) {
				stop();
			}
		};

		return action;
	}

	public JSecurityAction createViewAction() {
		JSecurityAction action = new JSecurityAction(ConsoleLocaleFactory.getString("view_text"),
				ConsoleIconFactory.getSwingIcon("ticket.png"), ConsoleLocaleFactory.getString("view_config_tooltip")) {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			public void execute(ActionEvent e) {
				if (multiPreviewPanel == null) {
					multiPreviewPanel = new MultiPreviewPanel();
				}

				TNode node = TElementManager.getSelectedNode(dataBox);
				if (node == null) {
					JBasicOptionPane.showMessageDialog(HandleManager.getFrame(InspectorTopology.this),
							ConsoleLocaleFactory.getString("service_or_gateway_selected"),
							SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

					return;
				}

				Map<String, String> metadataMap = (Map<String, String>) node.getUserObject();
				String group = metadataMap.get("G");
				String serviceId = metadataMap.get("ID");

				String partialConfig = ConsoleController.remoteConfigView(group, serviceId);
				multiPreviewPanel.getPartialPreviewPanel().setKey(ConsoleCache.getKey(group, serviceId));
				multiPreviewPanel.getPartialPreviewPanel().setConfig(partialConfig);

				String globalConfig = ConsoleController.remoteConfigView(group, group);
				multiPreviewPanel.getGlobalPreviewPanel().setKey(ConsoleCache.getKey(group, group));
				multiPreviewPanel.getGlobalPreviewPanel().setConfig(globalConfig);

				JBasicOptionPane.showOptionDialog(HandleManager.getFrame(InspectorTopology.this), multiPreviewPanel,
						ConsoleLocaleFactory.getString("view_config_tooltip"), JBasicOptionPane.DEFAULT_OPTION,
						JBasicOptionPane.PLAIN_MESSAGE, ConsoleIconFactory.getSwingIcon("banner/property.png"),
						new Object[] { SwingLocale.getString("close") }, null, true);
			}
		};

		return action;
	}

	public JSecurityAction createRefreshServiceListAction() {
		JSecurityAction action = new JSecurityAction(ConsoleLocaleFactory.getString("refresh_text"),
				ConsoleIconFactory.getSwingIcon("explorer.png"),
				ConsoleLocaleFactory.getString("refresh_right_service_list_tooltip")) {
			private static final long serialVersionUID = 1L;

			public void execute(ActionEvent e) {
				setServiceIds();
				setInstances();

				conditionPanel.setServiceIds();
			}
		};

		return action;
	}
}