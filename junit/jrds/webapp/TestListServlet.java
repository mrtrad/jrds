package jrds.webapp;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import jrds.Tools;
import jrds.factories.xml.JrdsDocument;
import jrds.factories.xml.JrdsElement;
import jrds.factories.xml.NodeListIterator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestListServlet {
    static final private Logger logger = Logger.getLogger(TestListServlet.class);

    @BeforeClass
    static public void configure() throws Exception {
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.Slf4jLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "DEBUG");
        Tools.configure();
        Tools.prepareXml(false);
        Tools.setLevel(logger, Level.TRACE, Status.class.getName());
    }

    @Test 
    public void testListServlet() throws Exception
    {
        ServletTester tester = ToolsWebApp.getTestServer(new Properties());
        File cwd = new File (".");

        URLClassLoader cl = new URLClassLoader(new URL[] {cwd.toURI().toURL()});
        InputStream webxmlStream = cl.getResourceAsStream("web/WEB-INF/web.xml");
        JrdsDocument webxml = Tools.parseRessource(webxmlStream);
        for(JrdsElement n: new NodeListIterator<JrdsElement>(webxml, Tools.xpather.compile("/web-app/servlet/servlet-class"))) {
            String servletClassName = n.getTextContent().trim();
            @SuppressWarnings("unchecked")
            Class< ? extends HttpServlet> sclass = (Class<? extends HttpServlet>) getClass().getClassLoader().loadClass(servletClassName);
            tester.addServlet(sclass, "/" + sclass.getCanonicalName());
            logger.trace(sclass);
        }
        for(JrdsElement n: new NodeListIterator<JrdsElement>(webxml, Tools.xpather.compile("/web-app/listener/listener-class"))) {
            String className = n.getTextContent().trim();
            @SuppressWarnings("unchecked")
            Class<ServletContextListener> sclass = (Class<ServletContextListener>) getClass().getClassLoader().loadClass(className);
            logger.trace(sclass);
        }
        cl.close();
    }

}
