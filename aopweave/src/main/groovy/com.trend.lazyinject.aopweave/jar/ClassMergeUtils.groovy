package com.trend.lazyinject.aopweave.jar

import com.android.SdkConstants
import org.apache.commons.io.FileUtils

public class ClassMergeUtils {
    static void mergeJar(File sourceDir, File targetJar) {
        if (sourceDir == null) {
            throw new IllegalArgumentException("sourceDir should not be null")
        }

        if (targetJar == null) {
            throw new IllegalArgumentException("targetJar should not be null")
        }

        if (!targetJar.parentFile.exists()) {
            FileUtils.forceMkdir(targetJar.getParentFile())
        }

        FileUtils.deleteQuietly(targetJar)

        JarZip jarZip = new JarZip(targetJar)
        try {
            jarZip.setFilter(new JarZip.IZipEntryFilter() {
                @Override
                boolean checkEntry(String archivePath) throws JarZip.IZipEntryFilter.ZipAbortException {
                    return archivePath.endsWith(SdkConstants.DOT_CLASS)
                }
            })

            jarZip.addFolder(sourceDir)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            jarZip.close()
        }
    }

}
