package frame;

import converter.Converter;

public class HeaderFramePayload extends FramePayload {
    private String headers;
    private String e;
    private String dependency;
    private String weight;
    private Converter converter;

    public HeaderFramePayload() {
        converter = new Converter();
    }

    public void setHeaders(int headers) {
        this.headers = converter.intToBinaryString(headers, 0); // TODO headers are string
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

    public String getHeaders() {
        return headers;
    }

    public String getE() {
        return e;
    }

    public String getDependency() {
        return dependency;
    }

    public String getWeight() {
        return weight;
    }

    public int getSize() {
        return super.getSize() +headers.length() + e.length() + dependency.length() + weight.length();
    }
}
