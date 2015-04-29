#Rebridge for Minecraft
######An HTTP server glued to a Forge Mod
##How to build

* Clone this project
* In a shell/cmd/Powershell, run `gradlew.bat setupDecompWorkspace --refresh-dependencies`
* Open IntelliJ IDEA and import from existing sources
* Select Gradle and import
* Exit IntelliJ
* Run `gradlew.bat gIR`

##Notes
* NanoHTTPD included as source until https://github.com/NanoHttpd/nanohttpd/issues/169 is resolved
* Heavily inspired by an old mod called Modbridge, unfortunately the source to that was lost so here we are

#To do
## Investigate Java 8
* Apparently Forge can be patched to work with Java 8
* This would allow nice things like https://vert-x3.github.io/

## Investigate Vert.X
* NanoHTTPD is pretty limited, is it worth moving to a full framework?

## Routing
* probably similar to Microsoft's WebAPI routing

```
GET /api/status
GET /api/player/{player}/{attribute}
GET /api/world/{world}/{attribute}
POST /api/player/{player}/{attribute}
```

## Abstract the server away from Minecraft
* Work out how to run *just the server* in IntelliJ
* Move all the minecraft-related stuff to something like an IMinecraftProvider
* Implement a mock IMinecraftProvider


## Support more than one player on the server
* Requires lots of changes
