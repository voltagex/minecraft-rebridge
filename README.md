#Rebridge for Minecraft
######An HTTP server glued to a Forge Mod
##How to build

* Clone this project
* In a shell/cmd/Powershell, run `gradlew.bat setupDecompWorkspace --refresh-dependencies`
* Open IntelliJ IDEA and import from existing sources
* Select Gradle and import

##Notes
* NanoHTTPD included as source until https://github.com/NanoHttpd/nanohttpd/issues/169 is resolved
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

* Implement SMPPlayerProvider or similar

## API
* Figure out if Loader.instance().getActiveModList().get(4).getMod().getClass().getMethods()[0].invoke(Loader.instance().getActiveModList().get(4).getMod().getClass().newInstance()) really is an abomination