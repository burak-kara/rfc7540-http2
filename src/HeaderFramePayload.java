import java.util.BitSet;

public class HeaderFramePayload extends FramePayload {
    private BitSet headers = new BitSet();
    private BitSet e = new BitSet(1);
    private BitSet dependency = new BitSet(31);
    private BitSet weight = new BitSet(8);


    public void setHeaders(int headers) {
        this.headers = (new Converter()).longToBitSet(headers);
    }

    public void setE(int e) {
        this.e = (new Converter()).longToBitSet(e);
    }

    public void setDependency(int dependency) {
        this.dependency = (new Converter()).longToBitSet(dependency);
    }

    public void setWeight(int weight) {
        this.weight = (new Converter()).longToBitSet(weight);
    }

    public BitSet getHeaders() {
        return headers;
    }

    public BitSet getE() {
        return e;
    }

    public BitSet getDependency() {
        return dependency;
    }

    public BitSet getWeight() {
        return weight;
    }
}
