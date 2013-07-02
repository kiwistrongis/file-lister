//standard library imports
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;

public class FileLister {
	//conversion parameters
	public String delimiter;
	public String eol;
	public String encoding;
	public String default_output_ext;
	public boolean recursive;
	//working variables
	public File input;
	public File output;
	public Vector<File> directories;
	public Vector<Worker> workers;
	public Vector<WorkerTerminationListener> listeners;
	public Vector<String> results;
	//statistic variables
	public Object statslock;
	public boolean ready;
	public boolean started;
	public boolean done;
	public int completed;
	public int total;
	public Vector<Exception> failureCauses;
	//misc vars
	public PrintStream log;
	public FileFilter filter;

	//constructors
	public FileLister( File input, File output){
		//convertion parameters
		delimiter = "\t";
		eol = "\r\n";
		encoding = "UTF-8";
		recursive = false;
		//conversion variables
		this.input = input;
		this.output = output;
		this.ready = false;
		//statistic variables
		statslock = new Object();
		//misc vars
		listeners = new Vector<WorkerTerminationListener>();}

	//public member functions
	public void prep()
			throws IOException, FileNotFoundException {
		//reinitialization
		ready = false;
		started = false;
		done = false;
		completed = 0;
		failureCauses = new Vector<Exception>();
		directories = new Vector<File>();
		workers = new Vector<Worker>();
		results = new Vector<String>();
		log = new PrintStream( new File( "failure.log"));

		//assert input file existance
		if( ! input.exists())
			throw new FileNotFoundException(
				"Input Directory does not Exist");
		//assert input is a directory
		if( ! input.isDirectory())
			throw new FileNotFoundException(
				"Input is not a Directory");
		//assert input is a not directory
		if( output.exists()){
			if( output.isDirectory())
				throw new IOException("Output File is a Directory");}

		//select directories
		if( recursive){}
		else;
			//directories.add( input);

		//create worker threads
		for( File directory : directories)
			workers.add( new Worker( directory));
		//reset stats
		total = workers.size();
		ready = true;
		return;}

	public void start(){
		output.getParentFile().mkdirs();
		synchronized( statslock){
			started = true;}
		if( total != 0)
			for( Worker worker : workers)
				worker.start();
		else{
			done = true;
			finishUp();
			//update listeners
			WorkerTerminationEvent event =
				new WorkerTerminationEvent( this, null);
			for( WorkerTerminationListener l : listeners)
				l.workerTerminated( event);}}

	//private member functions
	private PrintWriter open( File file)
			throws FileNotFoundException {
		return new PrintWriter(
			new BufferedWriter(
				new OutputStreamWriter(
					new FileOutputStream( file),
					Charset.forName( encoding).newEncoder())));}

	private void handleWorkerTermination( Worker worker){
		//update stats
		synchronized( statslock){
			if( !worker.succeeded)
				failureCauses.add( worker.failureCause);
			completed++;
			if( completed == total)
				finishUp();}
		//update listeners
		WorkerTerminationEvent event =
			new WorkerTerminationEvent( this, worker);
		for( WorkerTerminationListener l : listeners)
			l.workerTerminated( event);}

	private void finishUp(){
		Collections.sort( results);
		try{
			PrintWriter outputWriter = open( output);
			for( String result : results){
				outputWriter.printf( "%s%s", result, delimiter);
				outputWriter.flush();}
			outputWriter.close();}
		catch( FileNotFoundException e){
			log.println(e.getStackTrace());}
		synchronized( statslock){
			done = true;}}

	//subclasses
	protected class Worker extends Thread {
		public File input;
		public boolean completed;
		public boolean succeeded;
		public Exception failureCause;
		public Worker( File input){
			this.input = input;
			completed = false;
			succeeded = false;}
		public void run(){
			succeeded = true;
			completed = true;
			//for each child file in input,
			//  results.add( file relative to FileList.input);
			handleWorkerTermination(this);}
	}
}