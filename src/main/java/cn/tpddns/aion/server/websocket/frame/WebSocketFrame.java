package cn.tpddns.aion.server.websocket.frame;

public class WebSocketFrame {
    boolean isLast = true;
    int rsv1 = 0;
    int rsv2 = 0;
    int rsv3;
    byte opcode;
    boolean mask;
    int payloadLen = 0;

    byte[] maskingKeyB;

    byte[] payloadB;

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public int getRsv1() {
        return rsv1;
    }

    public void setRsv1(int rsv1) {
        this.rsv1 = rsv1;
    }

    public int getRsv2() {
        return rsv2;
    }

    public void setRsv2(int rsv2) {
        this.rsv2 = rsv2;
    }

    public int getRsv3() {
        return rsv3;
    }

    public void setRsv3(int rsv3) {
        this.rsv3 = rsv3;
    }

    public byte getOpcode() {
        return opcode;
    }

    public void setOpcode(byte opcode) {
        this.opcode = opcode;
    }

    public boolean isMask() {
        return mask;
    }

    public void setMask(boolean mask) {
        this.mask = mask;
    }

    public int getPayloadLen() {
        return payloadLen;
    }

    public void setPayloadLen(int payloadLen) {
        this.payloadLen = payloadLen;
    }

    public byte[] getMaskingKeyB() {
        return maskingKeyB;
    }

    public void setMaskingKeyB(byte[] maskingKeyB) {
        this.maskingKeyB = maskingKeyB;
    }

    public byte[] getPayloadB() {
        return payloadB;
    }

    public void setPayloadB(byte[] payloadB) {
        this.payloadB = payloadB;
    }



}
