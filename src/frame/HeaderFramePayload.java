package frame;

import converter.Converter;

public class HeaderFramePayload extends FramePayload {
    private String headers;
    private String e;
    private String dependency;
    private String weight;
    private String padLength;
    private String padding;
    private Converter converter;

    public HeaderFramePayload() {
        converter = new Converter();
    }

    public void setPadLength(int length) {
        this.padLength = converter.intToBinaryString(length, 8);
    }

    public void setPadding(int padding) {
        this.padding = converter.intToBinaryString(padding, 0);
    }

    public void setHeaders(String headers) {
        this.headers = converter.stringToBinaryString(headers);
    }

    public void setE(int e) {
        this.e = converter.intToBinaryString(e, 1);
    }

    public void setDependency(int dependency) {
        this.dependency = converter.intToBinaryString(dependency, 31);
    }

    public void setWeight(int weight) {
        this.weight = converter.intToBinaryString(weight, 8);
    }

    public int getSize() {
        return padLength.length() + padding.length() + headers.length() + e.length() + dependency.length() + weight.length();
    }

    public String getFrame() {
        return this.padLength + this.e + this.dependency + this.weight + this.headers + this.padding;
    }
}
