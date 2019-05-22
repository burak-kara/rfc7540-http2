package frame;

import converter.Converter;

public class Frame {
    private String length;
    private String type;
    private String flags;
    private String r;
    private String streamIdentifier;
    private FramePayload framePayload;
    private Converter converter;

    public Frame() {
        converter = new Converter();
    }

    public void setLength(int len) {
        this.length = converter.intToBinaryString(len, 24);
    }

    public void setType(int type) {
        this.type = converter.intToBinaryString(type, 8);
    }

    public void setFlags(int flags) {
        this.flags = converter.intToBinaryString(flags, 8);
    }

    public void setR(int r) {
        this.r = converter.intToBinaryString(r, 1);
    }

    public void setStreamIdentifier(int streamIdentifier) {
        this.streamIdentifier = converter.intToBinaryString(streamIdentifier, 31);
    }

    public void setFramePayload(FramePayload framePayload) {
        this.framePayload = framePayload;
    }

    public String getFrame() {
        return this.length + this.type + this.flags + this.r + this.streamIdentifier + this.framePayload.getFrame();
    }
}
