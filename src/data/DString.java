package data;

public class DString implements Variant{
    public final String content;
    public DString(String content) { this.content = content; }
    public boolean isTrue() {
        return !content.equals("");
    }

    @Override
    public DString operate(Variant other, OperationType type, OperationDirection direction) {
        if (direction != OperationDirection.POSITIVE) throw new UnsupportedOperationException("Strings can only be operated on with positive operations");

        if (other instanceof DString ds) {
            switch (type) {
                case OperationType.LINEAR: return new DString(content + ds.content);
                default: throw new UnsupportedOperationException("Only addition permitted as operation between strings");
            }
        }

        else if (other instanceof Measure measure && type == OperationType.PROPORTIONAL) {
            String result = "";
            int repeats = (int) Math.round(measure.magnitude);
            while (repeats > 0) {
                repeats--;
                result = result + content;
            }
            return new DString(result);
        }

        else if (type == OperationType.LINEAR) {
            return new DString(content + other.toString());
        }

        throw new UnsupportedOperationException("No resolution for operation: " + this + " cannot operate with " + other + " as " + type + "/" + direction);
    }
}
