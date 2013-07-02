//standard library imports
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Document;

/** Controller sub-class
 * handles all user input
 **/
public class Controller extends KeyAdapter
		implements ActionListener, WorkerTerminationListener,
			DocumentListener {
	//constant fields
	public final static String
		sp_start_ac = "sp_start_ac",
		sp_close_ac = "sp_close_ac",
		sp_options_ac = "sp_options_ac",
		sp_input_text_ac = "sp_input_text_ac",
		sp_input_button_ac = "sp_input_button_ac",
		sp_output_text_ac = "sp_output_text_ac",
		sp_output_button_ac = "sp_output_button_ac",
		op_recursive_ac = "op_recursive_ac",
		op_relativize_ac = "op_relativize_ac",
		op_cancel_ac = "op_cancel_ac",
		op_done_ac = "op_done_ac",
		pp_done_ac = "pp_done_ac",
		documentContainer_property = "documentContainer_property";
	// major objects
	Gui gui;
	FileLister fileLister;
	// minor fields

	public Controller(){
		gui = null;
		fileLister = null;}

	public void listenTo(FileLister fileLister){
		//listen to fileLister
		this.fileLister = fileLister;
		fileLister.listeners.add(this);}

	public void listenTo( Gui gui){
		//listen to gui
		this.gui = gui;

		// start panel
		this.listenTo( gui.startPanel.start, sp_start_ac);
		this.listenTo( gui.startPanel.close, sp_close_ac);
		this.listenTo( gui.startPanel.options, sp_options_ac);
		this.listenTo( gui.startPanel.input.text, sp_input_text_ac);
		this.listenTo( gui.startPanel.input.button, sp_input_button_ac);
		this.listenTo( gui.startPanel.output.text, sp_output_text_ac);
		this.listenTo( gui.startPanel.output.button, sp_output_button_ac);

		// options panel
		this.listenTo( gui.optionsPanel.recursive, op_recursive_ac);
		this.listenTo( gui.optionsPanel.relativize, op_relativize_ac);
		this.listenTo( gui.optionsPanel.cancel, op_cancel_ac);
		this.listenTo( gui.optionsPanel.done, op_done_ac);

		// progress panel
		this.listenTo( gui.progressPanel.progressBar);
		this.listenTo( gui.progressPanel.button, pp_done_ac);

		//update gui with fileLister's files
		setInput( fileLister.input);
		setOutput( fileLister.output);
		updateMessage();
		//update options panel
		gui.optionsPanel.recursive.setSelected(
			fileLister.recursive);
		gui.optionsPanel.relativize.setSelected(
			fileLister.relativize);}

	public void listenTo( JButton button, String ac){
		button.setActionCommand( ac);
		button.addActionListener( this);
		button.addKeyListener( this);}

	public void listenTo( JCheckBox button, String ac){
		button.setActionCommand( ac);
		button.addActionListener( this);
		button.addKeyListener( this);}

	public void listenTo( JTextField field, String ac){
		field.setActionCommand( ac);
		field.addActionListener( this);
		field.addKeyListener( this);
		Document doc = field.getDocument();
		doc.putProperty( documentContainer_property, ac);
		doc.addDocumentListener( this);}

	public void listenTo( JProgressBar bar){
		bar.addKeyListener( this);}
	
	//handles
	public void workerTerminated( WorkerTerminationEvent event){
		if( fileLister!= null)
			synchronized( fileLister.statslock){
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						gui.progressPanel.progressBar.setValue(
							fileLister.completed);}});
				if( fileLister.done)
					gui.progressPanel.button.setEnabled( true);}
		if( event.worker != null)
			gui.progressPanel.log.append(
				String.format(
					"Finished Directory %s\n",
					event.worker.input.getName()));
		else
			gui.progressPanel.log.append("Done");}


	public void actionPerformed( ActionEvent event){
		String ac = event.getActionCommand();
		//System.out.println(ac);
		switch( ac){
			//start panel
			case sp_start_ac:{
				fileLister.start();
				while( true){
					synchronized( fileLister.statslock){
						if( fileLister.started){
							gui.progressPanel.log.setText("");
							gui.progressPanel.progressBar.setMaximum(
								fileLister.total);
							gui.progressPanel.progressBar.setValue(
								fileLister.completed);
							gui.progressPanel.switchTo();
							break;}}}
				break;}
			case sp_close_ac:{
				gui.close();
				break;}
			case sp_options_ac:{
				gui.optionsPanel.switchTo();
				break;}
			case sp_input_text_ac:{
				break;}
			case sp_input_button_ac:{
				gui.fileChooser.setCurrentDirectory(
					fileLister.input.isDirectory() ?
						fileLister.input :
						fileLister.input.getParentFile());
				File selection = gui.getFile(
					"Select Input Directory");
				if( selection != null)
					setInput( selection);
				break;}
			case sp_output_text_ac:{
				break;}
			case sp_output_button_ac:{
				gui.fileChooser.setCurrentDirectory(
					fileLister.output.isDirectory() ?
						fileLister.output :
						fileLister.output.getParentFile());
				File selection = gui.getFile(
					"Select Output Directory");
				if( selection != null)
					setOutput( selection);
				break;}

			//progress panel
			case pp_done_ac:{
				if( fileLister.done){
					gui.startPanel.switchTo();
					try{ fileLister.prep();}
					catch( java.io.IOException exception){
						exception.printStackTrace();}
					gui.startPanel.start.setEnabled( fileLister.ready);}
				break;}

			//options panel
			case op_recursive_ac:{
				fileLister.recursive = 
					gui.optionsPanel.recursive.isSelected();
				break;}
			case op_relativize_ac:{
				fileLister.relativize = 
					gui.optionsPanel.relativize.isSelected();
				break;}
			case op_cancel_ac:{
				break;}
			case op_done_ac:{
				gui.startPanel.switchTo();
				break;}
			//say what?
			default:break;}}

	public void keyPressed(KeyEvent e){
		//System.out.println( e.getKeyCode());
		switch( e.getKeyCode()){
			case KeyEvent.VK_ESCAPE:{
				synchronized( fileLister.statslock){
					if( fileLister.done || ! fileLister.started)
						gui.close();}
				break;}
			case KeyEvent.VK_ENTER:{
				updateMessage();
				break;}
			default: break;}}

	public void changedUpdate(DocumentEvent event){
		documentUpdate( event);}
	public void insertUpdate(DocumentEvent event){
		documentUpdate( event);}
	public void removeUpdate(DocumentEvent event){
		documentUpdate( event);}
	public void documentUpdate( DocumentEvent event){
		Document doc = event.getDocument();
		String field_ac = (String) doc.getProperty(
			documentContainer_property);
		textFieldUpdate( field_ac);}

	public void textFieldUpdate( String ac){
		String content = null;
		switch( ac){
			case sp_input_text_ac:{
				content = gui.startPanel.input.text.getText();
				setInput( content);
				break;}
			case sp_output_text_ac:{
				content = gui.startPanel.output.text.getText();
				setOutput( content);
				break;}
			default:break;}}

	//private functions
	private void setInput(String text){
		File input = new File( text);
		fileLister.input = input;
		updateMessage();}
	private void setInput(File file){
		fileLister.input = file;
		gui.startPanel.input.text.setText(
			file.getAbsolutePath());
		updateMessage();}

	private void setOutput(String text){
		File output = new File( text);
		fileLister.output = output;
		updateMessage();}
	private void setOutput(File file){
		fileLister.output = file;
		gui.startPanel.output.text.setText(
			file.getAbsolutePath());
		updateMessage();}

	private void updateMessage(){
		try{
			fileLister.prep();
			gui.startPanel.message.setText( "Ready");
			gui.startPanel.start.setEnabled( fileLister.ready);}
		catch( Exception e){
			gui.startPanel.message.setText( e.getMessage());
			gui.startPanel.start.setEnabled( false);}}
}
