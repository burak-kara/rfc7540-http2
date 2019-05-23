package frame;

import converter.Converter;

public class DataFramePayload extends FramePayload {
    private Converter converter;
    private String data;
    private String padLength;
    private String padding;

    public DataFramePayload() {
        converter = new Converter();
    }

    public void setPadLength(int length) {
        this.padLength = converter.intToBinaryString(length, 8);
    }

    public void setPadding(int padding) {
        this.padding = converter.intToBinaryString(padding, 0);
    }

    public void setData(String str) {
        this.data = converter.stringToBinaryString(str);
    }

    public int getSize() {
        return padding.length() + padLength.length() + data.length();
    }

    public String getFrame() {
        return this.padLength + this.data + this.padding;
    }
}
