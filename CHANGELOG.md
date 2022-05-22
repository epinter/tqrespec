# Changelog
## [0.11.1] - 2022-05-22
### Added
- Add support for TQ v2.10.20820
- Enable JPMS for all dependencies using improved jar patching system

### Changed
- Improved savegame parse to better handle new format
- Updated Russian and Ukranian translations (thanks Evgeniy Chefranov)
- Refactor resource loading to work with JPMS

### Fixed
- Fix minor ui initialization bug

## [0.11.0] - 2021-12-04
### Added
- Add support for TQ v2.10.19520 (released with Eternal Embers) 

### Changed
- Java upgraded to 17

### Fixed
- Fix minor UI bug
- Update Steam game detection process

## [0.10.2] - 2021-06-09
### Changed
- Changed UI string text from 'Android' to 'Mobile'

### Fixed
- Fix manual path chooser (shown when game installation isn't found)

## [0.10.1] - 2021-05-27
### Changed
- Add quest files to automatic backup (backup size increased)
- Support new mobile savegame format

### Fixed
- Fix update link to properly open web browser.

## [0.10.0] - 2021-05-22
### Added
- Support for respec attributes of mobile save games (TQ Legendary Edition for Android)
- Support for skills points reclaim of mobile save games (TQ Legendary Edition for Android)
- Support for copy and convert mobile savegames to Windows (TQ Legendary Edition for Android)
- Support for copy and convert Windows savegames to mobile (TQ Legendary Edition for Android)
- Savedata directory to work on savegames outside 'My Games' (Android or Windows)
- Backup feature (takes a full backup of the selected character)
- Export Player.chr to JSON (for debugging or analysis)

### Changed
- Gamedata directory is now considered before game installation. Files placed there will override detected game.
- Always monitor TQ.exe to disable save.
- Completely refactor binary file patching system to simplify and improve extendability
- Improve parsing system and data type detection
- UI changed to accomodate new mobile features

### Fixed
- Minor UI bug fixed

## [0.9.2] - 2021-04-30
### Added
- Copy character's storage when copying character ([#1](https://github.com/epinter/tqrespec/issues/1))

### Changed
- Improved window resize
- Add wait cursor for longer operations
- Better preloader visual

### Fixed
- Fixed minor UI bugs

## [0.9.1] - 2021-04-17
### Fixed
- Fix version check
- Fix crash when savegame directory is not found

## [0.9.0] - 2021-04-17
### Added
- Add feature to change character gender ([#27](https://github.com/epinter/tqrespec/issues/27))
- Add feature to list all characters and export CSV ([#31](https://github.com/epinter/tqrespec/issues/31))
- Add initial support for teleports (not in use yet)

### Changed
- Small improvements to visual and behavior of scrollbars and main window components
- Add option to disable scaling and aspect ratio when resizing specific window
- Resized main window and some controls

### Fixed
- Fix a rare case when alert was getting stuck
- Fix random UI bug when spinners didn't work

## [0.8.2] - 2020-10-31
### Added
- Added support for multiple databases and resources (used for TQ-IT legacy disc and future mods support)
- Added translation for uk (by Evgeniy Chefranov)
- Added translation for it (by Loris Gabriele)
- Alert user when character have a skill not present in game database

### Changed
- Refactor game detection to handle installation types, game version and dlcs
- Removed unneed dlls from app directory

### Fixed
- Fix crash when running in development without jpms support
- Fix compatibility with legacy disc version

## [0.8.1] - 2020-08-29
### Added
- Added support for multiple languages (controlled by game option)
- Added translation for pt-br
- Added translation for ru

## [0.8.0] - 2020-06-21
### Fixed
- Fix crash while parsing database with empty skills([#17](https://github.com/epinter/tqrespec/issues/17))
- Fix crash while searching for game installation([#19](https://github.com/epinter/tqrespec/issues/19))
- Better handling of empty data from database

### Added
- Prompt user to configure game path if not detected
- Logging
- Command line options for debugging (--debug <number>)
- Support 32bit builds

### Changed
- Code cleanup
- Frameworks/libraries upgraded
- Runtime upgraded
- Improve save file parsing, add support for more data types

## [0.7.2] - 2019-08-01
### Fixed
- Fixed bug when saving available points

## [0.7.1] - 2019-07-18
### Added
- Detect 'alternative' types of game installation, based on steam
- Detect game installation from Microsoft Store ([#11](https://github.com/epinter/tqrespec/issues/11))

### Changed
- Properly reset UI after character copy
- Attributes increments and minimum are read from database, not hardcoded anymore
- Improved path detection, skips invalid directories
- Save and use last successfull used game installation path (if valid)
- Memory usage optimization

### Fixed
- Fixed out of memory bug when parsing invalid string
- Fixed parsing of characters with itemName value ([#13](https://github.com/epinter/tqrespec/issues/13))

## [0.7.0] - 2019-07-14
### Added
- Mastery removal feature ([#5](https://github.com/epinter/tqrespec/issues/5))
- Reset button
- Added feature to run without game installed, reads gamedata from a directory ([#9](https://github.com/epinter/tqrespec/issues/9))
- Block TQRespec usage when game is running ([#7](https://github.com/epinter/tqrespec/issues/7))
- Added icons to buttons

### Changed
- Parsing process rewritten
- Before loading character, verify that every data in savegame is known, aborts if not

### Fixed
- Don't crash if Steam is not installed
- Fixed minor bug on window resize 

## [0.6.1] - 2019-07-06
### Fixed
- Fixed GOG library detection

## [0.6.0] - 2019-07-06
### Added
- Feature to reclaim points from all skills
- Feature to reclaim mastery points above level 1
- Added game text and database data parsing (for skills and class names)
- Distribution package creation and app-image automated in build system
- Added some needed unit tests

### Changed
- Improved errors and exceptions handling
- Updated from Java 9.0.4 to Java 11.0.2
- Updated JavaFX to 11.0
- Improved application startup
- Project and source code refactor, upgraded dependencies, Dependency Injection
- Build script refactor
- Added support for multiplatform development

### Fixed
- Small bug fixes and code warnings

### Removed
- Removed .jar and reduced .zip distribution formats
- Removed dist scripts

## [0.2.1] - 2019-05-12
### Added
- Compatibility with Titan Quest 2.1

### Fixed
- Fixed bug when loading character

## [0.2.0] - 2018-04-07
### Added
- Feature to copy character
- Added script to generate zip with app-image
- Discard all in memory player data when an exception occurs

### Changed
- Rework UI
- Added support to tabs on main window, for future
- Rework player parsing and in-memory patching of raw data

### Fixed
- Disable ui controls while saving or copying
- Encode player name properly, to support more characters and avoid bugs

## [0.1.2] - 2018-01-13
### Added
- GUI
- New version check
- Dist scripts

### Changed
- Data parsing rework
- Project and source code refactor

## [0.1.1] - 2018-01-07
### Added
- Initial testing version, core classes, without GUI

[unreleased]: https://github.com/epinter/tqrespec/compare/v0.7.1...HEAD
[0.7.1]: https://github.com/epinter/tqrespec/compare/v0.7.0...v0.7.1
[0.7.0]: https://github.com/epinter/tqrespec/compare/v0.6.1...v0.7.0
[0.6.1]: https://github.com/epinter/tqrespec/compare/v0.6.0...v0.6.1
[0.6.0]: https://github.com/epinter/tqrespec/compare/v0.2.1...v0.6.0
[0.2.1]: https://github.com/epinter/tqrespec/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/epinter/tqrespec/compare/v0.1.2...v0.2.0
[0.1.2]: https://github.com/epinter/tqrespec/compare/v0.1.1...v0.1.2
