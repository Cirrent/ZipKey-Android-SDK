# CIRRENT™ Mobile App SDK

CIRRENT™ App SDK contains CIRRENT™ Wi-Fi Onboarding and [CIRRENT™ Mobile App Intelligence solutions](https://www.infineon.com/cms/en/design-support/service/cloud/cirrent-product-analytics/cirrent-mobile-app-intelligence-mai/):
- CIRRENT™ Wi-Fi Onboarding helps to onboard a device onto the user's private Wi-Fi network via Soft AP or BLE;
- CIRRENT™ Mobile App Intelligence (MAI) allows you to identify Wi-Fi onboarding problems by looking at broad trends from your entire fleet and correlating performance metrics and onboarding issues across multiple variables (router, ISP, fw/hw version, or your own custom parameters).

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
  implementation 'com.cirrent:cirrentsdk:1.6.10'
}
```

## Using CIRRENT™ Mobile App Intelligence (MAI)

First, you need to *initialize* the MAI using the pre-generated *Analytics token* and `init(Context appContext, String token, MaiCallback callback)` method.

### Token generation

- Go to [CIRRENT™ Console](https://console.cirrent.com/api-keys) and create *the App API Key and Secret* clicking **"Create API Key"**. Use the **"app"** Key Type. ***Please note:*** your API key consists of two parts: *{accountID}_{key}*.
- Generate the token using the MAI:
```java
String token = MobileAppIntelligence.createToken(900, "{accountId}", "{appId}", "{apiKey}", "{apiSecret}");

```

### Initialization

Initialization starts the SDK. Captures and sends the phone information (phone model, OS) and the Wi-Fi information used to identify a router, ISP, SSID for the network, etc.  
```java
MobileAppIntelligence.init(applicationContext, "{analyticsToken}", new MaiCallback() {
      @Override
      public void onTokenInvalid(MobileAppIntelligence.Retrier retrier) {
        retrier.retry("{re-generated valid token}");
      }

      @Override
      public void onFailed(MAIError e) {
        switch (e.getType()) {
          case REQUEST_FAILED:
          case INIT_REQUIRED:
          case INIT_DATA_COLLECTING_IS_ACTIVE:
          case LACK_OF_LOCATION_PERMISSION:
          case RESERVED_STEP_NAME_USED:
          case START_ONBOARDING_REQUIRED:
          case ONBOARDING_TYPE_REQUIRED:
          case END_ONBOARDING_REQUIRED:
        }
      }
    });

```
***Please note:*** the MAI must be initialized before calling any other methods. Otherwise, `MaiCallback.onFailed(MAIError)` will be called.

### Three simple steps to get more info about users onboarding experience

When the MAI is initialized, you can start capturing data related to particular onboarding, following these three simple steps: 
 
*These three steps allow the app developers to understand how many onboarding attempts succeeded and what is the duration that a user spent to onboard a device to Wi-Fi. This allows developers to also see what was the last step that the user was on before abandoning in case of unsuccessful onboarding attempts.*

#### 1. Start Onboarding

```java
MobileAppIntelligence.startOnboarding(OnboardingType.SOFTAP);
```
This method tells the CIRRENT™ Cloud that onboarding has been started. Also creates a unique *OnboardingID* and stores it for subsequent calls (until *endOnboarding()* method is called).

#### 2. Identify key points of your onboarding process

And enrich them by adding:

```java
MobileAppIntelligence.enterStep(
        StepData.create(
            StepResult.SUCCESS,
            "{this_step_name}",
            "{reason_why_it_happened}"
        ).setDebugInfo(
            new HashMap<String, String>() { //optional debug info
              {
                put("key1", "value1");
                put("key2", "value2");
              }
            }
        )
    );
```
 
It will tell the CIRRENT™ Cloud what is happening during your onboarding process. CIRRENT™ MAI captures all steps and their duration. All this information allows developers to understand the root cause of the issue.

#### 3. End Onboarding

```java
MobileAppIntelligence.endOnboarding();
    //OR
    //if onboarding wasn't successful.
MobileAppIntelligence.endOnboarding(
        EndData.createFailure("{failure_reason}").setDebugInfo(
            new HashMap<String, String>() {
              {
                put("key1", "error1");
                put("key2", "error2");
              }
            }
        )
    );
```

It tells the CIRRENT™ Cloud that onboarding has been ended. Close out the onboarding id.

## CHANGELOG
### 1.6.10
- CIRRENT™ Wi-Fi Onboarding API was simplified; 
- bug fixes and improvements;
### 1.6.5
- bug fixes and improvements;
### 1.6.3
- bug fixes and improvements;
### 1.6.1
- bug fixes and improvements;
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
- API has been simplified: now `MobileAppIntelligence.init(Context appContext, String token, MAICallback callback)` the only method that requires `Context` and `MAICallback`;
- BLE & Soft AP onboarding protocols have been changed;
- ZipKey onboarding type has been completely removed;
- improved stability.
### 1.3.13
- bug fixes and improvements;
### 1.3.12
- bug fixes and improvements;
### 1.3.11
- bug fixes and improvements;
### 1.3.10
- bug fixes and improvements;
### 1.3.9
- bug fixes and improvements;
- added an ability to restart "init" data collecting if location permission is granted.
### 1.3.8
- bug fixes and improvements;
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
- bug fixes and improvements;
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
