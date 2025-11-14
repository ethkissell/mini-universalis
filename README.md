# Mini Universalis

Team Members:

    Names: Ethan Kissell, Achintya Kumar

## Description

Our project will be a self playing game similar to Polymorphia, but instead will focus on a 2D grid that has Nations placed upon it. These Nations will start as seeds randomly placed on the grid and will expand in the four cardinal directions into empty Provinces every turn. These Provinces will have a development level starting between 1-3, and capped at 9 that influences an army capacity, and how fast these armies grow every turn. Each Nation will have a strategy dependent on if it has any empty provinces to expand into, and the current size of their army. These strategies are an Offensive, attacking neighboring Nations to accrue more Provinces, and a Defensive, holding an army size to defend against invading Nations. The Nations also get a certain amount of development points dependent on size and total development to distribute randomly among their Provinces.

## Patterns Used

- Builder
  - To create the map
- Strategy
  - To change the actions of the Nations depending on army and neighbors
- Factory
  - To create provinces
  - To create a nation with a name pool and starting strategy
- Singleton
  - For an event bus for the Visual Observer
- Observer
  - Visual Observer for updating a UI

## Running the Game

Currently ran using playToCompletion_printsFullGameAndFinishes() in UnversalisTest
