//standard library imports
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.	swing.*;
import javax.imageio.ImageIO;

public class Gui extends JFrame{
	// minor fields
	public String title;
	public String icon_filename;
	public int window_x;
	public int window_y;
	public int window_width;
	public int window_height;
	public boolean options_enabled;
	//components
	public MyCardLayout cards;
	public Container frame;
	public JPanel panel;
	public OptionsPanel optionsPanel;
	public ProgressPanel progressPanel;
	public StartPanel startPanel;
	public JFileChooser fileChooser;


	public Gui(){
		//default fields
		title = "File Lister";
		icon_filename = "resource/file-lister.png";
		window_x = 0;
		window_y = 0;
		window_width = 500;
		window_height = 200;
		options_enabled = true;
		//components
		cards = null;
		frame = null;
		panel = null;
		optionsPanel = null;
		progressPanel = null;
		startPanel = null;
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(
			JFileChooser.DIRECTORIES_ONLY);}

	public void setup(){
		//Window setup
		setTitle(title);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// set icon
		try{
			this.setIconImage(
				ImageIO.read(
					new ResourceManager().locate(icon_filename)));}
		catch(Exception e){ System.out.println(e);}

		//add content
		// get frame
		frame = getContentPane();
		// main panel
		cards =  new MyCardLayout();
		panel = new JPanel( cards);
		// start panel
		startPanel = new StartPanel();
		panel.add( startPanel, startPanel.NAME);
		// options panel
		optionsPanel = new OptionsPanel();
		panel.add( optionsPanel, optionsPanel.NAME);
		// progress panel
		progressPanel = new ProgressPanel();
		panel.add( progressPanel, progressPanel.NAME);
		// show start panel
		frame.add( panel);

		//done
		startPanel.switchTo();
		this.setResizable(true);
		this.setLocationRelativeTo(null);
		System.out.println("Done Loading GUI");
		this.setVisible(true);}

	//functions
	public void close(){
		Toolkit.getDefaultToolkit().
			getSystemEventQueue().postEvent(
			new WindowEvent(this, WindowEvent.WINDOW_CLOSING));}

	public File getFile(String dialogTitle){
		fileChooser.setDialogTitle( dialogTitle);
		int returnCode = fileChooser.showOpenDialog( this);
		File result = null;
		switch( returnCode){
			case JFileChooser.CANCEL_OPTION:
				System.out.println("File selection cancelled");
				break;
			case JFileChooser.APPROVE_OPTION:
				System.out.println("File chosen");
				result = fileChooser.getSelectedFile();
				break;
			case JFileChooser.ERROR_OPTION:
				System.out.println("File selection error");
				break;
			default: break;}
		return result;}

	//sub-classes
	protected class MyCardLayout extends CardLayout {
		@Override
		public Dimension preferredLayoutSize(Container parent) {
			Component current = findCurrentComponent(parent);
			if (current != null) {
				Insets insets = parent.getInsets();
				Dimension pref = current.getPreferredSize();
				pref.width += insets.left + insets.right;
				pref.height += insets.top + insets.bottom;
				return pref;}
			else return super.preferredLayoutSize(parent);}

		public Component findCurrentComponent(Container parent) {
			for (Component comp : parent.getComponents())
				if (comp.isVisible())
					return comp;
			return null;}
	}
	
	protected class CustomJPanel extends JPanel {
		public String NAME;
		public JButton defaultButton;
		protected CustomJPanel( String NAME){
			this.NAME = NAME;
			defaultButton = null;}
		public void switchTo(){
			Gui.this.setVisible(false);
			cards.show( panel, NAME);
			Gui.this.setMinimumSize(null);
			Gui.this.pack();
			Gui.this.setMinimumSize( Gui.this.getSize());
			Gui.this.getRootPane().setDefaultButton( defaultButton);
			Gui.this.setLocationRelativeTo(null);
			Gui.this.setVisible(true);}
	}
	protected class StartPanel extends CustomJPanel {
		FileSelectionRow input;
		FileSelectionRow output;
		boolean output_manuallySet;
		JLabel message;
		JButton close;
		JButton options;
		JButton start;

		public StartPanel(){
			super("Start");
			GridBagLayout layout = new GridBagLayout();
			this.setLayout( layout);
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.fill = GridBagConstraints.NONE;
			constraints.insets = new Insets( 2, 2, 2, 2);

			//input row
			input = new FileSelectionRow("Input");
			// label
			constraints.gridx = 0;
			constraints.gridy = 0;
			this.add( input.label, constraints);
			// text field
			constraints.gridx = 1;
			constraints.weightx = 1;
			constraints.gridwidth = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			this.add( input.text, constraints);
			//reset constraints
			constraints.weightx = 0;
			constraints.gridwidth = 1;
			constraints.fill = GridBagConstraints.NONE;
			// button
			constraints.gridx = 2;
			this.add( input.button, constraints);

			//output row
			output = new FileSelectionRow("Output");
			output_manuallySet = false;
			// label
			constraints.gridx = 0;
			constraints.gridy = 1;
			this.add( output.label, constraints);
			// text field
			constraints.gridx = 1;
			constraints.weightx = 1;
			constraints.gridwidth = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			this.add( output.text, constraints);
			//reset constraints
			constraints.weightx = 0;
			constraints.gridwidth = 1;
			constraints.fill = GridBagConstraints.NONE;
			// button
			constraints.gridx = 2;
			this.add( output.button, constraints);

			//file count
			message = new JLabel(
				"Convertible files found: ");
			// add file count label
			constraints.gridx = 0;
			constraints.gridy = 2;
			constraints.weightx = 1;
			constraints.gridwidth = 3;
			constraints.anchor = GridBagConstraints.LINE_START;
			this.add( message, constraints);
			//reset
			constraints.weightx = 0;
			constraints.gridwidth = 1;
			constraints.anchor = GridBagConstraints.CENTER;

			//button row
			JPanel buttons = new JPanel();
			close = new JButton("Close");
			close.setMnemonic( KeyEvent.VK_C);
			options = new JButton("Options");
			options.setEnabled( options_enabled);
			options.setVisible( options_enabled);
			options.setMnemonic( KeyEvent.VK_O);
			start = new JButton("Start");
			start.setMnemonic( KeyEvent.VK_S);
			//buttons.add( message);
			buttons.add( close);
			buttons.add( options);
			buttons.add( start);
			// add button row
			constraints.gridx = 0;
			constraints.gridy = 3;
			constraints.gridwidth = 3;
			constraints.anchor = GridBagConstraints.LINE_END;
			this.add( buttons, constraints);

			//set default button
			defaultButton = start;}

		protected class FileSelectionRow {
			JLabel label;
			JTextField text;
			JButton button;
			public FileSelectionRow(String labelText){
				//label
				label = new JLabel( labelText);
				//text field
				text = new JTextField(30);
				//button
				button = new JButton("...");
				Dimension button_dim = button.getMinimumSize();
				button_dim.width = button_dim.height;
				button.setMinimumSize(button_dim);
				button.setPreferredSize(button_dim);}
		}
	}
	protected class ProgressPanel extends CustomJPanel {
		JTextArea log;
		JProgressBar progressBar;
		JButton button;

		public ProgressPanel(){
			super("Progress");
			GridBagLayout layout = new GridBagLayout();
			this.setLayout( layout);
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.fill = GridBagConstraints.NONE;
			constraints.insets = new Insets( 2, 2, 2, 2);

			//Log
			log = new JTextArea();
			log.setEditable(false);
			JScrollPane logscroller = new JScrollPane( log);
			Dimension logscroller_dim = new Dimension( 150, 200);
			logscroller.setMinimumSize( logscroller_dim);
			logscroller.setPreferredSize( logscroller_dim);
			//add log
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 1;
			constraints.weighty = 1;
			constraints.fill = GridBagConstraints.BOTH;
			constraints.gridwidth = 2;
			this.add( logscroller, constraints);
			//reset constraints
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.fill = GridBagConstraints.NONE;
			constraints.gridwidth = 1;

			//progress bar
			progressBar = new JProgressBar(0, 10);
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
			// add progress bar with constraints
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.ipadx = 100;
			constraints.weightx = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			this.add( progressBar, constraints);
			//reset constraints
			constraints.ipadx = 0;
			constraints.weightx = 0;
			constraints.fill = GridBagConstraints.NONE;

			//button
			button = new JButton("Done");
			button.setVerticalTextPosition( AbstractButton.BOTTOM);
			button.setHorizontalTextPosition( AbstractButton.CENTER);
			button.setEnabled(false);
			button.setMnemonic( KeyEvent.VK_D);
			// add button with constraints
			constraints.gridx = 1;
			this.add( button, constraints);

			//set default button
			defaultButton = button;}
	}
	protected class OptionsPanel extends CustomJPanel {
		public JLabel mode_label;
		public JButton cancel;
		public JButton done;

		public OptionsPanel(){
			super("Options");
			GridBagLayout layout = new GridBagLayout();
			this.setLayout( layout);
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.fill = GridBagConstraints.NONE;
			constraints.insets = new Insets( 2, 2, 2, 2);

			mode_label = new JLabel("Include Subfolders:");
			constraints.gridx = 0;
			constraints.gridy = 0;
			this.add( mode_label, constraints);

			cancel = new JButton("Cancel");
			done = new JButton("Done");
			JPanel buttons = new JPanel();
			//buttons.add( cancel);
			buttons.add( done);
			constraints.gridx = 0;
			constraints.gridy = 2;
			constraints.gridwidth = 2;
			constraints.anchor = GridBagConstraints.LINE_END;
			this.add( buttons, constraints);

		}
	}
}