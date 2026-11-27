# Changelog

## 0.3.2
### Fixed
- Crash "TickNextTick list out of synch": torch placement during staged (background) city generation called vanilla canPlaceBlockAt, which loaded/populated chunks off the server thread. Now checked against the staged view.

## 0.3.1
### Changed
- Performance: uses ArchaicLib 1.40.0 FastRandom instead of java.util.Random, plus the 1.40.0 cached block-state / allocation-free legacy world calls. Requires ArchaicLib 1.40.0+.

## 0.3.0
- First release of the 1.12.2 (Cleanroom) port of Walled City Generator / Great Wall / CA Ruins by formivore. Requires ArchaicLib 1.36.0+.
