# GalacDecks
## An Open-Source Digital Collectible Card Game

Battle for galactic supremacy by smashing your opponent's homeworld to smithereens. But keep yours intact-- that's where you keep your stuff.

This project's source code is being released as open-source so other game developers can see how I handle the issues of a real-time, multiplayer asynchronous game with highly flexible game logic. Please see the note below about what is and is not open-sourced.

The server is written in Java (8+). The project uses Maven to manage dependencies, so dependencies are pulled in automatically.

The client is a Unity project, allowing for cross-platform development. 

Client-server communication is done using Websockets. Unity currently does not support Websockets "out of the box" so the project includes a Websocket-Sharp assembly (https://github.com/sta/websocket-sharp).

##What parts of the project are Open-Source:##

* The server source code. There are additional dependencies, all of which to the best of my knowledge are also open-source.
* The source code for the Unity game client component (*.cs files).

##What parts aren't:##

* The game artwork, sound, and music. Some of these are included in the repo for convenience of testing, but may not be used without their owner's permission.
* Third-party assets. The only critical one is JSON.Net, which is available from the asset store. 

##Requirements (Playing)##

TBD. Probably some kind of computer.

##Requirements (Developing)##

To build the client, you'll need:

* Unity3D, free or professional
* JSON.Net for Unity, available in the Asset store
* Additional Unity Assets may be used for game components, if so, they'll be added here.

To build and run the server, you'll need:

* Java 1.8+ JDK
* Maven (https://maven.apache.org/)
* The server uses the Hibernate ORM for data storage (imported by Maven). It's configured to use HSQLDB for testing, but we use MySQL in production, so you'll probably want that too.


