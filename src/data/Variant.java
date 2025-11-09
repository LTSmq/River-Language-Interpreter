package data;

public interface Variant {
    public boolean isTrue();
    public static final Variant DEFAULT = null;
    default public Variant operate(Variant other, OperationType type, OperationDirection direction) {
        throw new UnsupportedOperationException("Default of variant operation; not implemented");
    }
}
