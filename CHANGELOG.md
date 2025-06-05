# Changelog
## [1.1.0] - 2025-06-05
### Added
- Mod support
- Monitor game process on Linux

### Fixed
- Fixed texconverter-cli not starting on Windows

### Changed
- Switch to G1GC garbage collector
- Use case-insensitive game and save path detection

## [1.0.3] - 2025-06-01
### Added
- Added tag to characters list (for external saves).
- Added tool to extract game files, for ARC and ARZ. With option to convert textures. See the [README.md](src/main/java/dev/pinter/tqextract/README.md).
- Added tool to convert TEX textures.
- Added map decompiler.

### Changed
- Updated Russian translation ([#57](https://github.com/epinter/tqrespec/pull/57)). By [@chefranov](https://github.com/chefranov).
- Updated Ukrainian translation ([#57](https://github.com/epinter/tqrespec/pull/57)).  By [@chefranov](https://github.com/chefranov).
- Improve Linux scripts, now there's an option to create a .desktop.
- Read attribute, level and skill points from database.

## [1.0.2] - 2025-05-08
### Fixed
- Fixed version check

## [1.0.1] - 2025-05-08
### Fixed
- Fixed full backup on Linux

## [1.0.0] - 2025-05-08
### Fixed
- Fixed linux script not passing debug parameter
- Updated google-guava library, fixes a bug
- Minor UI improvents and fixes

### Changed
- Improved Linux support

### Added
- Full backup of character, can be enabled in Misc tab
- Detect Steam game and savegame paths on Linux
- Electrum
- Editing of Skill points available
- Gold editing
- Electrum editing
- Level editing
- Unrestricted attributes editing (not locked by points available)
- Reset character stats
- Teleports add/remove
- Automatic 'Legendary Hero' restriction removal when unlocking teleports to normal/epic difficulties
- Unlocks all inventory bags
- More tests to improve reliability

## [0.14.0] - 2025-01-19
### Fixed
- Don't remove altMoney(Electrum) when converting save to mobile, issue #54

### Changed
- Updated OpenJFX SDK
- Improved Linux support

## [0.13.1] - 2024-07-03
### Fixed
- Fix non-latin languages font
- Code updated and optimized for Java 21
- Fix code warnings

### Added
- Use game font if available

## [0.13.0] - 2024-05-31
### Fixed
- Fixed crash during parse of edited options.txt, issue #51.
- Fixed rare crash on launch when older java versions are installed, issue #50.

### Changed
- Improved logging of uncaught runtime exceptions.
- Updated to Java 21
- Updated dependencies

## [0.12.2] - 2024-05-27
### Fixed
- Fix missing bags when converting from mobile.

## [0.12.1] - 2022-12-12
### Changed
- Updated Russian and Ukranian translations (thanks Evgeniy Chefranov)
- Updated JDK to 17.0.5, fixes generic error message popup when application terminates with non-zero exit status

## [0.12.0] - 2022-09-18
### Added
- Archive (activated in characters window). When archived, the savegame is moved so is hidden from game.

### Changed
- Font replaced.

## [0.11.3] - 2022-09-05
### Added
- French translation (by [EtienneLamoureux](https://github.com/EtienneLamoureux))

### Changed
- Repositioned UI elements to avoid truncated text on tabs

### Fixed
- Fixed error when reading custom .arc

## [0.11.2] - 2022-06-09
### Fixed
- Add missing variable (buffSkillName) from update v2.10.20820 ([#42](https://github.com/epinter/tqrespec/issues/42))

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
