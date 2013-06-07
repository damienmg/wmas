package wmas.gui.run;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import wmas.reports.Reports;
import wmas.reports.ReportsTableModel;

public class ReportDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	private Vector<JTable> tables = new Vector<JTable>();
	private JButton save = new JButton("Export...");
	private JButton close = new JButton("Close");
	private JFileChooser fileChooser = new JFileChooser();
	private Reports reports = null;
	private JTabbedPane parentPane = null;

	private void construct() {
		getRootPane().setLayout(new GridBagLayout());
		getRootPane().removeAll();
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		if (reports != null) {
			tables.add(new JTable(new ReportsTableModel(reports, -1)));
			for (int i = 0; i < reports.getNbReport(); i++)
				tables.add(new JTable(new ReportsTableModel(reports, i)));
		}
		parentPane = new JTabbedPane();
		for (int i = 0; i < tables.size(); i++)
			parentPane.addTab(i == 0 ? "Summary report"
					: "Report of execution " + i,
					new JScrollPane(tables.get(i)));
		getRootPane().add(parentPane, c);

		save.addActionListener(this);
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		c.weighty = 0;
		c.gridwidth = 1;
		getRootPane().add(new JLabel(), c);
		c.weightx = 0;
		getRootPane().add(save, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		getRootPane().add(close, c);
		pack();

		FileFilter ff = new FileNameExtensionFilter(
				"Spreadsheet formats (CSV, XLS)", "CSV", "XLS");
		fileChooser.addChoosableFileFilter(ff);
		fileChooser.setFileFilter(ff);
	}

	public ReportDialog(Dialog owner, String title, Reports reports) {
		super(owner, title, true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.reports = reports;
		construct();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	public ReportDialog(Frame owner, String title, Reports reports) {
		super(owner, title, true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.reports = reports;
		construct();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	public ReportDialog(String title, Reports reports) {
		super();
		setTitle(title);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.reports = reports;
		construct();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try {
				if (f.getName().toLowerCase().endsWith(".xls")) {
					((ReportsTableModel) tables.get(0).getModel())
							.exportExcel(f);
				} else {
					((ReportsTableModel) tables.get(
							parentPane.getSelectedIndex()).getModel())
							.exportCSV(f);
				}
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this, e1.getMessage(),
						"Error during exportation", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
