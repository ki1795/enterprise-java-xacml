package tests.an.xacml.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

public class TestUtil {
    public static String getFileFromClassPath(String fileName) throws FileNotFoundException {
        File fSchema = new File(fileName);
        if (fSchema.exists() && fSchema.isFile()) {
            return fileName;
        }

        // we got an absolute path or a relative path, so we don't try to load it from classpath.
        if (!fileName.equalsIgnoreCase(fSchema.getName())) {
            throw new FileNotFoundException("Can not find file '" + fileName + "'");
        }

        URL url = TestUtil.class.getResource(fileName);
        if (url != null) {
            return url.getFile();
        }

        url = ClassLoader.getSystemResource(fileName);
        if (url != null) {
            return url.getFile();
        }

        url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        if (url != null) {
            return url.getFile();
        }
        throw new FileNotFoundException("Can not load file '" + fileName + "'");
    }
}