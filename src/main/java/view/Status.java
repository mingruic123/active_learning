package view;

/**
 * A singleton class to monitor training progress
 */
public class Status {
	private static int[] iterations = {0,0,0,0,0};
	private static Status status = null;
	private Status(){}

	public static synchronized Status getStatus(){
		if(status == null){
			return new Status();
		}
		return status;
	}
	public void printProgress(int fold, int iteration){
		iterations[fold] = iteration;
		System.out.print("\r"+iterations[0] + " " + iterations[1] + " " +  iterations[2] + " " +  iterations[3] + " " +  iterations[4]);
	}

}
