package client_server.domain.packet;

import lombok.Data;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Data
public class Message {

    public static final int BYTES_WITHOUT_MESSAGE = Integer.BYTES + Integer.BYTES;
    public static final int MAX_MESSAGE_SIZE = 255;
    public static final int BYTES_MAX_SIZE = BYTES_WITHOUT_MESSAGE + MAX_MESSAGE_SIZE;

    public byte[] getMessage() { return message; }

    public Integer getbUserId() { return bUserId; }

    public Integer getcType() {
        return cType;
    }

    public enum cTypes {
        INSERT_PRODUCT,
        DELETE_PRODUCT,
        UPDATE_PRODUCT,
        GET_PRODUCT,
        GET_LIST_PRODUCTS,
        GET_PRODUCTS_STATISTICS,
        DELETE_ALL_IN_GROUP, // видалити всі продукти з даної групи
        INSERT_GROUP,
        DELETE_GROUP, // видалити всю групу і її рядочки
        UPDATE_GROUP,
        GET_GROUP,
        GET_LIST_GROUPS,
        LOGIN,
        ADD_USER,
    }

    byte[] whole;
    Integer cType;
    Integer bUserId;
    byte[] message;

    public Message(byte[] whole){

        this.whole = whole;

        try {
            byte[] decoded = DeEncriptor.decode(whole);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            this.cType = buffer.getInt();
            this.bUserId = buffer.getInt();
            message = new byte[decoded.length - 8];
            buffer.get(message);

        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException e) {
            e.printStackTrace();
        }
    }

    public Message(Integer cType, Integer bUserId, byte[] message) {
        this.cType = cType;
        this.bUserId = bUserId;
        this.message = message;

        this.whole = toPacketPart();
    }

    public byte[] toPacketPart() {
        byte[] msg = ByteBuffer.allocate(8 + message.length)
                .putInt(cType)
                .putInt(bUserId)
                .put(message).array();

        byte[] res = new byte[8 + message.length];

        try {
            res = DeEncriptor.encode(msg);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return res;
    }

    public int getMessageBytesLength() {
        return whole.length;
    }

}