package frame;

import converter.Converter;
import java.util.BitSet;

public class FramePayload {
    private BitSet padLength = new BitSet(8);
    private BitSet padding = new BitSet();

    public void setPadLength(int length) {
        this.padLength = (new Converter()).longToBitSet(length);
    }

    public void setPadding(int padding) {
        this.padding = (new Converter()).longToBitSet(padding);
    }

    public BitSet getPadLength() {
        return padLength;
    }

    public BitSet getPadding() {
        return padding;
    }
}
