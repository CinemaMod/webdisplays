package net.montoyo.wd.net;

import net.minecraft.network.FriendlyByteBuf;
import net.montoyo.wd.utilities.Vector3i;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BufferUtils {
	public static void writeUShort(FriendlyByteBuf buf, int v) {
		// TODO: write a pair of bytes manually
		buf.writeInt(v);
	}
	
	public static int readUShort(FriendlyByteBuf buf) {
		return buf.readInt();
	}
	
	public static void writeBytes(FriendlyByteBuf buf, byte[] data) {
		writeUShort(buf, data.length);
		for (byte datum : data) buf.writeByte(datum);
	}
	
	public static byte[] readBytes(FriendlyByteBuf buf) {
		byte[] data = new byte[readUShort(buf)];
		for (int i = 0; i < data.length; i++) data[i] = buf.readByte();
		return data;
	}
	
	public static void writeVec3i(FriendlyByteBuf buf, Vector3i pos) {
		buf.writeInt(pos.x);
		buf.writeInt(pos.y);
		buf.writeInt(pos.z);
	}
	
	public static Vector3i readVec3i(FriendlyByteBuf buf) {
		return new Vector3i(buf.readInt(), buf.readInt(), buf.readInt());
	}
	
	public static void writeEnum(FriendlyByteBuf buf, Enum<?> value, byte byteCount) {
		int ord = 0;
		if (value != null) ord = value.ordinal() + 1;
		switch (byteCount) {
			case 1 -> buf.writeByte(ord);
			case 2 -> buf.writeShort(ord);
			case 4 -> buf.writeInt(ord);
			default -> throw new RuntimeException("Invalid byte count " + byteCount + ". Must be 1, 2... and 4");
		}
	}
	
	public static <T> void writeArray(FriendlyByteBuf buf, T[] elements, Consumer<T> writer) {
		writeUShort(buf, elements.length);
		for (T element : elements) writer.accept(element);
	}
	
	public static <T> T[] readArray(FriendlyByteBuf buf, Supplier<T> reader) {
		//noinspection unchecked
		T[] ts = (T[]) new Object[readUShort(buf)];
		for (int i = 0; i < ts.length; i++) ts[i] = reader.get();
		return ts;
	}
	
	public static Enum<?> readEnum(FriendlyByteBuf buf, Function<Integer, Enum<?>> mapper, byte byteCount) {
		int ord = switch (byteCount) {
			case 1 -> buf.readByte();
			case 2 -> buf.readShort();
			case 4 -> buf.readInt();
			default -> throw new RuntimeException("Invalid byte count " + byteCount + ". Must be 1, 2, or 4");
		};
		if (ord == 0) return null;
		
		return mapper.apply(ord - 1);
	}
}
