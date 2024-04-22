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
Custom properties are available with their types
```properties
#This will enable more logging, default false
io.github.srdjan-v.hotswap-gradle.debug=Boolean

#Agent GitURl
#Default https://api.github.com/repos/HotswapProjects/HotswapAgent/releases
io.github.srdjan-v.hotswap-gradle.agent-api-url=String

#This will set the agent into offline mode explicitly
io.github.srdjan-v.hotswap-gradle.offline-mode=Boolean

#Data dir, default GradleUserHomeDir
io.github.srdjan-v.hotswap-gradle.data-directory=String

#If the agent should persist cached dcevm's, default true 
io.github.srdjan-v.hotswap-gradle.cached-registry-persistence=Boolean
```

## Disclamer

This plugin is built using Gradle 8.5, and it's using a lot of internal classes.
Don't expect it for it to be stable when changing versions.
But nearly all code that depends on internal classes has be abstracted,
so that the end user is able to change the implementation on the fly.
