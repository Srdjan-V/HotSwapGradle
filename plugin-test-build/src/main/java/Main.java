public class Main {
    public static void main(String[] args) {
        for (Props value : Props.values()) {
            System.out.println(System.getProperty(value.value));
        }
    }

    enum Props {
        JAVA_HOME("java.home"),
        JAVA_VERSION("java.version"),
        JAVA_VENDOR("java.vendor"),
        RUNTIME_NAME("java.runtime.name"),
        RUNTIME_VERSION("java.runtime.version"),
        VM_NAME("java.vm.name"),
        VM_VERSION("java.vm.version"),
        VM_VENDOR("java.vm.vendor"),
        OS_ARCH("os.arch"),
        Z_ERROR("Internal");

        private final String value;
        Props(String internal) {
            value = internal;
        }
    }
}