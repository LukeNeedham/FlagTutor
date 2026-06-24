# Rules
This is an ordered list of todo items. Each item should be done in isolation, and a separate PR opened for each item. Items must be done in order. When an item is done, the PR should include the original TODO text in the description, and the todo should be removed from this list. When asked to implement the top todo item, you always take just the top item. Always create a PR automatically after implementing a todo item — do not ask whether to open one.

# Todos
- refactor to make the app fully offline - bundle the country data file in the apk instead of making API calls, and bundle the flag images in the apk also instead of using URLs. then we can also precompute the map image for each country using a gradle task that builds pngs based on the bundled country data, so the map data doesnt need to be in app code, and we dont have to draw to a canvas at runtime
- open the country selected in google maps when the user clicks the map image