public class Test37 {

    private int x;

    public Test37() {
        foo();
    }

    private void foo(Runnable r) {
        int a = x;
        int b = y;
        int y = 123;
    }
}
