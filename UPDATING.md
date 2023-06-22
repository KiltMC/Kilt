## Updating Kilt to newer versions of Forge
1. Change the `forge_commit_hash` in [gradle.properties](gradle.properties)
2. Run `cloneForgeApi`, then `createPatches`
3. In IntelliJ, manually go through the patch diffs and remove any unnecessary `-` signs. 
   If a new block of code has been removed and has no `+` signs around it, just remove that block. **The only exception is ForgeHooksClient.**
4. After matching, run `getForgeApi` to automatically correct the sources.