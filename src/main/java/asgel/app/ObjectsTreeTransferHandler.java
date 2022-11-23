package asgel.app;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import asgel.core.model.ModelOBJ;

@SuppressWarnings("serial")
public class ObjectsTreeTransferHandler extends TransferHandler {

	public static final DataFlavor OBJ_FLAVOR = new DataFlavor(ModelOBJ.class, "Model Object Data Flavor");

	private App app;

	public ObjectsTreeTransferHandler(App app) {
		super();
		this.app = app;
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		if (!support.isDrop()) {
			return false;
		}
		for (DataFlavor flavor : support.getDataFlavors()) {
			if (!OBJ_FLAVOR.equals(flavor)) {
				return false;
			}
		}
		JTree.DropLocation loc = (JTree.DropLocation) support.getDropLocation();
		TreePath path = loc.getPath();
		if (path == null) {
			return false;
		}
		Object obj = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
		return obj instanceof String || obj instanceof ObjectCategory;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.MOVE;

	}

	@Override
	public boolean importData(TransferHandler.TransferSupport support) {
		if (!support.isDrop()) {
			return false;
		}
		Transferable data = support.getTransferable();
		if (!(data.isDataFlavorSupported(OBJ_FLAVOR))) {
			return false;
		}
		ModelOBJ obj = null;
		try {
			obj = (ModelOBJ) data.getTransferData(OBJ_FLAVOR);
		} catch (UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
		}
		JTree.DropLocation loc = (JTree.DropLocation) support.getDropLocation();
		TreePath path = loc.getPath();
		if (path == null) {
			return false;
		}
		Object target = ((DefaultMutableTreeNode) loc.getPath().getLastPathComponent()).getUserObject();
		if (target instanceof String s) {
			if (s.equals("Default")) {
				obj.setCategory(null);
			} else if (s.equals("Categories")) {
				String cat = JOptionPane.showInputDialog(app.getJFrame(), "Which Category?");
				if (cat != null && !"".equals(cat)) {
					obj.setCategory(cat);
				}
			}
			app.getSelectedModelHolder().rebuildNodeRepresentation();
			app.updateObjectsTree();
			return true;
		}
		if (target instanceof ObjectCategory cat) {
			obj.setCategory(cat.toString());
			app.getSelectedModelHolder().rebuildNodeRepresentation();
			app.updateObjectsTree();
		}
		return false;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		JTree tree = (JTree) c;

		Object target = ((DefaultMutableTreeNode) tree.getLastSelectedPathComponent()).getUserObject();
		if (target instanceof ModelOBJ obj) {
			return new ObjectTransferable(obj);
		}
		return null;
	}

	private class ObjectTransferable implements Transferable {

		private ModelOBJ object;

		public ObjectTransferable(ModelOBJ object) {
			this.object = object;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { OBJ_FLAVOR };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return OBJ_FLAVOR.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (!flavor.equals(OBJ_FLAVOR)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return object;
		}

	}
}
