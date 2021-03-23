package com.halo.cache.utils;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author shoufeng
 */

public class KryoSerializerUtils {

	private static volatile KryoSerializerUtils kryoSerializerUtils;

	public static KryoSerializerUtils instance() {

		if (ObjectUtils.isEmpty(kryoSerializerUtils)) {
			synchronized (KryoSerializerUtils.class) {
				if (ObjectUtils.isEmpty(kryoSerializerUtils)) {
					kryoSerializerUtils = new KryoSerializerUtils();
					return kryoSerializerUtils;
				}
			}
		}

		return kryoSerializerUtils;
	}

	public static KryoSerializerUtils newInstance() {

		return new KryoSerializerUtils();
	}

	private ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {

		Kryo kryo = new Kryo();
		kryo.setRegistrationRequired(false);

		return kryo;

	});

	@SneakyThrows
	public <T> byte[] encode(T obj) {

		Kryo kryo = kryoThreadLocal.get();

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); Output output = new Output(bos);) {

			kryo.writeObjectOrNull(output, obj, obj.getClass());
			output.close();

			return bos.toByteArray();
		}

	}

	public Object decode(byte[] bytes) {
		return decode(bytes, Object.class);
	}

	@SneakyThrows
	public <T> T decode(byte[] bytes, Class<T> tClazz) {

		Kryo kryo = kryoThreadLocal.get();

		try (Input input = new Input(new ByteArrayInputStream(bytes));) {

			T obj = kryo.readObjectOrNull(input, tClazz);
			input.close();

			return obj;
		}

	}

}
