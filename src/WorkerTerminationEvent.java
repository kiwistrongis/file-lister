import java.util.EventObject;
public class WorkerTerminationEvent extends EventObject {
	FileLister.Worker worker;
	public WorkerTerminationEvent( Object source,
			FileLister.Worker worker) {
		super(source);
		this.worker = worker;}
}