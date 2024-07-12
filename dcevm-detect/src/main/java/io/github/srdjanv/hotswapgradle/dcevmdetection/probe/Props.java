package io.github.srdjanv.hotswapgradle.dcevmdetection.probe;

public enum Props {
    VM_NAME("java.vm.name"),
    VM_VENDOR("java.vm.vendor"),
    VM_VERSION("java.vm.version");

    private final String value;

    Props(String internal) {
        value = internal;
    }

    public String getValue() {
        return value;
    }
}
