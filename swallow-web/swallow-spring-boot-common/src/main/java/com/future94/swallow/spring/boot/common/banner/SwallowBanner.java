package com.future94.swallow.spring.boot.common.banner;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.security.CodeSource;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author weilai
 */
@Slf4j
@Order(LoggingApplicationListener.DEFAULT_ORDER + 1)
public class SwallowBanner implements ApplicationListener<ApplicationPreparedEvent> {

    private Logger logger = LoggerFactory.getLogger(SwallowBanner.class);

    private static final AtomicBoolean PRINTED = new AtomicBoolean(false);

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final String SWALLOW_BANNER = "\n" +
            "                    _ _               \n" +
            "  _____      ____ _| | | _____      __\n" +
            " / __\\ \\ /\\ / / _` | | |/ _ \\ \\ /\\ / /\n" +
            " \\__ \\\\ V  V / (_| | | | (_) \\ V  V / \n" +
            " |___/ \\_/\\_/ \\__,_|_|_|\\___/ \\_/\\_/  \n" +
            "                                      ";

    private static final String GITHUB_URL = "  https://github.com/future94/swallow";

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        if (PRINTED.compareAndSet(false, true)) {
            printBanner();
        }
    }

    private void printBanner() {
        String bannerText = LINE_SEPARATOR +
                SWALLOW_BANNER +
                LINE_SEPARATOR +
                " :: Swallow :: (v" +
                getVersion(getClass(), "1.0.0") +
                ")" + GITHUB_URL + LINE_SEPARATOR;
        if (logger.isInfoEnabled()) {
            logger.info(bannerText);
        } else {
            System.out.print(bannerText);
        }
    }

    private String getVersion(Class<?> cls, String defaultVersion) {
        String version = cls.getPackage().getImplementationVersion();
        if (StringUtils.hasText(version)) {
            version = cls.getPackage().getSpecificationVersion();
        }
        if (StringUtils.hasText(version)) {
            return version;
        }
        // guess version fro jar file name if nothing's found from MANIFEST.MF
        CodeSource codeSource = cls.getProtectionDomain().getCodeSource();

        if (codeSource == null) {
            log.info("No codeSource for class {} when getVersion, use default version {}", cls.getName(), defaultVersion);
            return defaultVersion;
        }
        String file = codeSource.getLocation().getFile();
        if (file != null && file.length() > 0 && file.endsWith(".jar")) {
            file = file.substring(0, file.length() - 4);
            int i = file.lastIndexOf('/');
            if (i >= 0) {
                file = file.substring(i + 1);
            }
            i = file.indexOf("-");
            if (i >= 0) {
                file = file.substring(i + 1);
            }
            while (file.length() > 0 && !Character.isDigit(file.charAt(0))) {
                i = file.indexOf("-");
                if (i < 0) {
                    break;
                }
                file = file.substring(i + 1);
            }
            version = file;
        }
        // return default version if no version info is found
        return !StringUtils.hasText(version) ? defaultVersion : version;
    }
}
