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
    implementation 'com.cirrent:cirrentsdk:1.6.5'
}
```
## CHANGELOG
### 1.6.5
#### Fixed
- a bug where the sdk couldn't get device info during Soft AP onboarding;
### 1.6.3
#### Fixed
- a bug where the sdk may cause a crash on OnePlus 7T phone;
- a bug where the sdk may cause a crash due to non-standard/incorrect Bonjour TXT Record.
### 1.6.1
#### Fixed
- a bug where `MobileAppIntelligence.removeAllCollectedData()` may cause a crash.
### 1.6.0
#### Changed
- now to report step details you need to call `enterStep(StepData)` method (you can create `StepData` object using `StepData.create()` method);
- now to report end onboarding details you need to call `endOnboarding(EndData)` method (you can create `EndData` object using `EndData.create()` method);
- steps "outside" onboarding were allowed (now you can use `enterStep(StepData)` right after MAI was initialized).
#### Added
- added an ability to report an onboarding type along with the `startOnboarding(OnboardingType)` method;
- added an ability to add debug info to `StepData` and `EndData` objects using `StepData.addDebugInfo(Map<String, String>)` or `EndData.addDebugInfo(Map<String, String>)` methods.
### 1.5.2
#### Changed
- API has been simplified: now `MobileAppIntelligence.init(Context appContext, String token, MAICallback callback)` the only method that requires  `Context` and  `MAICallback`;
- BLE & Soft AP onboarding protocols have been changed;
- ZipKey onboarding type has been completely removed;
- improved stability.
### 1.3.11
#### Fixed
- fixed a bug where an incorrect SRV description URL of UPnP service may cause IllegalArgumentException.
### 1.3.10
#### Fixed
- fixed a bug where an incorrect symbol in Bonjour TXTRecord may cause ArrayIndexOutOfBounds exception.
### 1.3.9
#### Fixed
- fixed a bug where the candidate networks list may contain SoftAP network.
#### Added
- an ability to restart "init" data collecting if location permission is granted.
### 1.3.8
#### Fixed
- a bug where MAI crashes the app with StringIndexOutOfBoundsException due to non-standart LOCATION field value in M-SEARCH response.
### 1.3.7
#### Changed
- improved stability and performance.
### 1.3.6
#### Added
- `MobileAppIntelligence.setDebugMode(boolean enabled)` . This method gives an ability to see more log entries.
### 1.3.5
#### Changed
- improved stability;
### 1.3.4
#### Fixed
- a bug where MAI can't generate a correct token;
### 1.3.3
#### Added
- an ability to set onboarding session timeout (default value = 15 mins);
#### Changed
- Mobile Onboarding Analytics renamed to Mobile App Intelligence;
### 1.3.2
#### Added
- OnboardingAnalytics.createToken();
### 1.3.1
#### Added
- Mobile Onboarding Analytics;
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
