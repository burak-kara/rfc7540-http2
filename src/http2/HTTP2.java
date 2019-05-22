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

<<<<<<< HEAD
    public void setData(Frame frame) {
        this.data = frame;
=======
    public void createDataFrame() {
        data = new Frame();
>>>>>>> parent of bb1ad5c... Added Server and Client Examples. Client must be fixed.
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
<<<<<<< HEAD

    public String getPacketAsString() {
        StringBuilder str = new StringBuilder();
        for (Frame frame: frames) {
            str.append(frame.getFrame());
        }
        return str.toString();
    }
=======
>>>>>>> parent of bb1ad5c... Added Server and Client Examples. Client must be fixed.
}
