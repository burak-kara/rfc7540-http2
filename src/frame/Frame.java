package frame;

import converter.Converter;

import java.util.BitSet;

public class Frame {
    private BitSet length = new BitSet(24);
    private BitSet type = new BitSet(8);
    private BitSet flags = new BitSet(8);
    private BitSet r = new BitSet(1);
    private BitSet streamIdentifier = new BitSet(31);
    private FramePayload framePayload;

    public Frame() {
       // test();
    }

    private void test() {
        length.set(0);
        length.set(1);
        System.out.println((new Converter()).bitSetToLong(length));
        System.out.println((new Converter()).longToBitSet(10));
    }

    public void setLength(int len) {
        this.length = (new Converter()).longToBitSet(len);
    }

    public void setType(int type) {
        this.type = (new Converter()).longToBitSet(type);
    }

    public void setFlags(int flags) {
        this.flags = (new Converter()).longToBitSet(flags);
    }

    public void setR(int r) {
        this.r = (new Converter()).longToBitSet(r);
    }

    public void setStreamIdentifier(int streamIdentifier) {
        this.streamIdentifier = (new Converter()).longToBitSet(streamIdentifier);
    }

    public void setFramePayload(FramePayload framePayload) {
        this.framePayload = framePayload;
    }

    public BitSet getLength() {
        return length;
    }

    public BitSet getType() {
        return type;
    }

    public BitSet getFlags() {
        return flags;
    }

    public BitSet getR() {
        return r;
    }

    public BitSet getStreamIdentifier() {
        return streamIdentifier;
    }

    public FramePayload getFramePayload() {
        return framePayload;
    }
}
