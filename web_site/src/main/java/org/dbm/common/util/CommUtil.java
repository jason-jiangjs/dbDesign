package org.dbm.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 共通处理辅助类
 */
public final class CommUtil {

    private final static Logger logger = LoggerFactory.getLogger(CommUtil.class);

    private static final Pattern phoneNumPattern = Pattern.compile("^1[\\d]{10}$");

//    private static final Pattern passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[\\.!@#$]).{7,18}$");
    private static final Pattern passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z]).{6,18}$");


    public static boolean isStrongPassword(String password) {
        return passwordPattern.matcher(StringUtils.trim(password)).matches();
    }

    /**
     * 检查制定http url是否符合既定规则
     */
    public static boolean checkRequestUrlPattern(HttpServletRequest request, List<String> patterns) {
        boolean excludedRequestFound = false;
        if (patterns != null && patterns.size() > 0) {
            for (String pattern : patterns) {
                RequestMatcher matcher = new AntPathRequestMatcher(pattern);
                if (matcher.matches(request)) {
                    excludedRequestFound = true;
                    break;
                }
            }
        }

        return excludedRequestFound;
    }

    /**
     * 输出结果到httpresponse PrintWriter
     */
    public static void outToPrintWriter(ServletResponse response, Map<String, Object> data) {
        try {
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print(JacksonUtil.bean2Json(data));
            out.flush();
            out.close();
        } catch (Exception exp2) {
            logger.error("发送error信息(json)时异常", exp2);
        }
    }

    /**
     * 读取resources目录下的文件
     */
    public static String getFileString(Resource file) {
        FileInputStream fileInputStream = null;
        try {
            //2、建立数据通道
            fileInputStream = new FileInputStream(file.getFile());
            byte[] buf = new byte[1024];
            int length = 0;
            StringBuilder txt = new StringBuilder();
            //当文件读取到结尾时返回 -1,循环结束。
            while ((length = fileInputStream.read(buf)) != -1) {
                txt.append(new String(buf, 0, length));
            }
            return txt.toString();
        } catch (IOException ioexp) {
            logger.error("读取文件流时出错 file={}", file.getFilename(), ioexp);
            return null;
        } finally {
            //最后记得，关闭流
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    logger.error("关闭文件流时出错 file={}", file.getFilename(), e);
                }
            }
        }
    }
}
