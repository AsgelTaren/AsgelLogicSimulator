package asgel.app.bundle;

import javax.swing.table.AbstractTableModel;

import asgel.app.App;

/**
 * @author Florent Guille
 **/

@SuppressWarnings("serial")
public class BundleTableModel extends AbstractTableModel {

	private App app;

	public BundleTableModel(App app) {
		super();
		this.app = app;
	}

	@Override
	public int getRowCount() {
		return app.getBundles().size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Bundle b = app.getBundles().get(rowIndex);
		switch (columnIndex) {
		case 0:
			return b;
		case 1:
			return b.getID();
		case 2:
			return b.getDesc();
		}
		return null;
	}

	@Override
	public String getColumnName(int index) {
		switch (index) {
		case 0:
			return "Name";
		case 1:
			return "ID";
		case 2:
			return "Desc";
		}
		return null;
	}

	@Override
	public Class<?> getColumnClass(int index) {
		if (index == 0) {
			return Bundle.class;
		}
		return String.class;
	}

}
