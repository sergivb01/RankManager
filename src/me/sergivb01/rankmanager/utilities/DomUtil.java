package me.sergivb01.rankmanager.utilities;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class DomUtil {
    public DomUtil() {
        super();
    }

    public static Document parse(File file) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        Document original = saxBuilder.build(file);
        ArrayList toInclude = Lists.newArrayList();
        for (Element include2 : original.getRootElement().getChildren("include")) {
            toInclude.add(include2.getAttributeValue("src"));
        }
        Iterator iterator = toInclude.iterator();
        while (iterator.hasNext()) {
            String include = (String)iterator.next();
            boolean found = false;
            File path = file.getParentFile();
            File including = new File(path, include);
            if (including.exists()) {
                found = true;
                try {
                    for (Element element : DomUtil.parse(including).getRootElement().getChildren()) {
                        original.getRootElement().addContent(element.clone().detach());
                    }
                }
                catch (IOException var9_14) {}
            } else {
                while (include.startsWith("../")) {
                    include = include.replace("../", "");
                }
                including = new File(path, include);
                if (including.exists()) {
                    found = true;
                    try {
                        for (Element element : DomUtil.parse(including).getRootElement().getChildren()) {
                            original.getRootElement().addContent(element.clone().detach());
                        }
                    }
                    catch (IOException var9_15) {
                        // empty catch block
                    }
                }
                if ((including = new File(path.getParentFile(), include)).exists()) {
                    found = true;
                    try {
                        for (Element element : DomUtil.parse(including).getRootElement().getChildren()) {
                            original.getRootElement().addContent(element.clone().detach());
                        }
                    }
                    catch (IOException var9_16) {
                        // empty catch block
                    }
                }
            }
            if (found) continue;
            Bukkit.getLogger().log(Level.WARNING, "File '" + including.getName() + "' was not found nor included!");
        }
        return original;
    }
}

