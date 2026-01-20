package ru.practicum.kafka;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;


public class GenericAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {
    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private Schema schema;

    public GenericAvroDeserializer(Schema schema) {
        this.schema = schema;
    }

    public GenericAvroDeserializer() {
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) return null;
        try {
            DatumReader<T> reader = new SpecificDatumReader<>(schema, schema, SpecificData.getForSchema(schema));
            BinaryDecoder decoder = decoderFactory.binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (Exception e) {
            throw new SerializationException("Ошибка десериализации Avro", e);
        }
    }
}
