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
    implementation 'com.cirrent:cirrentsdk:1.2.37'
}
```
## CHANGELOG
### 1.2.37
#### Added
- account id checking logic;
#### Changed
- context type for Bluetooth on-boarding(Activity -> Application);
### 1.2.35
#### Added
- optional `GatherEnvironmentCallback` with a `onEnvironmentGathered(boolean isEnvironmentCompletelyCollected)` method to the `CirrentService.gatherEnvironment()`.
### 1.2.33
#### Added
- `boolean isProviderKnownNetwork` and `String jwt` to the `DeviceInfoCallback.onDevicesFound(List<Device> nearbyDevices)` method.
### 1.2.32
#### Added
- initial release
