# Changelog

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
