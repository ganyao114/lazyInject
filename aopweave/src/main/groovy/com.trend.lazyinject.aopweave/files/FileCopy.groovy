package com.trend.lazyinject.aopweave.files

import com.android.build.api.transform.Format
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.trend.lazyinject.aopweave.jar.ClassMergeUtils
import org.apache.commons.io.FileUtils

public class FileCopy {

    public static void fullCopy(Collection<TransformInput> inputs,
                                TransformOutputProvider outputProvider) {
        inputs.each {

            it.directoryInputs.each {
                File target = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.JAR)
                ClassMergeUtils.mergeJar(it.file, target)
            }

            it.jarInputs.each {
                File target = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.JAR)
                FileUtils.copyFile(it.file, target)
            }
        }

    }

    public static void incrementalCopy(Collection<TransformInput> inputs,
                                       TransformOutputProvider outputProvider) {

        inputs.each {

            it.directoryInputs.each {
                File target = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.JAR)
                ClassMergeUtils.mergeJar(it.file, target)
            }

            it.jarInputs.each {
                File target = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.JAR)
                switch (it.status) {
                    case Status.REMOVED:
                        FileUtils.forceDelete(target)
                        break
                    case Status.CHANGED:
                        FileUtils.forceDelete(target)
                        FileUtils.copyFile(it.file, target)
                        break
                    case Status.ADDED:
                        FileUtils.copyFile(it.file, target)
                        break
                    default:
                        break

                }
            }


        }
    }

}
