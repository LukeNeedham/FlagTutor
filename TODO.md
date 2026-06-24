# Rules
This is an ordered list of todo items. Each item should be done in isolation, and a separate PR opened for each item. Items must be done in order. When an item is done, the PR should include the original TODO text in the description, and the todo should be removed from this list. When asked to implement the top todo item, you always take just the top item. Always create a PR automatically after implementing a todo item — do not ask whether to open one.

# Todos
- add a "reverse" game mode where the user gets shown the name of a country, and the 4 option buttons show flags, and the users needs to click the correct flag
- refactor to make the app fully offline - bundle the country data file in the apk instead of making API calls, and bundle the flag images in the apk also instead of using URLs. then we can also precompute the map image for each country using a gradle task that builds pngs based on the bundled country data, so the map data doesnt need to be in app code, and we dont have to draw to a canvas at runtime
- remove about page
- open the country selected in google maps when the user clicks the map image
- improve country button crumble animation when wrong - have it fall like glass shards rather than only rows
- for the first flag in the game, the buttons change colour once the flag colour extraction is done. avoid this by having no default colour for the buttons and only showing them once the colours are ready
- add missing map data for vatican city.
- for tiny countries, use a zoom that is far enough out for the user to be able to place it on the world map, and draw a white circle around the location of the country