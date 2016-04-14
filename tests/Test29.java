public class Test29 {

    private boolean a = true;

    private void boo() {
        int b = 0;
        {
            System.out.println("hey");
        }foo(new Runnable(){
        @Override
        public void run(){ 
            System.out.println("hey");}
        }
        , b);
    }

    private void foo(Runnable r,int a) {
        r.run();

    }
}
