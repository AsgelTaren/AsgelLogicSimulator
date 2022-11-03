package asgel.app.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;

import asgel.app.App;
import asgel.core.model.BundleRegistry.ObjectEntry;

/**
 * @author Florent Guille
 **/

@SuppressWarnings("serial")
public class OBJTreeTransferHandler extends TransferHandler {

	private App app;

	public OBJTreeTransferHandler(App app) {
		this.app = app;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) app.getTree().getLastSelectedPathComponent();
		if (node.getUserObject() instanceof ObjectEntry entry) {
			return new ObjectEntryTransferable(entry);
		}
		return null;
	}

	public int getSourceActions(JComponent c) {
		return MOVE;
	}

	public class ObjectEntryTransferable implements Transferable {

		private ObjectEntry entry;

		public ObjectEntryTransferable(ObjectEntry entry) {
			this.entry = entry;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { ModelHolder.OBJFLAVOR };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return ModelHolder.OBJFLAVOR.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			return entry;
		}

	}

}