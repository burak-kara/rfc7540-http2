package frame;

import converter.Converter;

public abstract class FramePayload {
    private String padLength;
    private String padding;
    private Converter converter;

    public FramePayload() {
        converter = new Converter();
    }

    public void setPadLength(int length) {
        this.padLength = converter.intToBinaryString(length, 8);
    }

    public void setPadding(int padding) {
        this.padding = converter.intToBinaryString(padding, 0); // TODO change 0
    }

    public String getPadLength() {
        return padLength;
    }

    public String getPadding() {
        return padding;
    }

    public int getSize() {
        return padLength.length() + padding.length();
    }

    public abstract String getFrame();
}
