package com.rpc.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * 单例工厂
 */
public class SingletonFactory {
    // 存储类对应的单例对象
    private static Map<Class, Object> objectMap = new HashMap<>();

    // 私有构造方法，防止外部实例化
    private SingletonFactory() {}

    /**
     * 获取某个类的单例对象
     * @param clazz 要获取单例对象的类
     * @param <T> 泛型参数，表示类的类型
     * @return 类的单例对象
     */
    public static <T> T getInstance(Class<T> clazz) {
        Object instance = objectMap.get(clazz);
        synchronized (clazz) {
            if (instance == null) {
                try {
                    instance = clazz.newInstance();
                    objectMap.put(clazz, instance);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        // 将 Object 类型的实例转换为泛型类型并返回
        return clazz.cast(instance);
    }
}
