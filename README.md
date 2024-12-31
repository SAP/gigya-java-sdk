[![Release](https://jitpack.io/v/SAP/gigya-java-sdk.svg)](https://jitpack.io/v/SAP/gigya-java-sdk)
# Java SDK 
[Learn more](https://github.com/SAP/gigya-java-sdk/wiki)

## Description
The Java SDK, provides a Java interface for the Gigya API. 
The library makes it simple to integrate Gigya services in your Java application.

## Requirements
[The Java SDK requires JDK1.5 and above](https://www.java.com/en/download/) 

Please note that the GSJavaSDK.jar file is compiled using JDK 1.8 with compatibility for 1.6, 
but you may use the SDK's source files and compile them in a JDK 1.5 environment.

## Download and Installation
### Using Jitpack
* Add the Jitpack reference to your root build.gradle file
```
allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io' }
    }
}
```
* Add the latest build reference to your app build.gradle file
```
dependencies {
  implementation 'com.github.SAP:gigya-java-sdk:sdk:3.5.1'
  implementation 'com.github.SAP:gigya-java-sdk:auth:3.5.1' // only if needed
}
```
### Using Binaries
* Go to releases
* Download the required version and place the .jar file in your */libs* folder
* Add to your gradle.build file
```
implementation files('libs/gigya-java-sdk-3.5.1.jar')
implementation files('libs/gigya-java-sdk-auth-1.0.2.jar') // only if needed
```
  
### Using the source code
* Clone the repo.
* Open the Project.
* Build via Gradle.

## Configuration
* [Obtain a Gigya APIKey and Secret key](https://github.com/SAP/gigya-java-sdk/wiki#obtaining-sap-customer-data-clouds-api-key-and-secret-key).
* Start using according to [documentation](https://github.com/SAP/gigya-java-sdk/wiki#logging-in-the-user).

## Limitations
None

## Known Issues
None

## How to obtain support
Open an issue in this repository.

## Contributing
Via pull request to this repository.

## Code of Conduct
See [CODE_OF_CONDUCT](https://github.com/SAP/gigya-java-sdk/blob/main/CODE_OF_CONDUCT.md)

## To-Do (upcoming changes)
None

## Licensing
Please see our [LICENSE](https://github.com/SAP/gigya-java-sdk/blob/main/LICENSE.txt) for copyright and license information.

[![REUSE status](https://api.reuse.software/badge/github.com/SAP/gigya-java-sdk)](https://api.reuse.software/info/github.com/SAP/gigya-java-sdk)
