package frame;

import converter.Converter;

public class DataFramePayload extends FramePayload {
    private Converter converter;
    private String data;

    public DataFramePayload() {
        converter = new Converter();
    }

    public void setData(int data) {
        // TODO
    }

    public String getData() {
        return data;
    }
}
