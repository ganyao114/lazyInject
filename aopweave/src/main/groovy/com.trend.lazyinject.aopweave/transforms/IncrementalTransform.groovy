package com.trend.lazyinject.aopweave.transforms

import com.android.SdkConstants
import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.android.utils.FileUtils
import com.google.common.io.ByteStreams
import com.google.common.io.Files
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinPool
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

abstract class IncrementalTransform extends Transform {

    static final String TMP_DIR = "lztmp"

    Project project

    File buildDir

    Logger logger


    List<File> classPaths = new LinkedList<>()
    List<File> filesNeedInject = new LinkedList<>()

    Map<String, FileNeedInject> dirtyFile = new ConcurrentHashMap<>()

    Set<String> dirtyClassName = new HashSet<>()

    public void addDirtyClassBytes(String filePath, String jarEntryName, byte[] classBytes) {
        FileNeedInject fileNeedInject = dirtyFile.get(filePath)
        if (fileNeedInject == null) {
            synchronized (filePath.intern()) {
                fileNeedInject = dirtyFile.get(filePath)
                if (fileNeedInject == null) {
                    fileNeedInject = new FileNeedInject()
                    if (jarEntryName != null && !jarEntryName.isEmpty()) {
                        fileNeedInject.isJar = true
                        fileNeedInject.jarBytes = new ConcurrentHashMap<>()
                    } else {
                        fileNeedInject.isJar = false
                    }
                    dirtyFile.put(filePath, fileNeedInject)
                }
            }
        }
        if (fileNeedInject.isJar) {
            fileNeedInject.jarBytes.put(jarEntryName, classBytes)
        } else {
            fileNeedInject.classBytes = classBytes
        }
    }

    void addDirtyClassName(String className) {
        dirtyClassName << className
    }

    boolean isClassDirty(String className) {
        return dirtyClassName.contains(className)
    }

    IncrementalTransform(Project project) {
        this.project = project
        this.logger = project.logger
        this.buildDir = project.buildDir
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

        //add system boot classpath
        project.android.bootClasspath.each {
            classPaths << it
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
                                log("=========== class file removed: ${outFile.absolutePath}")
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
                log(jarInput.name + " status: " + jarInput.status)
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
                            log("===========jar input: ${jarInput.file.absolutePath}")
                            log("===========jar removed: ${outJarFile.absolutePath}")
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

    void onTransformStart(TransformInvocation invocation) {}

    void onEachDirectory(DirectoryInput dirInput) {
        classPaths << dirInput.file
    }

    void onRealTransformFile(DirectoryInput dirInput, File inputFile, File outputFile) {
        Files.createParentDirs(outputFile)
        FileUtils.copyFile(inputFile, outputFile)

        filesNeedInject << outputFile
    }

    void onRealTransformJar(JarInput jarInput, File outJarFile) {
        Files.createParentDirs(outJarFile)
        FileUtils.copyFile(jarInput.file, outJarFile)

        if (filter(jarInput)) {
            filesNeedInject << outJarFile
        }
    }

    void onEachJar(JarInput jarInput) {
        classPaths << jarInput.file
    }

    void onTransformEnd(TransformInvocation invocation) {

        if (enable()) {

            long start = System.currentTimeMillis()

            warn("start weave")

            loadClass(invocation)
            doInject(invocation)
            diffClass(invocation)
            flushClass(invocation)

            warn("end weave - cost: " + (System.currentTimeMillis() - start) + " ms")
            warn("deal " + dirtyClassName.size() + " classes")

            if (invocation.isIncremental() && dirtyClassName.size() > 0) {
                warn("======================== classes deal ============================")
                printDealClasses()
                warn("======================== classes deal ============================")
            }
        } else {
            warn("disabled, so only copy file")
        }
    }

    abstract void loadClass(TransformInvocation invocation)

    abstract void doInject(TransformInvocation invocation)

    abstract void diffClass(TransformInvocation invocation)

    void flushClass(TransformInvocation invocation) {
        if (!dirtyFile.isEmpty()) {
            new ForkJoinPool().submit {
                dirtyFile.entrySet().parallelStream().forEach {
                    File file = new File(it.key)
                    FileNeedInject fileNeedInject = it.value
                    if (fileNeedInject.isJar) {
                        if (fileNeedInject.jarBytes != null && !fileNeedInject.jarBytes.isEmpty()) {
                            flushForJar(file, fileNeedInject)
                        }
                    } else {
                        if (fileNeedInject.classBytes != null) {
                            flushForFile(file, fileNeedInject)
                        }
                    }
                }
            }.get()
        }
    }

    boolean enable() {
        return true
    }

    boolean filter(String classPath) {
        return true
    }

    boolean filter(JarInput jarInput) {
        return true
    }


    void flushForFile(File file, FileNeedInject fileNeedInject) {
        final int index = file.absolutePath.indexOf(getName()) + getName().length()
        Closure handleFileClosure = { File innerFile ->
            if (fileNeedInject.classBytes != null) {
                def outputFile = new File(buildDir, getTmpDir() + innerFile.absolutePath.substring(index))
                Files.createParentDirs(outputFile)
                FileInputStream inputStream = new FileInputStream(innerFile)
                FileOutputStream outputStream = new FileOutputStream(outputFile)
                byte[] modified = fileNeedInject.classBytes
                outputStream.write(modified)
                inputStream.close()
                outputStream.close()

                Files.copy(outputFile, innerFile)
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

    void flushForJar(File outJarFile, FileNeedInject fileNeedInject) {
        final int index = outJarFile.absolutePath.indexOf(getName()) + getName().length()
        final def tmpFile = new File(buildDir, getTmpDir() + outJarFile.absolutePath.substring(index))
        Files.createParentDirs(tmpFile)

        new ZipInputStream(new FileInputStream(outJarFile)).withCloseable { zis ->
            new ZipOutputStream(new FileOutputStream(tmpFile)).withCloseable { zos ->

                ZipEntry entry
                while ((entry = zis.getNextEntry()) != null) {
                    byte[] modified = fileNeedInject.jarBytes.get(entry.name)
                    if (modified != null) {
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

    void log(String msg) {
        logger.info("LazyInject<" + getName() + "> - " + msg)
    }

    void warn(String msg) {
        logger.warn("LazyInject<" + getName() + "> - " + msg)
    }

    void printDealClasses() {
        dirtyClassName.each {
            warn("class: " + it)
        }
    }

    String getTmpDir() {
        return TMP_DIR + File.separatorChar + getName() + File.separatorChar
    }

}
