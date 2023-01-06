# Kilt
A Fabric mod that brings Forge mods into the Fabric ecosystem. Very experimental.

## FAQ (probably)
### Why?
I like Fabric. And I hate Forge.

### No, but like, doesn't Patchwork exist?
Patchwork has been in development for over 2 years, and I essentially got tired of waiting.
The source code that's currently being worked on isn't even available to the public!

### Okay... so how does this work?
So, instead of just bringing FML into Fabric, this essentially
reimplements most of Forge's APIs by either doing it in its own way, or using
an API that already exists, and translating it into a Forge API call.

### How have you not lost your sanity doing this?
Already have. Next question.

## for developers, TODO: move into another file
The forgeinjects package is designated specifically for the things
Forge patches, for having a way more compatible

## Credits
I want to give a huge amount of thanks to the Fabricators of Create
for making [Porting Lib](https://github.com/Fabricators-of-Create/Porting-Lib),
as without it, this would have been significantly harder to do.