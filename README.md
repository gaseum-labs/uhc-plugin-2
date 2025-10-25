# UHC Plugin 2

[Legacy Plugin](https://github.com/gaseum-labs/uhc-plugin)

## How to Use

1. Download the latest [Paper 1.21.8 Jar](https://papermc.io/downloads/paper)
2. Place it in a server directory and rename it to `server.jar`
3. Create a file named `run.bat` in the same directory with the contents:
   ```[YOUR PATH TO JAVA 22 EXE] -Xmx8g -jar server.jar nogui```
4. Execute `run.bat` and wait for server to fail
5. Open the generated file named `eula.txt` and edit it such that `eula=true`
6. Download [ProtocolLib jar v5.4.0](https://github.com/dmulloy2/ProtocolLib/releases/tag/5.4.0)
7. Download Latest release of [UHC Plugin 2](https://github.com/gaseum-labs/uhc-plugin-2/releases/tag/v0.0.1)
8. Place the 2 plugin jars in the directory named `plugins` in your server directory
9. Execute `run.bat` again to start the server

## Discord Bot Config

The discord bot is configured from the file `[server directory]/plugins/discord-config.json`. 
