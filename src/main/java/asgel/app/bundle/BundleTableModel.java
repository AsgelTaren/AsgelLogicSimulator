package asgel.app.bundle;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import asgel.app.App;
import asgel.core.bundle.Bundle;

/**
 * @author Florent Guille
 **/

@SuppressWarnings("serial")
public class BundleTableModel extends AbstractTableModel {

	private App app;
	private ArrayList<Bundle> bundles;

	public BundleTableModel(App app) {
		super();
		this.app = app;
		bundles = new ArrayList<>(app.getGlobalRegistry().getBundles().values());
	}

	@Override
	public int getRowCount() {
		return app.getGlobalRegistry().getBundles().size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Bundle b = bundles.get(rowIndex);
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
			return app.getText("bundlesdialog.bundle.name");
		case 1:
			return app.getText("bundlesdialog.bundle.id");
		case 2:
			return app.getText("bundlesdialog.bundle.desc");
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
