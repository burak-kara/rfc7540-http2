package frame;

import java.util.BitSet;

public class DataFramePayload extends FramePayload {
    private BitSet data = new BitSet();

    public void setData(int data) {
        this.data = (new Converter()).longToBitSet(data);
    }

    public BitSet getData() {
        return data;
    }
}
