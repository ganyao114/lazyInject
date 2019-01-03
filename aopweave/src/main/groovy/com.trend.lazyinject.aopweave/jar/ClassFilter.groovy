package com.trend.lazyinject.aopweave.jar

import com.android.build.api.transform.JarInput

import java.util.jar.JarEntry
import java.util.jar.JarFile;

public class ClassFilter {

    static boolean isIncludeFilterMatched(String str, List<String> filters) {
        return isFilterMatched(str, filters)
    }

    static boolean isFilterMatched(String str, List<String> filters) {
        if(str == null) {
            return false
        }

        if (filters == null || filters.isEmpty()) {
            return true
        }

        for (String s : filters) {
            if (isContained(str, s)) {
                return true
            }
        }

        return false
    }

    static boolean isContained(String str, String filter) {
        if (str == null) {
            return false
        }

        String filterTmp = filter
        if (str.contains(filterTmp)) {
            return true
        } else {
            if (filterTmp.contains("/")) {
                return str.contains(filterTmp.replace("/", File.separator))
            } else if (filterTmp.contains("\\")) {
                return str.contains(filterTmp.replace("\\", File.separator))
            }
        }

        return false
    }

    static int countOfFiles(File file) {
        if (file.isFile()) {
            return 1
        } else {
            File[] files = file.listFiles()
            int total = 0
            for (File f : files) {
                total += countOfFiles(f)
            }

            return total
        }
    }

    public static boolean filterClass(String classPath, List<String> includes) {
        return isIncludeFilterMatched(classPath, includes)
    }

    public static boolean filterJar(JarInput jarInput, List<String> includes) {
        if (includes == null || includes.isEmpty())
            return true
        boolean isInclude = false
        JarFile jarFile = new JarFile(jarInput.file)
        Enumeration<JarEntry> entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement()
            String entryName = jarEntry.getName()
            String tranEntryName = entryName.replace("/", ".").replace("\\", ".")
            if (isIncludeFilterMatched(tranEntryName, includes)) {
                isInclude = true
                break
            }
        }
        jarFile.close()
        return isInclude
    }
}
