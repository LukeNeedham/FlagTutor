# Rules
This is an ordered list of todo items. Each item should be done in isolation, and a separate PR opened for each item. Items must be done in order. When an item is done, the PR should include the original TODO text in the description, and the todo should be removed from this list. When asked to implement the top todo item, you always take just the top item. Always create a PR automatically after implementing a todo item — do not ask whether to open one.

# Todos
- add a debug page, accessible from the home page when in debug builds, which lists all countries with their map and their button to open more info
- add a "reverse" game mode where the user gets shown the name of a country, and the 4 option buttons show flags, and the users needs to click the correct flag
- refactor to make the app fully offline - bundle the country data file in the apk instead of making API calls, and bundle the flag images in the apk also instead of using URLs