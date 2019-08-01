## Installation
1. Add a repository to the `build.gradle` file:
```groovy
repositories {
        maven {
            url "https://raw.githubusercontent.com/Cirrent/ZipKey-Android-SDK/master/releases"
        }
    }
```
2. Add dependencies:
```groovy
dependencies {
    implementation 'com.cirrent:cirrentsdk:1.2.32'
    implementation 'com.cirrent:nis:1.1.2' // in case if you need NetworkIntelligenceService
}
```
