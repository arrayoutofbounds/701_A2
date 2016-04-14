public class Test35 {

    private int a;

    public Test35() {
        a = 1;
        {
            int a = 1;
        }foo(new Runnable(){
        @Override
        public void run(){ 
            int a = 1;}
        }
        , a);
    }

    private void foo(Runnable r,int a) {
        r.run();

    }
}
