package asgel.app.bundle;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * @author Florent Guille
 **/

public class BundleCellRenderer implements TableCellRenderer {

	private DefaultTableCellRenderer def;

	public BundleCellRenderer() {
		super();
		def = new DefaultTableCellRenderer();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		JLabel res = (JLabel) def.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value instanceof Bundle b) {
			res.setIcon(b.getIcon());
		}
		return res;
	}

}
