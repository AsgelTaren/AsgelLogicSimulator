package asgel.app;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Florent Guille
 **/

@SuppressWarnings("serial")
public class WorkingDirPanel extends JPanel {

	private JComboBox<File> combo;
	private LoadingFrame frame;

	public WorkingDirPanel(LoadingFrame frame, LaunchConfig config) {
		super();
		// Layout
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = gbc.weighty = 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.insets = new Insets(5, 5, 5, 5);
		setBorder(BorderFactory.createTitledBorder(frame.getApp().getText("loading.workingdir.select")));

		// Label
		add(new JLabel(frame.getApp().getText("loading.workingdir")), gbc);

		// Loading Frame
		this.frame = frame;

		// Combo box
		combo = new JComboBox<File>();
		combo.setEditable(false);

		// Loading
		try {
			JsonArray arr = config.getConfigJson().get("lastdirs").getAsJsonArray();
			for (JsonElement e : arr) {
				combo.addItem(new File(e.getAsString()));
			}
		} catch (Exception e) {
			Logger.INSTANCE.log(e.getMessage());
		}
		combo.addActionListener(e -> {
			update();
		});

		gbc.gridx = 1;

		add(combo, gbc);

		JButton browse = new JButton(frame.getApp().getText("loading.browse"));
		browse.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int choice = chooser.showDialog(this, frame.getApp().getText("loading.workingdir.approve"));
			if (choice == JFileChooser.APPROVE_OPTION) {
				combo.addItem(chooser.getSelectedFile());
			}

		});
		gbc.gridx = 2;
		add(browse, gbc);
	}

	public void update() {
		frame.getLaunchButton().setEnabled(combo.getSelectedIndex() != -1);
	}

	public void storeDirs(LaunchConfig config) {
		Logger log = Logger.INSTANCE.derivateLogger("[CONFIG]");
		try {
			File f = config.getConfigFile();
			if (!f.exists()) {
				log.log("No config file where found, trying to create one...");
				f.getParentFile().mkdirs();
				f.createNewFile();
			}

			JsonArray arr = new JsonArray();
			for (int i = 0; i < combo.getModel().getSize(); i++) {
				arr.add(combo.getModel().getElementAt(i).getPath());
			}
			JsonObject res = new JsonObject();
			res.add("lastdirs", arr);
			Utils.write(f, res.toString());
		} catch (Exception e) {
			log.log("Unable to create config file: " + e.getMessage());
		}
	}

	public File getWorkingDir() {
		return (File) combo.getSelectedItem();
	}

	@Override
	public String toString() {
		return frame.getApp().getText("loading.workingdir.title");
	}

}
