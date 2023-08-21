package com.jun.streamx.commons;

import com.jun.streamx.commons.exception.StreamxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

/**
 * 应用启动类, 该类提供两个功能
 * <ol>
 *    <li>自动生成可执行脚本，通过执行 {@code java -jar application.jar --parcel={app package path} {--spring.application.name=app-name}}.</li>
 *    <li>打印当前项目 http 协议接口资源树, 启动应用时加入参数 {@code --res-tree}.</li>
 * </ol>
 *
 * @author Jun
 * @since 1.0.0
 */
public class StreamxApp {

    private static final Logger log = LoggerFactory.getLogger(StreamxApp.class);
    private static final String DEFAULT_APP_PATH = "/streamx/app";
    private static final String APPLICATION_CONFIG_FILE_NAME = "application.yml";
    private static final String APPLICATION_NAME_KEY = "spring.application.name";

    /**
     * @see SpringApplication#run(Class, String...)
     */
    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
        var arguments = new String[args.length + 1];
        var needInitialize = false;
        for (int i = 0; i < arguments.length; i++) {
            if (i == args.length) {
                arguments[i] = "--spring.banner.location=classpath:streamx-banner.txt";
            } else {
                arguments[i] = args[i];
                if (arguments[i].startsWith("--parcel")) {
                    needInitialize = true;
                }
            }
        }

        // 不需要初始化，直接返回
        if (!needInitialize) {
            var ctx = SpringApplication.run(primarySource, arguments);
            var env = ctx.getEnvironment();
            var san = env.getProperty(APPLICATION_NAME_KEY);
            final ConfigurableApplicationContext context = ctx;

            // 日志打印
            Optional.ofNullable(primarySource.getAnnotation(EnableConfigurationProperties.class))
                    .map(EnableConfigurationProperties::value)
                    .stream()
                    .flatMap(Arrays::stream)
                    .forEach(clazz -> {
                        try {
                            var properties = context.getBean(clazz);
                            log.info("配置打印: {}", properties);
                        } catch (BeansException e) {
                            // do nothing
                        }
                    });
            var streamxName = env.getProperty("streamx.name");
            log.info("---「{}」启动完成---", StringUtils.hasText(streamxName) ? streamxName : san);

            // 有序调用监听器处理方法
            var listenerMap = context.getBeansOfType(ApplicationInitializedListener.class);
            listenerMap.values().stream()
                    .sorted(Comparator.comparingInt(ApplicationInitializedListener::order))
                    .forEach(ApplicationInitializedListener::onAppInitialized);
            return context;
        }

        // 抓取 applicationName
        String applicationName;
        String appPath = DEFAULT_APP_PATH;
        try {
            var yaml = new Yaml();
            var appConfigFileBytes = new ClassPathResource(APPLICATION_CONFIG_FILE_NAME).getInputStream().readAllBytes();
            LinkedHashMap<String, ?> load = yaml.load(new String(appConfigFileBytes, StandardCharsets.UTF_8));
            applicationName = Optional.of(load)
                    .map(l -> (LinkedHashMap<String, ?>) l.get("spring"))
                    .map(l -> (LinkedHashMap<String, ?>) l.get("application"))
                    .map(l -> (String) l.get("name"))
                    .orElse(null);

            // 尝试从 arguments 中获取参数 (arguments 优先度更高)
            for (String argument : arguments) {
                if (argument.startsWith("--spring.application.name=")) {
                    applicationName = splitValue(argument);
                }
                if (argument.startsWith("--parcel=")) {
                    appPath = splitValue(argument);
                }
            }
            if (applicationName == null) {
                log.error("spring.application.name 不能为空, 程序结束...");
                System.exit(0);
            }

            // 写入 application.yml
            Files.writeString(
                    new File(String.format("%s/%s/%s", appPath, applicationName, APPLICATION_CONFIG_FILE_NAME)).toPath(),
                    new String(appConfigFileBytes, StandardCharsets.UTF_8).replaceAll("\\r", ""),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new StreamxException("application.yml 读取或创建失败", e);
        }

        // 启动初始化
        initialize(applicationName, appPath);
        log.info("application [{}] initialize successful", applicationName);
        System.exit(0);

        return SpringApplication.run(primarySource, arguments);
    }

    /**
     * 初始化 app 运行脚本
     */
    public static void initialize(String applicationName, String appPath) {
        // 写入 systemd 文件
        var systemd = getParcelFileAsString("systemd.service");
        String systemdScript = String.format(
                systemd,
                applicationName,
                appPath,
                System.getProperty("java.version"),
                appPath,
                applicationName,
                applicationName
        );
        var serviceFile = new File(String.format("/etc/systemd/system/%s.service", applicationName));
        try {
            Files.writeString(
                    serviceFile.toPath(),
                    systemdScript,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new StreamxException("文件[%s]写入失败: %s", serviceFile.toPath(), e.getMessage());
        }

        // 脚本文件写入
        shellScriptInit(applicationName, appPath, "restart.sh");
        shellScriptInit(applicationName, appPath, "stop.sh");
        shellScriptInit(applicationName, appPath, "replace_and_restart.sh");
    }

    private static void shellScriptInit(String applicationName, String appPath, String scriptFile) {
        var restart = getParcelFileAsString(scriptFile);
        String restartScript = String.format(
                restart,
                applicationName
        );
        var restartFile = new File(String.format("%s/%s/%s", appPath, applicationName, scriptFile));
        try {
            Files.writeString(
                    restartFile.toPath(),
                    restartScript,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE
            );
            Files.setPosixFilePermissions(restartFile.toPath(), Set.of(
                    PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.GROUP_READ,
                    PosixFilePermission.GROUP_EXECUTE,
                    PosixFilePermission.OTHERS_READ,
                    PosixFilePermission.OTHERS_EXECUTE
            ));
        } catch (IOException e) {
            throw new StreamxException("文件[%s]写入失败: %s", restartFile.toPath(), e.getMessage());
        }
    }

    private static String getParcelFileAsString(String fileName) {
        try {
            final var res = new ClassPathResource(String.format("parcel/%s", fileName));
            byte[] bytes = res.getInputStream().readAllBytes();

            // 注意，由于 linux 与 windows 系统换行符不同，所以我们需要处理一下
            var str = new String(bytes, StandardCharsets.UTF_8);
            return str.replaceAll("\\r", "");
        } catch (IOException e) {
            throw new StreamxException("%s 读取失败: %s", fileName, e.getMessage());
        }
    }

    /**
     * 将 --spring.application.name=app-name 中的 value 分割出来
     *
     * @param argument 入参
     * @return 入参值
     */
    private static String splitValue(String argument) {
        if (!argument.contains("=")) {
            return "";
        }

        String[] split = argument.split("=");
        return split[1].trim();
    }
}
