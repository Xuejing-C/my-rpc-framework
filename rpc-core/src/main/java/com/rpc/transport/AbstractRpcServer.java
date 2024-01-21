package com.rpc.transport;

import com.rpc.annotation.Service;
import com.rpc.annotation.ServiceScan;
import com.rpc.enumeration.RpcError;
import com.rpc.exception.RpcException;
import com.rpc.provider.ServiceProvider;
import com.rpc.registry.ServiceRegistry;
import com.rpc.util.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;

public abstract class AbstractRpcServer implements RpcServer{
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String host;
    protected int port;

    protected ServiceRegistry serviceRegistry;
    protected ServiceProvider serviceProvider;

    public void scanServices() {
        // 获取启动类的类名
        String mainClassName = ReflectUtil.getStackTrace();
        Class<?> startClass;
        try {
            // 获取启动类的Class对象
            startClass = Class.forName(mainClassName);
            // 检查启动类是否标注了@ServiceScan注解
            if(!startClass.isAnnotationPresent(ServiceScan.class)) {
                logger.error("startup class lacks @ServiceScan annotation");
                throw new RpcException(RpcError.SERVICE_SCAN_PACKAGE_NOT_FOUND);
            }
        } catch (ClassNotFoundException e) {
            logger.error("occur unknown error");
            throw new RpcException(RpcError.UNKNOWN_ERROR);
        }
        // 获取@ServiceScan注解指定的扫描基础包
        String basePackage = startClass.getAnnotation(ServiceScan.class).value();
        // 如果未指定扫描基础包，则使用启动类所在包
        if("".equals(basePackage)) {
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));
        }
        // 获取指定包下的所有类
        Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);
        for(Class<?> clazz : classSet) {
            // 检查类是否标注了@Service注解
            if(clazz.isAnnotationPresent(Service.class)) {
                // 获取@Service注解上的服务名
                String serviceName = clazz.getAnnotation(Service.class).name();
                Object obj;
                try {
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("occur error when create " + clazz);
                    continue;
                }
                // 如果服务名为空，说明类可能实现了多个接口
                if("".equals(serviceName)) {
                    // 获取类实现的所有接口
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface: interfaces){
                        // 发布服务，使用接口的全限定名作为服务名
                        publishService(obj, oneInterface.getCanonicalName());
                    }
                } else {
                    // 如果服务名不为空，直接使用指定的服务名发布服务
                    publishService(obj, serviceName);
                }
            }
        }
    }

    @Override
    public <T> void publishService(T service, String serviceName) {
        serviceProvider.addServiceProvider(service, serviceName);
        serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
    }
}
