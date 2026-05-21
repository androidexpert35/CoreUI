# CoreUI Sample - Agent Reference

This module is a runnable demonstration app for the `coreui` library. It should model the same
clean-architecture boundaries used by the library while staying intentionally small and readable.

## Module intent

- Show how to use `BaseViewModel`, `UIState`, `AppBaseScreen`, and the typed navigation helpers in
  a real app flow.
- Keep one screen close to the library defaults and another screen focused on customization.
- Prefer copy-pasteable example code over demo-only abstractions.

## Package map

- `com.tony.coreui.sample.app`
  Application bootstrap, activity, app container, and view-model factories.
- `com.tony.coreui.sample.domain`
  Framework-agnostic models, enums, and repository contracts.
- `com.tony.coreui.sample.data`
  Fake repository implementations and in-memory demo data.
- `com.tony.coreui.sample.presentation`
  Compose UI, view models, route definitions, and theme code.

## Rules

- Follow the same event-driven pattern as `coreui`: screens talk to view models through typed
  events, not direct state mutation.
- Keep navigation in view models. Composables should not build raw route strings themselves.
- Define routes with `RouteDefinition` and typed arguments. Do not concatenate route strings.
- Keep domain models free of Android and Compose dependencies.
- Use `launchUiStateUpdate` for request flows unless a custom path is genuinely clearer.
- Prefer constructor injection through the app container. Do not add Hilt just for the sample.
- When a change only affects the sample app, stay inside `sample/` unless shared infrastructure
  truly needs to move.
