package com.ta.util;

import javax.servlet.ServletContext;
import java.io.File;

/**

 * Resolves the CSV data directory for reads and writes in the TA Recruitment application.
 * <p>
 * Resolution follows a priority chain:
 * </p>
 * <ul>
 *   <li>{@code -Dta.recruitment.data.dir=...} system property overrides everything.</li>
 *   <li>When running {@code mvn tomcat7:run} from the module root, prefers
 *       {@code src/main/webapp/data} so edits in the IDE match runtime (avoids writing
 *       only under {@code target/}). Detection requires an existing {@code users.csv}
 *       marker file in the candidate directory.</li>
 *   <li>Otherwise uses {@code webappdata} next to the exploded webapp if present and
 *       contains {@code users.csv}, else {@code &lt;webapp&gt;/data}.</li>

 * Resolves the CSV data directory for reads/writes.
 * <ul>
 *   <li>{@code -Dta.recruitment.data.dir=...} overrides everything.</li>
 *   <li>When running {@code mvn tomcat7:run} from the module root, prefers
 *       {@code src/main/webapp/data} so edits in the IDE match runtime (avoids writing only under {@code target/}).</li>
 *   <li>Otherwise uses {@code webappdata} next to the exploded webapp if present, else {@code &lt;webapp&gt;/data}.</li>

 * </ul>
 */
public final class DataDirUtil {


    /**
     * JVM system property name used to override the data directory location.
     * Set via {@code -Dta.recruitment.data.dir=/path/to/data} at launch time.
     */

    public static final String DATA_DIR_PROPERTY = "ta.recruitment.data.dir";

    private DataDirUtil() {}


    /**
     * Determines the absolute filesystem path to the CSV data directory.
     *
     * @param ctx the servlet context used to locate the deployed web application root;
     *            may be {@code null}-safe via fallback when {@link ServletContext#getRealPath(String)}
     *            returns {@code null}
     * @return the absolute path to the directory containing CSV data files (never {@code null})
     */

    public static String resolve(ServletContext ctx) {
        String override = System.getProperty(DATA_DIR_PROPERTY);
        if (override != null && !override.trim().isEmpty()) {
            return new File(override.trim()).getAbsolutePath();
        }

        String cwd = System.getProperty("user.dir");
        if (cwd != null) {
            File[] devCandidates = {
                    new File(cwd, "src/main/webapp/data"),
                    new File(cwd, "ta-recruitment/src/main/webapp/data")
            };
            for (File srcWebData : devCandidates) {
                if (new File(srcWebData, "users.csv").isFile()) {
                    return srcWebData.getAbsolutePath();
                }
            }
        }

        String webappRoot = ctx.getRealPath("/");
        if (webappRoot == null) {
            return new File("data").getAbsolutePath();
        }
        File webapp = new File(webappRoot);
        File parent = webapp.getParentFile();
        if (parent != null) {
            File webappdata = new File(parent, "webappdata");
            File marker = new File(webappdata, "users.csv");
            if (webappdata.isDirectory() && marker.isFile()) {
                return webappdata.getAbsolutePath();
            }
        }
        return new File(webapp, "data").getAbsolutePath();
    }
}
