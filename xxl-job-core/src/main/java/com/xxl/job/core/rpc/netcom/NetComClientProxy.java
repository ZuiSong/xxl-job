package com.xxl.job.core.rpc.netcom;

import com.xxl.job.core.rpc.codec.RpcRequest;
import com.xxl.job.core.rpc.codec.RpcResponse;
import com.xxl.job.core.rpc.netcom.jetty.client.JettyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * rpc proxy
 * @author xuxueli 2015-10-29 20:18:32
 */
public class NetComClientProxy implements FactoryBean<Object> {
	private static final Logger logger = LoggerFactory.getLogger(NetComClientProxy.class);

	// ---------------------- config ----------------------
	private Class<?> iface;
	private String serverAddress;
	private String accessToken;
	private JettyClient client = new JettyClient();
	public NetComClientProxy(Class<?> iface, String serverAddress, String accessToken) {
		this.iface = iface;
		this.serverAddress = serverAddress;
		this.accessToken = accessToken;
	}

	@Override
	public Object getObject() throws Exception {
		return Proxy.newProxyInstance(Thread.currentThread()
				.getContextClassLoader(), new Class[] { iface },
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						// todo 代理业务service, 把业务类的classname,要执行的method放在request里, 接收请求的时候作出相应的动作, 下面是接收请求输出作出的动作
						/**
						 * @see NetComServerFactory#invokeService(com.xxl.job.core.rpc.codec.RpcRequest, java.lang.Object)
						 */
						// request
						RpcRequest request = new RpcRequest();
	                    request.setServerAddress(serverAddress);
	                    request.setCreateMillisTime(System.currentTimeMillis());
	                    request.setAccessToken(accessToken);
	                    request.setClassName(method.getDeclaringClass().getName());
	                    request.setMethodName(method.getName());
	                    request.setParameterTypes(method.getParameterTypes());
	                    request.setParameters(args);
						/**
						 * todo 构造请求参数后，调用了下面的方法发送具体请求，这里的请求参数很重要，尤其是className,methodName,parameters, 接收参数的时候会使用反射调用到真正的方法
						 * @see JettyClient#send(com.xxl.job.core.rpc.codec.RpcRequest)
						 */
						// send
	                    RpcResponse response = client.send(request);
	                    
	                    // valid response
						if (response == null) {
							logger.error(">>>>>>>>>>> xxl-rpc netty response not found.");
							throw new Exception(">>>>>>>>>>> xxl-rpc netty response not found.");
						}
	                    if (response.isError()) {
	                        throw new RuntimeException(response.getError());
	                    } else {
	                        return response.getResult();
	                    }
	                   
					}
				});
	}
	@Override
	public Class<?> getObjectType() {
		return iface;
	}
	@Override
	public boolean isSingleton() {
		return false;
	}

}
