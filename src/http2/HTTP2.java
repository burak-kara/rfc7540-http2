package http2;

import frame.Frame;

import java.util.ArrayList;

public class HTTP2 {
    private Frame header;
    private Frame data;
    private ArrayList<Frame> frames = new ArrayList<>();

    public void createHeaderFrame() {
        header = new Frame();
        frames.add(header);
    }

    public void createDataFrame() {
        data = new Frame();
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
}
