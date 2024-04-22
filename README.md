# Hotswap Gradle

This plugin will provide/locate [DCEVM](https://dcevm.github.io/) and [HotswapAgent](http://hotswapagent.org/).
#

Basic example:
```groovy
plugins {
    id 'java'
    id 'io.github.srdjan-v.hotswap-gradle' version '0.1.0'
}

//this toolchain will be used if it cant find an dcevm
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

hotswapProvider {
    //This will provide a Jetbrains 11 JDK since its an known Dcevm
    configureTask(tasks.named("someJavaExecTask")) {
        languageVersion = JavaLanguageVersion.of(11)
    }

    //This will provide a Jetbrains 11 JDK since its an known Dcevm
    //and if available an snapshot HotswapAgent version
    configureTask(tasks.named("someJavaExecTask")) {
        languageVersion = JavaLanguageVersion.of(17)
        useSnapshot = true
    }

}
```

## Disclamer

This plugin is built using Gradle 8.5, and it's using a lot of internal classes.
Don't expect it for it to be stable when changing versions.
But nearly all code that depends on internal classes has be abstracted,
so that the end user is able to change the implementation on the fly.
