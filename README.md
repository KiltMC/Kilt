<h1 align="left">
<img align="center" width="56" alt="Kilt Logo" src="https://github.com/KiltMC/Kilt/blob/version/1.20.1/src/main/resources/assets/kilt/icon.png?raw=true"> Kilt
</h1>

A Fabric mod that brings (Neo)Forge mods into the Fabric ecosystem. Very experimental.

[![Discord invite](https://github.com/KiltMC/Kilt/blob/version/1.20.1/.github/assets/discord-plural_vector%201.svg?raw=true)](https://discord.gg/enGK2TymYJ)
[![Buy Me A Coffee](https://raw.githubusercontent.com/intergrav/devins-badges/c7fd18efdadd1c3f12ae56b49afd834640d2d797/assets/cozy/donate/kofi-plural-alt_vector.svg?raw=true)](https://ko-fi.com/bluspring)<br>
[![Wakatime](https://wakatime.com/badge/github/KiltMC/Kilt.svg)](https://wakatime.com/badge/github/KiltMC/Kilt)

## FAQ
### Why?
I like Fabric. And I hate Forge. More reasons [here](WHY.md).

### Download?
First, you must understand that Kilt is currently highly unstable, may break worlds, may cause crashes,
hence why it is currently published as **Alpha** to [Modrinth](https://modrinth.com/mod/kilt).

If you encounter any bugs or crashes, **do not report them to the mod developers unless they also occur on Forge**.
Instead, report them onto the [Kilt issue tracker](https://github.com/KiltMC/Kilt/issues), if a similar issue does not exist.

Now that that's out of the way, the download link may be accessed from the GitHub Actions page for nightly builds, and a quick access
download link may be found in the Discord (#rules-and-info), which also always downloads the latest build.

You may also download it from [Modrinth](https://modrinth.com/mod/kilt) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/kilt) for more "stable" release builds.

### What about updating/backporting to other versions of Minecraft?
While updates to newer versions are planned, Kilt is an incredibly large undertaking for only two people to work on.
A NeoForge 1.21.1 build is currently in progress, however that is going to take some time. Afterwards, we are able to move forward onto the latest version of Minecraft. 
Anything in-between and earlier is going to be unsupported, however someone else may take on that role instead.

### What about Patchwork?
Patchwork completely halted development. 
Even then, Kilt has far surpassed patchwork in most ways.

### What about Connector?
Connector's aim is to have Fabric mods on Forge, while Kilt aims to have Forge mods on Fabric.
Isn't it reasonable to just have both?

### Okay... so how does this work?
Basically this recreates FML in a way that functions with Fabric Loader, with some of its own changes
to hopefully improve on its performance, and bridging together Forge APIs with Fabric-native APIs for the sake
of compatibility. Additionally, the entire Forge API is already bundled within Kilt, it just relies on the
patch re-implementations (known as "injects" in Kilt's codebase) to be created first in mixin form.

For making the Forge mods themselves work, Kilt first remaps them from the Forge SRG format into Fabric's Intermediary format,
then applies some of its own "fixers" to ensure that everything will work correctly in the Fabric environment.

### How have you not lost your sanity doing this?
[Already have. Next question.](https://github.com/KiltMC/Kilt/blob/version/1.20.1/screaming.txt)

## Credits & Acknowledgements
I want to give a huge amount of thanks to the [Fabricators of Create](https://github.com/Fabricators-of-Create)
for making [Porting Lib](https://github.com/Fabricators-of-Create/Porting-Lib),
as without it, this would have been significantly harder to do.

Thank you to the [Minecraft Forge](https://github.com/MinecraftForge) developers, [cpw](https://github.com/cpw) and [LexManos](https://github.com/LexManos), and all of its contributors,
for making the Forge API, and having it open-sourced.

Thank you to the [FabricMC](https://fabricmc.net) developers, [modmuss50](https://github.com/modmuss50), [sfPlayer1](https://github.com/sfPlayer1), and [asiekierka](https://github.com/asiekierka), for
creating Fabric.

And thank you to my friend [Zuite](https://twitter.com/Zuite_), for being the wall that I
throw all my code frustrations and thought processes at, as
she has helped me tremendously to just stop and think about all of the
problems at hand.
