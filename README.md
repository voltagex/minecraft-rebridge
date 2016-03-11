#Rebridge for Minecraft
######An HTTP server glued to a Forge Mod
##How to build

* Clone this project
* In a shell/cmd/Powershell, run `gradlew.bat setupDecompWorkspace --refresh-dependencies`
* Open IntelliJ IDEA and import from existing sources
* Select Gradle and import

##Notes
* Coming back to this a long time later:
  * http://www.minecraftforge.net/forum/index.php?topic=14048.0#post_update_forgegradle
  * http://www.minecraftforge.net/forum/index.php/topic,34500.msg181756.html#msg181756
* NanoHTTPD included as source until https://github.com/NanoHttpd/nanohttpd/issues/99 is resolved
* Heavily inspired by an old mod called Modbridge, unfortunately the source to that was lost so here we are

#To do
## Routing
* probably similar to Microsoft's WebAPI routing

```
GET /api/status
GET /api/player/{player}/{attribute}
GET /api/world/{world}/{attribute}
POST /api/player/{player}/{attribute}
```

## Support more than one player on the server
* Change routing to be more like

```
GET /api/player/{playername}/{attribute}
```

* Should routing be provided/hinted by the I\*Provider?

* Implement SMPPlayerProvider or similar

## API
* Test it.
