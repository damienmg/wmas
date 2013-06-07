package wmas.reports;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.swing.table.AbstractTableModel;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import wmas.reports.Report.DataReport;
import wmas.reports.Report.EventReport;
import wmas.reports.Report.TimedReport;

public class ReportsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private Reports reports = null;
	private int selectedReport = -1;
	private List<String> dataList = new ArrayList<String>();
	private List<TimedReport> dataList2 = new ArrayList<TimedReport>();

	public ReportsTableModel(Reports reports, int n) {
		super();
		setReports(reports);
		setSelectedReport(n);
	}

	public void setReports(Reports reports) {
		this.reports = reports;
		selectedReport = -1;
		dataList.clear();
		dataList2.clear();
		if (reports.descr != null) {
			for (String s : reports.descr.storedReport.keySet()) {
				for (String s2 : reports.descr.storedReport.get(s).keySet()) {
					if (s2 == null || s2.isEmpty())
						dataList.add(s);
					else
						dataList.add(s + " - " + s2);
				}
			}
		}
		fireTableStructureChanged();
	}

	public void setSelectedReport(int n) {
		this.selectedReport = n;
		dataList2.clear();
		if (reports != null && n >= 0 && n < reports.reports.size()) {
			dataList2.addAll(reports.reports.get(n).allReports);
		}
		fireTableStructureChanged();
	}

	@Override
	public int getColumnCount() {
		if (reports == null || reports.reports.isEmpty() || selectedReport < 0
				|| (selectedReport >= reports.reports.size())) {
			return dataList.size() + 1;
		}
		return 3;
	}

	@Override
	public int getRowCount() {
		if (reports == null || reports.reports.isEmpty())
			return 0;
		if (selectedReport < 0 || (selectedReport >= reports.reports.size())) {
			return reports.reports.size();
		}
		return dataList2.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (reports == null || reports.reports.isEmpty())
			return null;
		if (selectedReport < 0 || (selectedReport >= reports.reports.size())) {
			if (columnIndex == 0)
				return rowIndex + 1;
			SortedSet<DataReport> ss = reports.reports.get(rowIndex).data
					.get(dataList.get(columnIndex - 1));
			if (ss == null || ss.isEmpty())
				return 0.0;
			return ss.last().value;
		}

		TimedReport tr = dataList2.get(rowIndex);
		if (columnIndex == 0)
			return tr.t;
		if (tr instanceof DataReport) {
			return columnIndex == 1 ? ((DataReport) tr).name : Double
					.toString(((DataReport) tr).value);
		} else if (tr instanceof EventReport) {
			return columnIndex == 1 ? ((EventReport) tr).name
					: ((EventReport) tr).entity;
		}
		return "";
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (reports == null || reports.reports.isEmpty())
			return super.getColumnClass(columnIndex);
		if (selectedReport < 0 || (selectedReport >= reports.reports.size())) {
			return columnIndex == 0 ? Integer.class : Double.class;
		} else {
			switch (columnIndex) {
			case 0:
				return Double.class;
			case 1:
			case 2:
				return String.class;
			}
		}
		return super.getColumnClass(columnIndex);
	}

	@Override
	public String getColumnName(int column) {
		if (reports == null || reports.reports.isEmpty())
			return "";
		if (selectedReport < 0 || (selectedReport >= reports.reports.size())) {
			if (column == 0)
				return "Run number";
			return dataList.get(column - 1);
		} else {
			switch (column) {
			case 0:
				return "Time";
			case 1:
				return "Name";
			case 2:
				return "Entity/value";
			}
		}
		return "";
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public void rootExcelSheet(WritableSheet sheet, WritableCellFormat boldFmt)
			throws RowsExceededException, WriteException {
		if (selectedReport >= 0) {
			setSelectedReport(-1);
		}
		sheet.addCell(new Label(0, 0, "Run number", boldFmt));
		int c = 1;
		for (String s : dataList) {
			sheet.addCell(new Label(c, 0, s, boldFmt));
			c++;
		}
		for (int r = 1; r <= reports.reports.size(); r++) {
			sheet.addCell(new Number(0, r, r));
			c = 1;
			for (String s : dataList) {
				SortedSet<DataReport> ss = reports.reports.get(r - 1).data
						.get(s);
				sheet.addCell(new Number(c, r,
						(ss == null || ss.isEmpty()) ? 0.0 : ss.last().value));
				c++;
			}
		}
	}

	public void runExcelSheet(WritableSheet sheet, int n,
			WritableCellFormat boldFmt) throws RowsExceededException,
			WriteException {
		sheet.addCell(new Label(0, 0, "Time", boldFmt));
		sheet.addCell(new Label(1, 0, "Name", boldFmt));
		sheet.addCell(new Label(2, 0, "Entity/Value", boldFmt));
		int r = 1;
		for (TimedReport tr : reports.reports.get(n).allReports) {
			sheet.addCell(new Number(0, r, tr.t));
			if (tr instanceof DataReport) {
				sheet.addCell(new Label(1, r, ((DataReport) tr).name));
				sheet.addCell(new Number(2, r, ((DataReport) tr).value));
			} else if (tr instanceof EventReport) {
				sheet.addCell(new Label(1, r, ((EventReport) tr).name));
				sheet.addCell(new Label(2, r, ((EventReport) tr).entity));
			}
			r++;
		}
	}

	public void exportExcel(File f) throws WriteException, IOException {
		WritableFont bold = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD);
		WritableCellFormat boldFmt = new WritableCellFormat(bold);

		WritableWorkbook workbook = Workbook.createWorkbook(f);
		rootExcelSheet(workbook.createSheet("Summary report", 0), boldFmt);
		for (int k = 0; k < reports.reports.size(); k++) {
			runExcelSheet(workbook.createSheet(
					"Report of execution " + (k + 1), k + 1), k, boldFmt);
		}
		workbook.write();
		workbook.close();
	}

	// RFC 4180 - CSV Format
	private static final String CSV_COMMA = ";"; // Excel knows only this comma.
													// Character.toString((char)0x2C);
	private static final String CSV_CR = Character.toString((char) 0x0D);
	private static final String CSV_DQUOTE = Character.toString((char) 0x22);
	private static final String CSV_LF = Character.toString((char) 0x0A);
	private static final String CSV_CRLF = CSV_CR + CSV_LF;

	private static String csvValue(String v) { // Returns an escaped item from a
												// string according to RFC 4180
		if (v == null)
			return "";
		// escaped = DQUOTE *(TEXTDATA / COMMA / CR / LF / 2DQUOTE) DQUOTE
		return CSV_DQUOTE + v.replace(CSV_DQUOTE, CSV_DQUOTE + CSV_DQUOTE)
				+ CSV_DQUOTE;
	}

	private static String csvLine(String[] v) { // Returns a record/header from
												// String[] according to RFC
												// 4180
		String r = "";
		for (int i = 0; i < v.length; i++) {
			if (i > 0)
				r += CSV_COMMA;
			r += csvValue(v[i]);
		}
		return r + CSV_CRLF;
	}

	public void exportCSV(File f) throws IOException {
		Writer fOutput = new FileWriter(f);
		String[] r = new String[getColumnCount()];
		for (int i = 0; i < r.length; i++)
			r[i] = getColumnName(i);
		fOutput.write(csvLine(r));
		for (int k = 0; k < getRowCount(); k++) {
			for (int i = 0; i < r.length; i++) {
				r[i] = getValueAt(k, i).toString();
			}
			fOutput.write(csvLine(r));
		}
		fOutput.close();
	}
}
