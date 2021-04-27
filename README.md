# The Shaft

An original game for [Ludum Dare 48](https://ldjam.com/events/ludum-dare/48)

## [Play!](https://kag0.github.io/ld48/dist/)

The objective of the game is to avoid hitting the ceiling (which is coated in invisible death spikes).  
Move the players with A and D, and J and L.  
But be careful, the other players can punch and kick with E and Q, and O and U.  
The last player shown on the screen is the winner.  
Press space to play again.

### Other

You can pause the movement of the platforms with `0`.  
On linux you can see the players hitboxes.

## Setup

```
npm install -g http-server
```

## Running

from the repo root

```
./mill game.buildGame
cd out/game/indigoBuild/dest
http-server -c-1
```

open your browser to the url shown in the terminal
