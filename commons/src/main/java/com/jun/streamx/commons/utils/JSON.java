package com.jun.streamx.commons.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.jun.streamx.commons.exception.JsonException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TimeZone;

/**
 * 基于 <strong>Jackson</strong>, 考虑使用此工具类替换 <strong>Fastjson</strong>
 *
 * @author Jun
 * @see ObjectMapper
 * @since 1.0.0
 */
public class JSON {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // LocalDateTime, LocalDate, LocalTime 三种类型序列化、反序列化配置
        var javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(Time.COMMON_DATE_TIME_FORMATTER));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(Time.COMMON_DATE_FORMATTER));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(Time.COMMON_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(Time.COMMON_DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(Time.COMMON_DATE_FORMATTER));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(Time.COMMON_TIME_FORMATTER));
        mapper.registerModule(javaTimeModule);

        // 时区
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        // 序列化及反序列化配置配置
        // 反序列化时，无视未知的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 序列化时，对象没有字段也不报错
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /**
     * 返回内部引用
     */
    public static ObjectMapper getObjectMapper() {
        return mapper;
    }


    /**
     * @see ObjectMapper#readTree(String)
     */
    public static JsonNode readTree(String content) {
        try {
            return mapper.readTree(content);
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * @see ObjectMapper#readTree(byte[])
     */
    public static JsonNode readTree(byte[] content) {
        try {
            return mapper.readTree(content);
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * @see ObjectMapper#treeToValue(TreeNode, Class)
     */
    public static <T> T treeToValue(JsonNode jsonNode, Class<T> clazz) {
        try {
            return mapper.treeToValue(jsonNode, clazz);
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * {@link #treeToValue(JsonNode, Class)} 泛型版本
     */
    public static <T> T treeToValue(JsonNode jsonNode, TypeReference<T> typeRef) {
        try {
            return mapper.readValue(mapper.treeAsTokens(jsonNode), mapper.getTypeFactory().constructType(typeRef));
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * @see ObjectMapper#createObjectNode()
     */
    public static ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

    /**
     * @see ObjectMapper#createArrayNode()
     */
    public static ArrayNode createArrayNode() {
        return mapper.createArrayNode();
    }

    /**
     * @see ObjectMapper#readValue(String, Class)
     */
    public static <T> T readValue(String content, Class<T> clazz) {
        try {
            return mapper.readValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * @see ObjectMapper#readValue(byte[], Class)
     */
    public static <T> T readValue(byte[] content, Class<T> clazz) {
        try {
            return mapper.readValue(content, clazz);
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * @see ObjectMapper#readValue(String, TypeReference)
     */
    public static <T> T readValue(String content, TypeReference<T> valueTypeRef) {
        try {
            return mapper.readValue(content, valueTypeRef);
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * @see ObjectMapper#readValue(byte[], TypeReference)
     */
    public static <T> T readValue(byte[] content, TypeReference<T> valueTypeRef) {
        try {
            return mapper.readValue(content, valueTypeRef);
        } catch (IOException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * @see ObjectMapper#writeValueAsBytes(Object)
     */
    public static byte[] writeValueAsBytes(Object value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * @see ObjectMapper#writeValueAsString(Object)
     */
    public static String writeValueAsString(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * 打印格式化 JSON 数据
     */
    public static String writeValueAsPrettyString(Object value) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * json 中可能存在嵌套对象，比如
     * <pre>
     * {
     *     ”user": {
     *         "name":"lewin",
     *         "age":2
     *     }
     * }
     * </pre>
     * <p>
     * 但我们仅仅只是想将 user 字段反序列化为 {@link String} 类型
     */
    public static class JsonNode2StringDeserializer extends JsonDeserializer<String> {

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectCodec codec = p.getCodec();
            TreeNode treeNode = codec.readTree(p);
            return treeNode.toString();
        }
    }
}
