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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.gene42.devops.shared.exceptions.Gene42Exception;
import com.gene42.devops.shared.utils.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public class JsonApiMapper {

    private ObjectMapper mapper;

    public JsonApiMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonApiBuilder map(Object obj, Class clazz) throws Gene42Exception {

        JsonApiResource classApiResourceAnnotation = (JsonApiResource) clazz.getAnnotation(JsonApiResource.class);

        JsonApiBuilder builder = new JsonApiBuilder();

        if (classApiResourceAnnotation == null) {
            return builder;
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

                JsonApiResource fieldJsonApiResourceAnnotation = field.getAnnotation(JsonApiResource.class);

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
        return builder;
    }

    private static String getRelationshipName(JsonApiResource jsonApiResource) {
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

    public static PropertyDescriptor[] getProperties(Class clazz) throws IntrospectionException {
       // Method[] methods = clazz.getDeclaredMethods();

       // for (Method method : methods) {
          //  method.
      //  }

        BeanInfo info = Introspector.getBeanInfo(clazz, Object.class);
        PropertyDescriptor[] props = info.getPropertyDescriptors();

        return props;
        /*for (PropertyDescriptor pd : props) {
            String name = pd.getName();
            Method getter = pd.getReadMethod();
            Class<?> type = pd.getPropertyType();

            //Object value = getter.invoke(bean);

           // System.out.println(String.format("name=[%s], getter=[%s], type=[%s]", name, getter.getName(), type));
        }*/
    }
}
