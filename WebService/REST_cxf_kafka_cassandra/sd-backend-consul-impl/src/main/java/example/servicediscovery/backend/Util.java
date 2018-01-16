package example.servicediscovery.backend;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Util {

    public static ArrayList<?> json2Object(String json, String className) {

        ArrayList res = new ArrayList();
        if (json != null && !json.isEmpty()) {
            ArrayList<Map<String, Object>> tmp = (ArrayList<Map<String, Object>>) json2Map(json);
            for (Map map : tmp) {
                Object srv = null;
                try {
                    srv = Class.forName(className).newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                setFieldValue(srv, map);
                res.add(srv);

            }
        }
        return res;
    }


    public static void setFieldValue(Object obj, Map<String, Object> valuesMap) {

        Class<?> cls = obj.getClass();
        Method[] methods = cls.getDeclaredMethods();
        Field[] fields = cls.getDeclaredFields();

        for (Field field : fields) {
            try {

                String fieldSetName = parseSetName(field.getName());
                if (!checkSetMethod(methods, fieldSetName)) {
                    continue;
                }
                Method fieldSetMet = cls.getMethod(fieldSetName, field
                        .getType());
                    String value = valuesMap.get(parseKeyName(field.getName())).toString();
                    String fieldType = field.getType().getSimpleName();
                    if ("String".equals(fieldType))
                       fieldSetMet.invoke(obj, value);
                    else if (null != value && !"".equals(value)) {
                        if ("Date".equals(fieldType)) {
                            //TODO:
                        } else if ("Integer".equals(fieldType)
                                || "int".equals(fieldType)) {
                            Integer intVal = Integer.parseInt(value);
                            fieldSetMet.invoke(obj, intVal);
                        } else if ("Long".equalsIgnoreCase(fieldType)) {
                            Long temp = Long.parseLong(value);
                            fieldSetMet.invoke(obj, temp);
                        } else if ("Double".equalsIgnoreCase(fieldType)) {
                            Double temp = Double.parseDouble(value);
                            fieldSetMet.invoke(obj, temp);
                        } else if ("Boolean".equalsIgnoreCase(fieldType)) {
                            Boolean temp = Boolean.parseBoolean(value);
                            fieldSetMet.invoke(obj, temp);
                        } else if (field.getType().isEnum()) {
                            fieldSetMet.invoke(obj, Enum.valueOf((Class<Enum>) field.getType(), value));
                        } else if ("ArrayList".equalsIgnoreCase(fieldType)) {
                            ArrayList<String> values = (ArrayList<String>) valuesMap.get(parseKeyName(field.getName()));
                            fieldSetMet.invoke(obj, values);
                        }
                    }else {
                            System.out.println("not support type:" + fieldType);
                    }





            } catch (Exception e) {

                continue;
            }
        }
    }

    /* unused, replace a sample implement
        private static Object getEnumConstant(Field field, String value) throws ClassNotFoundException {
            Object res = null;
            Class<?> clz = Class.forName(field.getType().getName());
            Object[] consts = clz.getEnumConstants();
            for (Object obj :  consts) {
                if (value.equals(obj.toString())) {
                    res = obj;
                    break;
                }
            }
            return res;


        }
    */
    private static boolean checkSetMethod(Method[] methods, String fieldSetMet) {
        for (Method met : methods) {
            if (fieldSetMet.equals(met.getName())) {
                return true;
            }
        }
        return false;
    }

    private static String parseKeyName(String key) {
        if (null == key || "".equals(key)) {
            return null;
        }
        return key.substring(0, 1).toUpperCase()
                + key.substring(1);
    }

    private static String parseSetName(String fieldName) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        return "set" + fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1);
    }

    private static String parseGetName(String fieldName) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        return "get" + fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1);
    }

    private static Object json2Map(String jsonString) {

        Stack<Map> mapsStack = new Stack<>();
        Stack<List> listStack = new Stack<>();
        Stack<Boolean> isListStack = new Stack<>();
        Stack<String> keysStack = new Stack<>();
        int cur_int = 0;
        String key;
        Object value = null;
        StringBuilder builder = new StringBuilder();
        char[] cs = jsonString.toCharArray();

        for (int i = 0; i < cs.length; i++) {

            switch (cs[i]) {
                case '{': //begin a object
                    mapsStack.push(new HashMap());
                    isListStack.push(false);
                    cur_int ++;
                    break;
                case ':':
                    keysStack.push(builder.toString().trim());
                    builder = new StringBuilder();
                    break;
                case '[':   //begin a List
                    isListStack.push(true);
                    listStack.push(new ArrayList());
                    break;
                case ',':
                    boolean isList = isListStack.peek();

                    if (builder.length() > 0)
                        value = builder.toString().trim();
                    builder = new StringBuilder();
                    if (!isList) {
                        key = keysStack.pop();
                        mapsStack.peek().put(key, value);
                    } else
                        listStack.peek().add(value);
                    value = "";
                    break;
                case ']':
                    isListStack.pop();

                    if (builder.length() > 0)
                        value = builder.toString().trim();
                    builder = new StringBuilder();
                    listStack.peek().add(value);
                    value = listStack.pop();
                    break;
                case '}':
                    isListStack.pop();

                    if (builder.length() > 0)
                        value = builder.toString().trim();
                    builder = new StringBuilder();
                    if (keysStack.size() == cur_int){
                       key = keysStack.pop();
                       mapsStack.peek().put(key, value);
                    }
                    value = mapsStack.pop();
                    cur_int --;
                    break;
                case '"':
                    break;
                default:
                    builder.append(cs[i]);
                    break;
            }

        }
        //System.out.println(value);
        return value;
    }


}
