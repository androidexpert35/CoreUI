# CoreUI

CoreUI is an Android Jetpack Compose library that provides a robust foundation for building modern Android applications. It includes typed state management, a base ViewModel with built-in coroutine safety, and a command-based navigation system.

## Project Structure

- **`coreui/`**: The main library module (Android Library).
- **`sample/`**: A sample application demonstrating how to use the library.
- **`doc/`**: Detailed documentation and agent references.

## Installation

### GitHub Packages

To use CoreUI in your project, add the following to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/YOUR_GITHUB_USER/CoreUI")
            credentials {
                username = "YOUR_GITHUB_USER"
                password = "YOUR_GITHUB_TOKEN"
            }
        }
    }
}
```

Then add the dependency in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.tony:coreui:1.0.0")
}
```

## Features

- **Typed State Management**: `UIState`, `UIStatus`, and `UIError`.
- **Base ViewModel**: Simplified handling of async data fetching with `launchUiStateUpdate`.
- **Command-based Navigation**: Decouple your ViewModels from the `NavController`.
- **Composable Scaffolds**: `AppBaseScreen` handles loading and error states automatically.

## Documentation

For a deep dive into the architecture and API, see [AGENTS.md](doc/AGENTS.md).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
