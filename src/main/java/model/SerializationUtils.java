package model;

import java.io.*;

public class SerializationUtils {
    public static byte[] serialize(Object object ){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = null;
        try {
            objectOutput = new ObjectOutputStream(byteArrayOutputStream);
            objectOutput.writeObject(object);
            objectOutput.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static Object deserialize( byte[] data ){
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        try {
            ObjectInput objectInput = new ObjectInputStream(inputStream);
            return objectInput.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
