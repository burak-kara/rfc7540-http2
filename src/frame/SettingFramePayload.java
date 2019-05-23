package frame;

import converter.Converter;

public class SettingFramePayload extends FramePayload {
    private String identifier;
    private String value;
    private Converter converter;

    public SettingFramePayload() {
        this.converter = new Converter();
    }

    public void setIdentifier(int identifier) {
        this.identifier = converter.intToBinaryString(identifier, 16);
    }

    public void setValue(int value) {
        this.value = converter.intToBinaryString(value, 32);
    }

    public int getSize() {
        return identifier.length() + value.length();
    }

    @Override
    public String getFrame() {
        return this.identifier + this.value;
    }
}
