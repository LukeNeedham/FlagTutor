# Rules
This is an ordered list of todo items. Each item should be done in isolation, and a separate PR opened for each item. Items must be done in order. When an item is done, the PR should include the original TODO text in the description, and the todo should be removed from this list. When asked to implement the top todo item, you always take just the top item. Always create a PR automatically after implementing a todo item — do not ask whether to open one.

# Todos
- remove card background from game page behind flag image
- add rounded corners to flag image (5dp)
- when user gets answer right, dont change the button of the correct answer (so dont show the tick icon)
- on the game page, the colours of the country option buttons should be the colours extracted from the flag image
- on the game page, change the country option buttons to be a 2x2 grid that fills width and takes up half the screen height, rounded corners
- add a "reverse" game mode where the user gets shown the name of a country, and the 4 option buttons show flags, and the users needs to click the correct flag
- refactor to make the app fully offline - bundle the country data file in the apk instead of making API calls, and bundle the flag images in the apk also instead of using URLs