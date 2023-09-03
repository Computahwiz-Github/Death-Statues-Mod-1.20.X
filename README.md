# Death-Statues-Mod-1.20.X
WIP Death Statues Mod

> [!WARNING]
> W.I.P.! As of this verion (1.0), "DeathStatuesToast.java" is superfluous and can be discarded. I have all the other basic functionality for the mod complete, but am tinkering with how to make a custom toast popup.

This is my first Minecraft mod and it uses fabric. The idea of the mod is just to spawn a statue of a player when they die and have it as a collectible.

At the moment, when the player dies, an armor stand spawns. The armor stand wears whatever the player had on (armor & mainhand/offhand items). It even grabs the player skin for a player head if there is no helmet!

There are three goals for this project:

- [ ] 1 - Make a "faux-player-model" instead of using an armor stand for the statue
  * 1.1 - Implement skin caching to avoid hitting api request limit
  * 1.2 -  Make the player model drop as an item with full NBT data instead of just destroying an invulnerable armor stand (items otherwise drop, A.K.A. duplicate)

- [x] 2 - Make custom toast popups to indicate when/where the player's death statue spawned and when the player destroys it
  * 2.1 - Make custom background/icon/sounds for toasts

- [ ] 3 - Make statue act as grave and contain all player's dropped items on death. 
  - 3.1 When the player destroys the statue, make it drop all the items except what you choose to leave on (specific armor/weapons)

- [ ] 4 - Make translations in the language file actually work. Will take any pointers or tips please!

P.S. I added a shaped saddle recipe for fun (leather & iron)
