# Knit Loader

A heavy abstraction of Kilt's loader code to allow for reuse in other bridge-type mods. Kilt's loader will continue to
exist under `KiltLoader`, but it will be extending off of Knit Loader to allow for proper mod loading in different mod loaders.

## Supported Loaders
- [FabricMC](https://fabricmc.net)
  - This is always going to be first priority support, as Fabric is very much the mod loader that I
    continue to choose to use, despite currently utilizing methods that are incredibly unsupported to 
    load mods directly into the loader.
- [QuiltMC](https://quiltmc.org)
  - Quilt support may be a bit shaky at first, but it will be possible. Depending on how the loader plugin API goes,
    Knit may undergo many changes to try to fit Quilt's loader plugins.
- [CichlidMC](https://cichlidmc.fish)
  - Cichlid is a next-generation Minecraft mod loader that is currently still in heavy development.
    While Kilt itself may not be supported under Cichlid for a while, the pathway will already be opened ahead of time.
    For official Cichlid support within Kilt, we currently need to wait on:
    - Fabric loader plugin
      - This is essential, as Kilt is very highly built around Fabric API bridging, and will end up suffering without it. 
    - Mixin support
      - This is top priority, because Kilt utilizes mixins very extensively throughout the codebase. 

## FAQ (probably)

### Why abstract the loader code away?
For the obvious reason that I don't want to keep copy-pasting code all over the place whenever
I want to make a new bridge mod. Kilt may very well not be my only bridge-type mod to create. I have other plans.

### Why support all these mod loaders?
Kilt was originally intended to only bring (Neo)Forge mods to Fabric. But I started getting an increasing amount of questions
about Quilt support, which I wasn't initially expecting. I was originally going to do a similar hack that I do for Fabric support
to implement Quilt, but I realized Kilt and other mods would very heavily benefit if I did properly support the intended way to do
such a thing. Additionally, this also allows Kilt to quickly implement support for Fabric's own loader plugins, whenever that occurs.

## License
Unlike Kilt, Knit Loader is instead licensed under the MIT license.

I did actually want to license the loader portion of Kilt under a permissive license from the beginning, and that's how I've generally treated it.
The reason why Kilt itself is licensed under LGPL-2.1 is specifically because that is the license both Forge and
NeoForge uses. Because Kilt is largely reimplementing and reusing the LGPL-licensed code, I do not have the right to
use a different license. But because Knit itself is separated, I'm able to license it more permissively.