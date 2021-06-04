package at.kaindorf.schnopsn.beans;

public class Message {
    private String type;
    private Object data;

    public Message(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public Message() {
    }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", data=" + data +
                '}';
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (type != null ? !type.equals(message.type) : message.type != null) return false;
        return data != null ? data.equals(message.data) : message.data == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }
}
