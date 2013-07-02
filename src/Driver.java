//standard library imports
import java.io.File;
import java.nio.file.Path;

public class Driver {
	public static void main( String[] args){
		//local variables
		boolean nogui = false;
		Path program_path;
		File input;
		File output;
		FileLister fileLister;
		final Configuration config = new Configuration();
		final Controller controller = new Controller();

		//find program path
		//handle args
		if( args.length == 2){
			input = new File( args[0]);
			output = new File( args[1]);}
		else {
			input = args.length == 0 ?
				new File( System.getProperty("user.dir")):
				new File( args[0]);
			Path input_path = input.toPath();
			output = input_path.resolve(
				input.getName().concat(".txt")).toFile();}

		//initialize fileLister
		fileLister = new FileLister( input, output);
		controller.listenTo( fileLister);

		//locate config file
		ResourceManager rm = new ResourceManager();
		File config_file = rm.locate("data/config.ini");
		if( config_file == null)
			config_file = rm.locate("data/default.ini");
		//load configuration from file
		try{
			config.open( config_file);}
		catch( Exception e){
			System.out.println("Configuration loading failed");
			e.printStackTrace();}

		//prepare fileLister
		try{
			if( config != null)
				config.load( fileLister);}
		catch( Exception e){
			e.printStackTrace();}

		//start gui
		config.loadGuiSettings();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Gui gui = new Gui();
				config.load( gui);
				gui.setup();
				controller.listenTo( gui);}});

		//start
		if( nogui){
			try{
				fileLister.prep();
				fileLister.start();}
			catch( Exception e){
				e.printStackTrace();}}}
}
