public class Test38 {

    public Test38() {
        method1(x);
    }

    private static int x;

    public static void method1(int x) {
        method2();
    }

    public static void method2(Runnable r) {
        int x;
    }
}
