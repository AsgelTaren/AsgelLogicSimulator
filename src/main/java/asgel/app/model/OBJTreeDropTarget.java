package asgel.app.model;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import asgel.core.model.BundleRegistry.ObjectEntry;

/**
 * @author Florent Guille
 **/

public class OBJTreeDropTarget implements DropTargetListener {

	private ModelHolder holder;

	public OBJTreeDropTarget(ModelHolder holder) {
		this.holder = holder;
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {

	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {

	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {

	}

	@Override
	public void dragExit(DropTargetEvent dte) {

	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		if (!dtde.isDataFlavorSupported(ModelHolder.OBJFLAVOR)) {
			dtde.rejectDrop();
		}

		dtde.acceptDrop(DnDConstants.ACTION_MOVE);
		java.awt.Point mouse = dtde.getLocation();
		try {
			holder.addObject((ObjectEntry) dtde.getTransferable().getTransferData(ModelHolder.OBJFLAVOR), mouse.x,
					mouse.y);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
