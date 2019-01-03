package com.trend.lazyinject.aopweave

import com.android.build.gradle.AppExtension
import com.trend.lazyinject.aopweave.transforms.LazyInjectTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

public class LazyInjectPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        if (!isAndroidProject(project)) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }

        project.android.registerTransform(new LazyInjectTransform(project))

        project.dependencies {
            String version = project.rootProject.publishVersion
            if (project.gradle.gradleVersion > "4.0") {
                implementation "com.trend.lazyinject:lib:${version}"
            } else {
                compile "com.trend.lazyinject:lib:${version}"
            }
        }

    }

    static boolean isAndroidProject(Project project) {
        def android = project.extensions.getByName("android")
        return android != null && android instanceof AppExtension
    }


}