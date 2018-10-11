package com.gene42.commons.utils.json;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.gene42.devops.shared.exceptions.Gene42Exception;
import com.gene42.devops.shared.utils.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public class JsonApiMapper {

    private ObjectMapper mapper;

    private static Cache<Class, Map<String, PropertyDescriptor>> PROPERTY_DESCRIPTOR_MAP =
        CacheBuilder.newBuilder().maximumSize(100).build();

    public JsonApiMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonApiResourceBuilder map(Object obj, Class clazz, JsonApiBuilder builder) throws Gene42Exception {

        JsonApiResource classApiResourceAnnotation = (JsonApiResource) clazz.getAnnotation(JsonApiResource.class);

        //JsonApiBuilder builder = new JsonApiBuilder();

        if (classApiResourceAnnotation == null) {
            return null;
        }

        Map<String, PropertyDescriptor> propMap = new HashMap<>();

        try {
            for (PropertyDescriptor propertyDescriptor : getProperties(clazz)) {
                propMap.put(propertyDescriptor.getName(), propertyDescriptor);
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        JsonApiResourceBuilder data = new JsonApiResourceBuilder().setType(classApiResourceAnnotation.type());

        builder.addData(data);
        //propMap.forEach((k, v) -> System.out.println(String.format("%s=%s", k, v.getName())));

        List<Field> fields = Arrays
            .stream(Optional.ofNullable(clazz.getDeclaredFields()).orElse(new Field[0]))
            .filter(f -> !Modifier.isStatic(f.getModifiers()))
            .collect(Collectors.toList()); //Utils.getAllDeclaredNonStaticFields(obj.getClass(), clazz);

        for (Field field : fields) {
            //System.out.println(String.format("Field Name = %s", field.getName()));

            if (propMap.containsKey(field.getName())) {

                JsonApiRelationship fieldJsonApiResourceAnnotation = field.getAnnotation(JsonApiRelationship.class);

                Method getter = propMap.get(field.getName()).getReadMethod();
                Object result = null;
                try {
                    result = getter.invoke(obj);
                    //System.out.println(String.format("Result of %s() = %s", getter.getName(), result));

                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

                result = Optional.ofNullable(result).orElse(NullNode.getInstance());

                if (fieldJsonApiResourceAnnotation == null) {
                    try {
                        String qweqwe = this.mapper.writeValueAsString(result);
                        try {
                            JSONObject value = new JSONObject(qweqwe);
                            data.putAttributes(value);
                        } catch (JSONException e1) {
                            data.putAttribute(field.getName(), result);
                        }

                        System.out.println(String.format("qweqwe=[%s]", qweqwe));
                        //data.putAttribute(field.getName(), qweqwe);

                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                } else {

                    Object id = getValueOfFieldWithAnnotation(result, JsonApiId.class);

                    data.addRelationship(
                        getRelationshipName(fieldJsonApiResourceAnnotation),
                        String.valueOf(id),
                        fieldJsonApiResourceAnnotation.type()
                    );
                }
            }
        }

        //clazz.getDe

        //Utils.getClassAnnotation(obj, JsonApiResource)
        return data;
    }

    private void handleData(JsonApiBuilder builder, Object obj, Class clazz) throws ExecutionException {

        JsonApiResource classApiResourceAnnotation = (JsonApiResource) clazz.getAnnotation(JsonApiResource.class);
        Map<String, PropertyDescriptor> propMap = getPropMap(clazz);

        JsonApiResourceBuilder data = new JsonApiResourceBuilder().setType(classApiResourceAnnotation.type());

        // PROPERTY_DESCRIPTOR_MAP.get(builder.getClass(), () -> getPropertyDescriptorMap(builder.getClass()));
    }

    private static Map<String, PropertyDescriptor> getPropMap(Class clazz) throws ExecutionException {
        return PROPERTY_DESCRIPTOR_MAP.get(clazz, () -> createPropertyDescriptorMap(clazz));
    }

    private static String getRelationshipName(JsonApiRelationship jsonApiResource) {
        String name = jsonApiResource.relationshipName();
        return StringUtils.isBlank(name) ? jsonApiResource.type() : name;
    }

   /* private static <T extends Annotation> Field getFieldWithAnnotation(Object obj, Class<T> clazz) {

    }*/
    public static <T extends Annotation> Object getValueOfFieldWithAnnotation(Object result, Class<T> clazz) {
        Map<String, PropertyDescriptor> propMap = new HashMap<>();

        try {
            for (PropertyDescriptor propertyDescriptor : getProperties(result.getClass())) {
                propMap.put(propertyDescriptor.getName(), propertyDescriptor);
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        List<Field> annotatedFields = Utils.getAnnotatedFields(result, clazz);

        if (annotatedFields.isEmpty()) {
            return null;
        }
        PropertyDescriptor descriptor = propMap.get(annotatedFields.get(0).getName());

        if (descriptor == null) {
            return null;
        }

        try {
            return descriptor.getReadMethod().invoke(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
        //Utils.getFieldValue(annotatedFields.get(0), result);
    }


    public static Map<String, PropertyDescriptor> createPropertyDescriptorMap(Class clazz) throws IntrospectionException {
        Map<String, PropertyDescriptor> propMap = new HashMap<>();

        for (PropertyDescriptor propertyDescriptor : getProperties(clazz)) {
            propMap.put(propertyDescriptor.getName(), propertyDescriptor);
        }

        return propMap;
    }

    public static PropertyDescriptor[] getProperties(Class clazz) throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(clazz, Object.class);
        return info.getPropertyDescriptors();
    }
}
