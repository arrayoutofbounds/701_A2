public class Test39 {

    public Test39() {
        method1(new Runnable(){
        @Override
        public void run(){ 
            z = 2;
        }
        }
        );
    }

    public static void method1(Runnable r) {
        int x = 1;
        r.run();

    }
}
