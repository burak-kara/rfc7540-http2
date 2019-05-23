package http2;

import frame.Frame;

import java.util.ArrayList;

public class HTTP2 {
    private Frame header;
    private Frame data;
    private Frame settings;
    private ArrayList<Frame> frames = new ArrayList<>();

    public void setHeader(Frame frame) {
        this.header = frame;
        frames.add(header);
    }

    public void setData(Frame frame) {
        this.data = frame;
        frames.add(data);
    }

    public void setSettings(Frame frame){
        this.settings = frame;
        frames.add(settings);
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
