package com.coderzxh.web.config;

import com.coderzxh.service.base.IDeviceService;
import com.coderzxh.web.annotation.Pass;
import com.coderzxh.common.base.Constant;
import com.coderzxh.common.util.ComUtil;
import com.coderzxh.web.protobuf.codec.GBT32960Decoder;
import com.coderzxh.web.protobuf.codec.GBT32960Encoder;
import com.coderzxh.web.protobuf.handler.ProtocolHandler;
import com.coderzxh.web.protobuf.handler.ServerIdleStateTrigger;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 启动时一块启动
 */

@Component
@Slf4j
public class MyCommandLineRunner implements CommandLineRunner, EnvironmentAware {


	@Value("${controller.scanPackage}")
	private String scanPackage;

	@Value("${server.servlet.context-path}")
	private String contextPath;

	@Override
	public void run(String... args) throws Exception {
		doScanner(scanPackage);
		Set<String> urlAndMethodSet  =new HashSet<>();
		for (String aClassName:Constant.METHOD_URL_SET) {
			Class<?> clazz = Class.forName(aClassName);
			String baseUrl = "";
			String[] classUrl ={};
			if(!ComUtil.isEmpty(clazz.getAnnotation(RequestMapping.class))){
				classUrl=clazz.getAnnotation(RequestMapping.class).value();
			}
			Method[] methods = clazz.getMethods();
			for (Method method:methods) {
				if(method.isAnnotationPresent(Pass.class)){
					String [] methodUrl = null;
					StringBuilder sb  =new StringBuilder();
					if(!ComUtil.isEmpty(method.getAnnotation(PostMapping.class))){
						methodUrl=method.getAnnotation(PostMapping.class).value();
						if(ComUtil.isEmpty(methodUrl)){
							methodUrl=method.getAnnotation(PostMapping.class).path();
						}
						baseUrl=getRequestUrl(classUrl, methodUrl, sb,"POST");
					}else if(!ComUtil.isEmpty(method.getAnnotation(GetMapping.class))){
						methodUrl=method.getAnnotation(GetMapping.class).value();
						if(ComUtil.isEmpty(methodUrl)){
							methodUrl=method.getAnnotation(GetMapping.class).path();
						}
						baseUrl=getRequestUrl(classUrl, methodUrl, sb,"GET");
					}else if(!ComUtil.isEmpty(method.getAnnotation(DeleteMapping.class))){
						methodUrl=method.getAnnotation(DeleteMapping.class).value();
						if(ComUtil.isEmpty(methodUrl)){
							methodUrl=method.getAnnotation(DeleteMapping.class).path();
						}
						baseUrl=getRequestUrl(classUrl, methodUrl, sb,"DELETE");
					}else if(!ComUtil.isEmpty(method.getAnnotation(PutMapping.class))){
						methodUrl=method.getAnnotation(PutMapping.class).value();
						if(ComUtil.isEmpty(methodUrl)){
							methodUrl=method.getAnnotation(PutMapping.class).path();
						}
						baseUrl=getRequestUrl(classUrl, methodUrl, sb,"PUT");
					}else {
						methodUrl=method.getAnnotation(RequestMapping.class).value();
						baseUrl=getRequestUrl(classUrl, methodUrl, sb,RequestMapping.class.getSimpleName());
					}
					if(!ComUtil.isEmpty(baseUrl)){
						urlAndMethodSet.add(baseUrl);
					}
				}
			}
		}
		Constant.METHOD_URL_SET=urlAndMethodSet;
		log.info("@Pass:"+urlAndMethodSet);
		deviceStateInit();
		runNetty();
	}

	private String  getRequestUrl(String[] classUrl, String[] methodUrl, StringBuilder sb,String requestName) {
		sb.append(contextPath);
		if(!ComUtil.isEmpty(classUrl)){
            for (String url:classUrl) {
                sb.append(url+"/");
            }
        }
		for (String url:methodUrl) {
            sb.append(url);
        }
        if(sb.toString().endsWith("/")){
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString().replaceAll("/+", "/")+":--:"+requestName;
	}

	private void doScanner(String packageName) {
		//把所有的.替换成/
		URL url  =this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
		// 是否循环迭代
		if(StringUtils.countMatches(url.getFile(), ".jar")>0){
			boolean recursive=true;
			JarFile jar;
			// 获取jar
			try {
				jar = ((JarURLConnection) url.openConnection())
						.getJarFile();
				// 从此jar包 得到一个枚举类
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					// 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
					JarEntry entry = entries.nextElement();
					String name = entry.getName();
					// 如果是以/开头的
					if (name.charAt(0) == '/') {
						// 获取后面的字符串
						name = name.substring(1);
					}
					// 如果前半部分和定义的包名相同
					if (name.startsWith(packageName.replaceAll("\\.","/"))) {
						int idx = name.lastIndexOf('/');
						// 如果以"/"结尾 是一个包
						if (idx != -1) {
							// 获取包名 把"/"替换成"."
							packageName = name.substring(0, idx)
									.replace('/', '.');
						}
						// 如果可以迭代下去 并且是一个包
						if ((idx != -1) || recursive) {
							// 如果是一个.class文件 而且不是目录
							if (name.endsWith(".class")
									&& !entry.isDirectory()) {
								// 去掉后面的".class" 获取真正的类名
								String className = name.substring(
										packageName.length() + 1, name
												.length() - 6);
								try {
									// 添加到classes
									Constant.METHOD_URL_SET.add(Class
											.forName(packageName + '.'
													+ className).getName());
								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File dir = new File(url.getFile());
		for (File file : dir.listFiles()) {
			if(file.isDirectory()){
				//递归读取包
				doScanner(packageName+"."+file.getName());
			}else{
				String className =packageName +"." +file.getName().replace(".class", "");
				Constant.METHOD_URL_SET.add(className);
			}
		}
	}

	private Environment environment;

	@Autowired
	private ProtocolHandler protocolHandler;
	@Autowired
	private ServerIdleStateTrigger serverIdleStateTrigger;
	@Autowired
	private IDeviceService deviceService;

	private static final int LISTEN_PORT = 32960;

	private void runNetty()throws Exception{

		if(Objects.equals(environment.getActiveProfiles()[0], "testJunit")){
			log.info("单元测试,不执行netty");
			return;
		}

		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup);
			serverBootstrap.channel(NioServerSocketChannel.class);

			Bootstrap clientBoot = new Bootstrap();
			clientBoot.group(workerGroup);
			serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline()
							.addLast("logging",new LoggingHandler("INFO"))
							.addLast(new IdleStateHandler(60, 60, 60, TimeUnit.SECONDS))
							.addLast(serverIdleStateTrigger)
							.addLast(new CombinedChannelDuplexHandler<>(new GBT32960Decoder(), new GBT32960Encoder()))
							.addLast(protocolHandler);
				}
			});
//            serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);

			ChannelFuture f = serverBootstrap.bind(LISTEN_PORT).sync();
			log.info("server listened on {}", LISTEN_PORT);
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}


	/**
	 * 开机则更新所有设备为下线状态
	 * @throws Exception
	 */
	private void deviceStateInit()throws Exception{
        if(!Objects.equals(environment.getActiveProfiles()[0], "prod")){
            log.info("非生产环境,不执行设备初始化");
            return;
        }
		deviceService.updateDeviceOffline();
		log.info("设备状态初始化成功");
	}
}
