# Quick Http Client
Quick HTTP Client is a Kotlin-based library that simplifies the process of making HTTP requests and handling responses. It is designed to offer a straightforward API for developers to perform network operations in their Android applications.

## Features
- Simple and concise API - Making HTTP requests is as simple as a few lines of code.
- HTTP Method Enum - Utilize the built-in RequestMethod enum for specifying HTTP methods with ease.
- SSL Pinning - Enhance security with integrated SSL Pinning, supporting both public key and certificate pinning.
- Retry Mechanism - Robust retry mechanism to handle intermittent network issues.
- Caching Strategy - Efficient response caching to reduce network load and speed up response time.
- Logging - Detailed logging to help with debugging and tracking network requests.

## Installation
To use the QuickHttpClient library in your project, add the following to your `settings.gradle` file
```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
        // Other repositories
    }
}
```
Then, add the dependency to your module-level build.gradle file:
```groovy
implementation "com.github.thaer-al-khishen:quick-http-client:1.0.0-beta01"
```

## Usage
```kotlin
NetworkManager.makeNetworkCall<List<JsonPlaceHolderPost>>(
  NetworkRequest(
    endpoint = "https://jsonplaceholder.typicode.com/posts",
    requestMethod = RequestMethod.GET,
  )
)
```

## License
This project is licensed under the Apache 2.0 License. Check the LICENSE file for details.
