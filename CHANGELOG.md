# Changelog

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
