# Go Mod Helper

![Build](https://github.com/ouedyan/go-mod-helper/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/20965-go-mod-helper.svg)](https://plugins.jetbrains.com/plugin/20965-go-mod-helper)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/20965-go-mod-helper.svg)](https://plugins.jetbrains.com/plugin/20965-go-mod-helper)

<!-- Plugin description -->
Provides helper docs, suggestions and actions for Go modules system dependencies.<br/>
Warns you about newer available dependency versions, and provides additional documentation to your go.mod file.

<ul>
<li>
<h3>Go mod dependencies updates</h3>
Warns you whenever there's a new version available for a direct dependency. Press <code>alt+⮐</code> or <code>⌥+⮐</code> and choose <em>"Update to the latest version"</em>.
</li>
<li>
<h3>Go Package's documentation</h3>
  Hover over the dependency's name in your go.mod to see its documentation.
</li>
<li>
<h3>Go Package's changelog</h3>
  Keep a track of your dependencies' changes.<br/>
  Hover over its version number to view its changelog.
</li>
<li>
<h3>Open a dependency's page on <a href="https://pkg.go.dev/">pkg.go.dev</a></h3>
  Hover over a dependency's name and follow the dependency's pkg.go.dev external link at the bottom.
</li>
<li>
<h3>Edit Linting Rules</h3>
  You can edit or disable newer dependency version warning just like you would do with a normal inspection.<br/> Just
  head to <kbd>Settings/Preferences</kbd> > <kbd>Editor</kbd> > <kbd>Inspections</kbd> > <kbd>Go modules</kbd> > <kbd>
  General</kbd> > <kbd>Update go.mod direct dependencies...</kbd> inspection config.
</li>
</ul>

<!-- Plugin description end -->

![Newer version available screenshot](https://live.staticflickr.com/65535/52660524688_e266863260_o.png)

![Update to the latest version screenshot](https://live.staticflickr.com/65535/52660317059_b80aee4dc2_o.png)

![Dependency's documentation screenshot](https://live.staticflickr.com/65535/52660481450_4f9f15024a_o.png)

![Dependency's changelog screenshot](https://live.staticflickr.com/65535/52660524658_57c2a3f465_o.png)

![Open dependency's page page on pkg.go.dev screenshot](https://live.staticflickr.com/65535/52659539257_539c40a7be_o.png)

![Edit linting rules screenshot](https://live.staticflickr.com/65535/52659539232_19e8f6715b_o.png)

## Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "
  go-mod-helper"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/ouedyan/go-mod-helper/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
