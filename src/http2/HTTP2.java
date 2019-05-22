package http2;

import frame.Frame;

import java.util.ArrayList;

public class HTTP2 {
    private Frame header;
    private Frame data;
    private ArrayList<Frame> frames = new ArrayList<>();

    public void setHeader(Frame frame) {
        this.header = frame;
        frames.add(header);
    }

    public void setData(Frame frame) {
        this.data = frame;
        frames.add(data);
    }

    public Frame getHeader() {
        return header;
    }

    public Frame getData() {
        return data;
    }

    public ArrayList<Frame> getFrames() {
        return frames;
    }

    public String getPacketAsString() {
        StringBuilder str = new StringBuilder();
        for (Frame frame: frames) {
            str.append(frame.getFrame());
        }
        return str.toString();
    }
}
