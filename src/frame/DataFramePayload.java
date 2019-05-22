package frame;

import converter.Converter;

public class DataFramePayload extends FramePayload {
    private Converter converter;
    private String data;

    public DataFramePayload() {
        converter = new Converter();
    }

    public void setData(String str) {
        this.data = converter.stringToBinaryString(str);
        // TODO
    }

    public int getSize() {
        return super.getSize() + data.length();
    }

    public String getFrame() {
        return super.getPadLength() + this.data + super.getPadding();
    }
}
