package edu.jiangxin.apktoolbox.swing.extend;

import edu.jiangxin.apktoolbox.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileListPanel extends JPanel {

	private static final Logger LOGGER = LogManager.getLogger(FileListPanel.class.getSimpleName());

	private JPanel leftPanel;

	private JPanel rightPanel;

	private JList<File> fileList;

	private DefaultListModel<File> fileListModel;

	public FileListPanel() {
		super();
		initUI();
	}

	public List<File> getFileList() {
		List<File> fileList = new ArrayList<>();
		Object[] objectArray = fileListModel.toArray();
		for (Object obj : objectArray) {
			if (obj instanceof File) {
				fileList.add((File) obj);
			}
		}
		return fileList;
	}

	private void initUI() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		createLeftPanel();
		add(leftPanel);

		add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));

		createRightPanel();
		add(rightPanel);
	}

	private void createLeftPanel() {
		fileList = new JList<>();
		fileListModel = new DefaultListModel<>();
		fileList.setModel(fileListModel);

		JScrollPane scrollPane = new JScrollPane(fileList);
		scrollPane.setPreferredSize(
				new Dimension(Constants.DEFAULT_SCROLL_PANEL_WIDTH, Constants.DEFAULT_SCROLL_PANEL_HEIGHT));

		leftPanel = new JPanel();
		leftPanel.setBorder(BorderFactory.createTitledBorder("File List"));
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setTransferHandler(new FileListTransferHandler());
		leftPanel.add(scrollPane);
	}

	private void createRightPanel() {
		rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

		JPanel rightContentPanel = new JPanel();
		rightPanel.add(Box.createVerticalGlue());
		rightPanel.add(rightContentPanel);
		rightPanel.add(Box.createVerticalGlue());

		rightContentPanel.setLayout(new GridLayout(6, 1, 0, Constants.DEFAULT_Y_BORDER));

		JButton addFileButton = new JButton("Add File");
		addFileButton.addMouseListener(new AddFileButtonMouseAdapter());
		rightContentPanel.add(addFileButton);

		JButton addDirectoryButton = new JButton("Add Directory");
		addDirectoryButton.addMouseListener(new AddDirectoryButtonMouseAdapter());
		rightContentPanel.add(addDirectoryButton);

		JButton removeSelectedButton = new JButton("Remove Selected");
		removeSelectedButton.addMouseListener(new RemoveSelectedButtonMouseAdapter());
		rightContentPanel.add(removeSelectedButton);

		JButton clearButton = new JButton("Clear All");
		clearButton.addMouseListener(new ClearButtonMouseAdapter());
		rightContentPanel.add(clearButton);

		JButton selectAllButton = new JButton("Select All");
		selectAllButton.addMouseListener(new SelectAllButtonMouseAdapter());
		rightContentPanel.add(selectAllButton);

		JButton inverseSelectedButton = new JButton("Inverse Selected");
		inverseSelectedButton.addMouseListener(new InverseSelectedButtonMouseAdapter());
		rightContentPanel.add(inverseSelectedButton);

		for (Component component : rightContentPanel.getComponents()) {
			component.setPreferredSize(new Dimension(Constants.DEFAULT_BUTTON_WIDTH, Constants.DEFAULT_BUTTON_HEIGHT));
		}
	}

	private final class FileListTransferHandler extends TransferHandler {
		@Override
		public boolean importData(JComponent comp, Transferable t) {
			try {
				Object object = t.getTransferData(DataFlavor.javaFileListFlavor);
				for (File file : (List<File>) object) {
					fileListModel.addElement(file);
				}
				return true;
			} catch (IOException e) {
				LOGGER.error("importData failed: IOException");
			} catch (UnsupportedFlavorException e) {
				LOGGER.error("importData failed: UnsupportedFlavorException");
			}
			return false;
		}

		@Override
		public boolean canImport(JComponent jComponent, DataFlavor[] dataFlavors) {
			for (DataFlavor dataFlavor : dataFlavors) {
				if (DataFlavor.javaFileListFlavor.equals(dataFlavor)) {
					return true;
				}
			}
			return false;
		}
	}

	private final class AddFileButtonMouseAdapter extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			JFileChooser jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.setDialogTitle("Select A File");
			int ret = jfc.showDialog(new JLabel(), null);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = jfc.getSelectedFile();
				fileListModel.addElement(file);
			}
		}
	}

	private final class AddDirectoryButtonMouseAdapter extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			JFileChooser jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setDialogTitle("Select A Directory");
			int ret = jfc.showDialog(new JLabel(), null);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = jfc.getSelectedFile();
				fileListModel.addElement(file);
			}

		}
	}

	private final class RemoveSelectedButtonMouseAdapter extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			List<File> files = fileList.getSelectedValuesList();
			for (File file : files) {
				fileListModel.removeElement(file);
			}
		}
	}

	private final class ClearButtonMouseAdapter extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			fileListModel.removeAllElements();
		}
	}

	private final class SelectAllButtonMouseAdapter extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			int[] selectedIndices = new int[fileListModel.getSize()];
			for (int i = 0; i < selectedIndices.length; i++) {
				selectedIndices[i] = i;
			}
			fileList.clearSelection();
			fileList.setSelectedIndices(selectedIndices);
		}
	}

	private final class InverseSelectedButtonMouseAdapter extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			int[] newIndices = new int[fileListModel.getSize() - fileList.getSelectedIndices().length];
			for (int i = 0, j = 0; i < fileListModel.getSize(); ++i) {
				if (!fileList.isSelectedIndex(i)) {
					newIndices[j++] = i;
				}
			}
			fileList.clearSelection();
			fileList.setSelectedIndices(newIndices);
		}
	}

}
