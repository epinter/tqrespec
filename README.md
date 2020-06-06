# TQ Respec

[![Steam](https://img.shields.io/badge/steam-guide-red.svg)](https://steamcommunity.com/sharedfiles/filedetails/?id=1262483108)
[![Latest Release](https://img.shields.io/github/release/epinter/tqrespec.svg)](https://github.com/epinter/tqrespec/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/epinter/tqrespec/total.svg)](https://github.com/epinter/tqrespec/releases/latest)
[![Release Date](https://img.shields.io/github/release-date/epinter/tqrespec.svg)](https://github.com/epinter/tqrespec/releases/latest)
[![Steam views](https://img.shields.io/steam/views/1262483108.svg)](https://steamcommunity.com/sharedfiles/filedetails/?id=1262483108)
[![License](https://img.shields.io/github/license/epinter/tqrespec.svg)](https://github.com/epinter/tqrespec/blob/master/LICENSE)

## Introduction

TQRespec is a tool for [Titan Quest](https://titanquestgame.com) game that helps you to change your character at any time. While this tool permits you make changes not available in game, cheating features are not available.

#### ***Download***

You can download TQRespec from the releases page, [here](https://github.com/epinter/tqrespec/releases/latest). There's no installation, just extract and run.

#### ***Requirements***

This software requires **Microsoft Windows 64-bit** (tested with Windows 7, 8, 8.1 and 10), **Windows 32-bit is not supported**. You don't need to have java or any other software installed, but if you have problems like missing dlls, check if you have Microsoft Visual C++ Redist 2015 installed.

## **How to use**

#### ***Before you start***

Keep in mind that this software make modifications to your save game (more specifically the file Player.chr). You shouldn't modify your characters while the game is running, you can corrupt your save game. ***YOU SHOULD ALWAYS KEEP A FULL BACKUP OF YOUR SAVE GAME FOLDER*** (Documents/My Games/Titan Quest - Immortal Throne).

![TQRespec Screenshot 1](https://raw.githubusercontent.com/epinter/tqrespec/master/assets/screenshot_attributes.png "Attributes")
![TQRespec Screenshot 2](https://raw.githubusercontent.com/epinter/tqrespec/master/assets/screenshot_skills.png "Skills")

#### ***Running without the game installed***

This application uses game data. If you want to run it on a PC that doesn't have the game installed, make sure you have a 'gamedata' directory with Database and Text resources inside it. The structure should look like this:

~~~
TQRespec/app
TQRespec/runtime
TQRespec/gamedata/Database
TQRespec/gamedata/Text
TQRespec/TQRespec.exe
~~~

Where Database and Text folders (with its contents) are copied from the game.
The savegame needs to be at the same location as the game uses (Documents/My Games/...).

#### ***Use it!***

Select the character you want to change. Now you can see some information like Class and Difficulty, and start to change your savegame.

#### ***Attributes***

There are five attributes available to change (Health, Energy, Strength, Intelligence, Dexterity). When you decrease an attribute, you will see the number in "Available Points" increasing, and when you increase an attribute the points are automatically got from "Available Points". The number that increases in each of the attribute fields follows in game rules. In fact, you can't give more points to your character, just redistribute. So no cheat.

#### ***Skills***

Now if you change to Skills tab, you will see two lists with the skills of your character. Below this lists, there are two buttons for each. The first button "Reclaim All Skills Points" will remove all points allocated on skills on that mastery and make the points available to use in game. The button "Mastery" permits you to reduce the mastery level to 1 or remove the mastery completely.

If you have a mastery on level 24 and 7 skills with points allocated, you can click on "Reclaim All Skills Points" to have the points from the 7 skills back to you. Then you click on "Mastery / Reclaim" to have 23 points back from the mastery, so you will have the mastery with just 1 point allocated, or you can click on "Mastery / Remove" to have all 24 points from mastery.

#### ***Copy your character***

If you want to change the name of you character, you can type the new name in the "New save" text field then click on "Copy To" button. A new character will be created, with progress, inventory with all items, attributes, skills, etc... The only thing you will not have on the copy is the Storage.

#### ***Saving and backup***

After you finish, you can click on Save. At this moment, TQRespec will make a backup of the file "Player.chr" inside a .zip in the folder "Titan Quest - Immortal Throne/SaveData/TQRespec Backup". The file "Player.chr" is where the games saves everything about the character, except the progress, transfer area and storage.

## ***Project and source code***

TQRespec is developed in Java language, using OpenJFX to provide graphical user interface, and a few more other open source dependencies. The current version is built using OpenJDK 14.
The software was made to work in Microsoft Windows operating system, since the game runs only in Windows. But because Java is multi-platform, this project is prepared to allow you to develop and test under Linux. To do so, you will need to have this structure inside the project (./gamedata):
~~~
gamedata/SaveData/Main/_savegame/Player.chr
gamedata/Database/database.arz
gamedata/Text/Text_EN.arc
~~~


#### ***Building***

Before building it, you need to have OpenJFX(JavaFX) SDK inside the ./sdk subdirectory. A repository with the needed files can be found [here](https://github.com/epinter/openjfx-sdk). Just clone it inside sdk directory.

With JDK 14 installed, you can build executing the 'clean' and 'build' gradle tasks with the command:

~~~
gradlew clean
gradlew build
~~~

#### ***Running the development version***

With this repository and openjfx-sdk properly in place, you can run with:

~~~
gradlew run
~~~



## **Troubleshooting**

#### ***Startup***
This software can fail to start if the game is not detected. Game is detected searching for Uninstall information from Windows, and data from your Steam installation. If you can't start, get the error message inside the "Show details" and open an issue. Or maybe you can find some help in the discussion at Steam Guides.
If your game path is not detected, you still can run TQRespec copying the game data, to do so follow the instructions [Running without the game installed](https://github.com/epinter/tqrespec#running-without-the-game-installed).


#### ***Errors and reporting***
If you see an error popup while using TQRespec, click on "Show details" and copy the complete error message (called java Exception). With this exception, a developer can find exactly where the code failed. If the software is crashing and you don't get an error message, or is crashing during startup, go to Windows Explorer and find the log file called **tqrespec.log** inside the directory %TEMP%. The %TEMP% is the windows temporary directory for you user.

