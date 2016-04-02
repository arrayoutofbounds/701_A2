package se701;

public class StudentSample {

	public static void main(String[] args) {
		sampleMethod(new Runnable(){
			@Override
			public void run(){ 
				for (int i = 0; i < 20; i++) {
					System.out.println(i);
				}
			}
		}
				);
		sampleMethod(new Runnable(){
			@Override
			public void run(){ 
				for (int i = 0; i < 5; i++) {
					System.out.println(i);
				}
			}
		}
				);
	}

	public static void sampleMethod(Runnable r) {
		System.out.println("Execution started");
		r.run();

		System.out.println("Execution ended");
	}
}
