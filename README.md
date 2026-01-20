# AvaAwa.eu native Android app

A tool to assess avalanche terrain, based on the slope parameters and regional avalanche forecast. 
Key advantage to the tools existing on the market is the highlight of the avalanche danger not only 
based on the region, but also considering the aspect and elevation relevant to the avalanche problems, 
specified in the bulletin. The information on the specific problems is available at a glance, looking 
at a particular route you are planning.

## Features
- **Avalanche Bulletin Mode**: Fetches and displays real-time avalanche danger levels from official bulletins
- **Risk Mode**: Highlights risky terrain, as per the regional avalanche bulletin, based on the following parameters of the relevant problems(see the matrix in the popup):
    - Danger level
    - Elevation range
    - Aspect
    - Steepness

- **Custom Mode**: Interactive tools to filter terrain based on:
    - Elevation range (min/max)
    - Slope angle(>30°, >35°, >40°)
    - Aspect

**Disclaimer**: This tool is for informational purposes only. It does not replace official avalanche bulletins or professional danger assessments. You are solely responsible for your safety; entering the backcountry involves significant risk.

## Tech Stack
- **Language**: [Kotlin](https://kotlinlang.org/)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Asynchronous Work**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [Gson](https://github.com/google/gson)
- **Maps**: [Mapbox Maps SDK for Android](https://www.mapbox.com/android-docs/maps/overview/)
- **UI Components**: Android Jetpack (ViewModel, LiveData), Material Design

### Prerequisites
- Android Studio Giraffe or newer.
- Mapbox Access Token - get it on the website.

