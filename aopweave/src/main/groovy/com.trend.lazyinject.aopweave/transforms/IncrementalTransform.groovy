package com.trend.lazyinject.aopweave.transforms

import com.android.SdkConstants
import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.android.utils.FileUtils
import com.google.common.io.Files
import com.trend.lazyinject.aopweave.classes.JavassistClassGetter
import javassist.ClassPool
import javassist.WeaveClassPool
import org.gradle.api.Project

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

abstract class IncrementalTransform extends Transform {

    Project project

    List<File> classPaths = new LinkedList<>()
    List<File> filesNeedInject = new LinkedList<>()

    ClassPool classPool

    IncrementalTransform(Project project) {
        this.project = project
        if (useJavassist()) {
            classPool = new WeaveClassPool(true)
        }
        //add system boot classpath
        project.android.bootClasspath.each {
            classPaths.add(it)
        }
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation invocation) throws IOException, TransformException, InterruptedException {
        //以下实现参考自com.android.build.gradle.internal.transforms.CustomClassTransform

        final TransformOutputProvider outputProvider = invocation.outputProvider
        assert outputProvider != null

        // Output the resources, we only do this if this is not incremental,
        // as the secondary file is will trigger a full build if modified.
        if (!invocation.isIncremental()) {
            outputProvider.deleteAll()
        }

        //transform 开始回调
        onTransformStart(invocation)
        //遍历输入文件，初始化相应参数并复制目标文件
        invocation.inputs.each { TransformInput input ->
            // 遍历目录
            input.directoryInputs.each { DirectoryInput dirInput ->
                final File outputDir = outputProvider.getContentLocation(
                        dirInput.name,
                        dirInput.contentTypes,
                        dirInput.scopes,
                        Format.DIRECTORY)
                // 遍历目录接口
                onEachDirectory(dirInput)
                if (invocation.isIncremental()) {
                    dirInput.changedFiles.each { File inputFile, Status status ->
                        switch (status) {
                            case Status.NOTCHANGED:
                                break
                            case Status.ADDED:
                            case Status.CHANGED:
                                //增量编译时改变包名的话，Input file 会出现 Directory
                                if (!inputFile.isDirectory() && inputFile.name.endsWith(SdkConstants.DOT_CLASS)) {
                                    // 真正需要处理的文件
                                    File outFile = toOutputFile(outputDir, dirInput.file, inputFile)
                                    onRealTransformFile(dirInput, inputFile, outFile)
                                }
                                break
                            case Status.REMOVED:
                                File outFile = toOutputFile(outputDir, dirInput.file, inputFile)
                                Logger.i("===========class file removed: ${outFile.absolutePath}")
                                FileUtils.deleteIfExists(outFile)
                                break
                        }
                    }
                } else {
                    FileUtils.getAllFiles(dirInput.file).each { File file ->
                        if (file.name.endsWith(SdkConstants.DOT_CLASS)) {
                            File outFile = toOutputFile(outputDir, dirInput.file, file)
                            // 真正需要处理的文件
                            onRealTransformFile(dirInput, file, outFile)
                        }
                    }
                }
            }

            //遍历jar
            input.jarInputs.each { JarInput jarInput ->
                Logger.d(jarInput.name + " status: " + jarInput.status)
                final File outJarFile = outputProvider.getContentLocation(
                        jarInput.name,
                        jarInput.contentTypes,
                        jarInput.scopes,
                        Format.JAR)
                // 遍历Jar接口
                //TODO 只添加非 Removed 的文件？
                onEachJar(jarInput)
                if (invocation.isIncremental()) {
                    switch (jarInput.getStatus()) {
                        case Status.NOTCHANGED:
                            break
                        case Status.ADDED:
                        case Status.CHANGED:
                            // 真正需要处理的 jar 文件
                            onRealTransformJar(jarInput, outJarFile)
                            break
                        case Status.REMOVED:
                            Logger.i("===========jar input: ${jarInput.file.absolutePath}")
                            Logger.i("===========jar removed: ${outJarFile.absolutePath}")
                            FileUtils.deleteIfExists(outJarFile)
                            break
                    }
                } else {
                    // 真正需要处理的 jar 文件
                    onRealTransformJar(jarInput, outJarFile)
                }
            }
        }

        //transform 结束回调
        onTransformEnd(invocation)

    }

    /**
     * For example:<p>
     *     outputDir is {@code /out/}, inputDir is {@code /in/}, inputFile is {@code /in/com/demo/a.class}.
     *     <p>so return {@code /out/com/demo/a.class}
     *
     * @param outputDir
     * @param inputDir
     * @param inputFile
     * @return
     */
    @NonNull
    static File toOutputFile(File outputDir, File inputDir, File inputFile) {
        return new File(outputDir, FileUtils.relativePossiblyNonExistingPath(inputFile, inputDir))
    }

    /**
     * Transform.transform() 接口开始时回调
     */
    void onTransformStart(TransformInvocation invocation) {}

    /**
     * 遍历 DirectoryInput 时回调
     * @param dirInput
     */
    void onEachDirectory(DirectoryInput dirInput) {
        classPaths.add(dirInput.file)
    }

    /**
     * 在非增量模式下：对所有输入文件回调；增量模式下：只对有变化的输入文件回调
     * @param dirInput 遍历目录时的 DirectoryInput
     * @param inputFile 遍历 DirectoryInput 时的 File
     * @param outputFile 根据 inputFile 创建的 File
     */
    void onRealTransformFile(DirectoryInput dirInput, File inputFile, File outputFile) {
        Files.createParentDirs(outputFile)
        FileUtils.copyFile(inputFile, outputFile)

        injectForFile(outputFile)
    }

    void onRealTransformJar(JarInput jarInput, File outJarFile) {
        Files.createParentDirs(outJarFile)
        FileUtils.copyFile(jarInput.file, outJarFile)

        if (filter(jarInput)) {
            injectForJar(outJarFile)
        }
    }

    /**
     * 遍历 JarInput 时回调
     * @param jarInput
     */
    void onEachJar(JarInput jarInput) {
        classPaths.add(jarInput.file)
    }

    /**
     * 在非增量模式下：对所有输入 Jar 回调；增量模式下：只对有变化的输入 Jar 文件回调
     * @param jarInput 遍历目录时的 JarInput
     * @param outJarFile 根据 JarInput 生成的 File
     */


    /**
     * Transform.transform() 接口结束时回调
     */
    void onTransformEnd(TransformInvocation invocation) {
        if (useJavassist()) {
            JavassistClassGetter.addClassPath(classPaths, classPool)
        }

    }

    abstract boolean useJavassist()

    boolean filter(String classPath) {
        return true
    }

    boolean filter(JarInput jarInput) {
        return true
    }

    abstract byte[] doInject(String className, InputStream inputStream)


    private static void injectForFile(File file) {
        final int index = file.absolutePath.indexOf(getName()) + getName()
        Closure handleFileClosure = { File innerFile ->
            String filePath = innerFile.absolutePath
            if (filter(filePath)) {
                def outputFile = new File(buildDir, TMP_DIR + innerFile.absolutePath.substring(index))
                Files.createParentDirs(outputFile)
                FileInputStream inputStream = new FileInputStream(innerFile)
                FileOutputStream outputStream = new FileOutputStream(outputFile)
                byte[] modified = injectForInputStream(innerFile.name, inputStream)
                outputStream.write(modified)
                inputStream.close()
                outputStream.close()

                Files.copy(outputFile, innerFile)
            } else {
//                Logger.i("skip class file:>> " + filePath)
            }
        }


        if (file.isDirectory()) {
            file.eachFileRecurse {
                handleFileClosure
            }
        } else {
            handleFileClosure.call(file)
        }
    }

    private void injectForJar(File outJarFile) {
        final int index = outJarFile.absolutePath.indexOf(getName()) + getName().length()
        final def tmpFile = new File(buildDir, TMP_DIR + outJarFile.absolutePath.substring(index))
        Files.createParentDirs(tmpFile)

        new ZipInputStream(new FileInputStream(outJarFile)).withCloseable { zis ->
            new ZipOutputStream(new FileOutputStream(tmpFile)).withCloseable { zos ->

                ZipEntry entry
                while ((entry = zis.getNextEntry()) != null) {
                    if (filter(entry.name)) {
                        byte[] modified = injectForInputStream(entry.name, zis)
                        zos.putNextEntry(new ZipEntry(entry.name))
                        zos.write(modified)
                    } else {
                        zos.putNextEntry(entry)
                        ByteStreams.copy(zis, zos)
                    }
                    zos.closeEntry()
                    zis.closeEntry()
                }
            }
        }

        Files.copy(tmpFile, outJarFile)
    }

}
