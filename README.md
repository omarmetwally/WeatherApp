# Weather - Your Forecast Guide -
## Play Store Link:
Soon (In review)
## API Documentation
https://openweathermap.org/forecast5
## Features

### Weather Updates:
- **Current Weather:** Displays current weather details including temperature, weather condition, humidity, wind speed, and more for your location.
- **Forecast:** Access a 5-day/3-hour weather forecast to help plan your week ahead.

### Alerts and Notifications:
- **Weather Alerts:** Set up alerts for specific weather conditions like rain, thunderstorms, or significant temperature changes.
- **Notification Service:** Receive notifications about weather conditions at scheduled times for your saved locations.

### Location Management:
- **Dynamic Location Updates:** Automatically updates weather details based on your current location.
- **Multiple Locations:** Allows saving and managing multiple locations for quick weather checks.
- **Map Integration:** Select locations for weather updates directly from an integrated map feature.

### Customization and Preferences:
- **Units of Measurement:** Choose between Celsius, Fahrenheit, and Kelvin for temperature; and metric or imperial units for wind speed.
- **Language Support:** The app supports multiple languages for weather updates and alerts. (English and Arabic for Now)

## Technical Details

### Architecture
- **MVVM (Model-View-ViewModel):** MVVM architectural pattern to ensure a clean separation of concerns, making the codebase more maintainable and testable.

### Modern Android Development
- **Kotlin Coroutines and Flow:** For asynchronous programming and state management.
- **StateFlow:** Used for lifecycle-aware data observation.
- **Room Database:** For local persistence, enabling offline access and quick data retrieval.
- **WorkManager and AlarManager:** Manages background tasks such as fetching weather updates and setting alerts.
- **ViewModel:** Manages UI-related data in a lifecycle-conscious way.

### Networking
- **Retrofit:** For network calls.

### Location Services
- **Google Location Services:** Acquires the user's location to provide relevant weather information.
- **Geocoder:** Acquires the user's location Address.

### Notifications
- **Android Notification Channels:** Delivers timely and context-aware weather alerts to the user.

### Additional Libraries and Tools
- **Glide:** For efficient image loading, used to display weather condition icons.
- **Lottie:** For high-quality animations, especially in the splash screen.

## App Demo
Soon (Images and Video)





