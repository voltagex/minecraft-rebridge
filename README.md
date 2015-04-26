#Rebridge for Minecraft
######An HTTP server glued to a Forge Mod
##How to build

* Clone this project
* Pull down Forge from http://files.minecraftforge.net/maven/net/minecraftforge/forge/1.8-11.14.1.1392/forge-1.8-11.14.1.1392-src.zip
* Extract Forge to the project directory
* In a shell/cmd/Powershell, run `gradlew.bat setupDecompWorkspace --refresh-dependencies`
* Open IntelliJ IDEA and import from existing sources
* Select Gradle and import
* Exit IntelliJ
* Run `gradlew.bat gIR`

##Notes
* NanoHTTPD included as source until https://github.com/NanoHttpd/nanohttpd/issues/169 is resolved
* Heavily inspired by an old mod called Modbridge, unfortunately the source to that was lost so here we are
