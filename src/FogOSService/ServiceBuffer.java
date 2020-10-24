package FogOSService;

public class ServiceBuffer {
    byte[] buf;
    int capacity;
    int nextRead;
    int nextWrite;
    ServiceBufferErrors lastError;

    public ServiceBuffer(int capacity) {
        buf = new byte[capacity];
        this.capacity = capacity;
        this.nextRead = 0;
        this.nextWrite = 0;
        this.lastError = ServiceBufferErrors.NONE;
    }

    public int readFromBuffer(byte[] buf, int max) {
        int ret;

        if (max < this.remaining()) {
            this.lastError = ServiceBufferErrors.INSUFFICIENT_MEMORY;
            return -1;
        }

        if (this.nextRead < this.nextWrite) {
            System.arraycopy(this.buf, this.nextRead, buf, 0, this.nextWrite - this.nextRead);
            ret = this.nextWrite - this.nextRead;
        } else {
            int tmp = this.capacity - this.nextRead;
            System.arraycopy(this.buf, this.nextRead, buf, 0, tmp);
            System.arraycopy(this.buf, 0, buf, tmp, this.nextWrite);
            ret = tmp + nextWrite;
        }
        this.nextRead = this.nextWrite;
        return ret;
    }

    public int writeToBuffer(byte[] buf, int len) {
        int ret;

        if (len > this.capacity) {
            this.lastError = ServiceBufferErrors.INSUFFICIENT_MEMORY;
            return -1;
        }

        if (len < this.capacity - this.nextWrite) {
            System.arraycopy(buf, 0, this.buf, this.nextWrite, len);
            this.nextWrite = this.nextWrite + len;
        } else {
            int tmp = this.capacity - this.nextWrite;
            System.arraycopy(buf, 0, this.buf, this.nextWrite, tmp);
            System.arraycopy(buf, tmp, this.buf, 0, len - tmp);
            this.nextWrite = len - tmp;
        }

        ret = len;
        return ret;
    }

    public int getCapacity() {
        return capacity;
    }

    public int remaining() {
        int written;

        if (this.nextRead <= this.nextWrite) {
            written = this.nextWrite - this.nextRead;
        } else {
            written = this.capacity - (this.nextRead - this.nextWrite);
        }

        return this.capacity - written;
    }

    public void getLastError() {
        lastError.printErrorMessage();
    }

    public boolean hasInput() {
        return this.nextWrite != this.nextRead;
    }
}

enum ServiceBufferErrors {
    NONE("No error"),
    INSUFFICIENT_MEMORY("insufficient memory");

    String err;

    ServiceBufferErrors(String msg) {

    }

    void printErrorMessage() {
        System.out.println("Error> " + err);
    }
}
